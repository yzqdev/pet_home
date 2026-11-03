package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.network.PetCompassDataPacket;
import com.github.yzqdev.pethome.server.entity.CitadelEntityData;
import com.github.yzqdev.pethome.server.entity.RecallBallEntity;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 宠物罗盘核心：
 * 1. PetId 永久身份 —— 首次驯服（懒初始化）时生成并写入宠物 citadel 数据（随实体持久化），
 *    宠物床复活的完整 NBT 会把它带回新实体，PetId 跨死亡/复活/存档不变；
 *    Entity UUID 只是当前实体实例的定位手段。
 * 2. PetRecord 档案 —— 事件驱动更新（join/tick 低频/死亡），存于 PHWorldData（内存更新 + setDirty，
 *    由原版正常保存机制落盘，不每 tick 写盘）。
 * 3. GUI 数据 —— 玩家右键罗盘后按 owner 打包档案（含动态 isLoaded/实时位置），S2C 发给玩家。
 */
public class PetCompassTracker {

    /** 宠物永久身份在 citadel 数据里的键（三侧同名） */
    public static final String PET_ID_TAG = "PetId";

    // ==================== PetId 身份 ====================

    /** 读宠物 PetId（citadel 数据的 {@link #PET_ID_TAG} 键）；未生成或格式非法返回 null，不写入。 */
    @Nullable
    public static UUID getPetId(LivingEntity pet) {
        CompoundTag tag = CitadelEntityData.getCitadelTag(pet);
        String id = tag.getString(PET_ID_TAG);
        try {
            return id.isEmpty() ? null : UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 写入 PetId 到宠物 citadel 数据（随实体持久化；宠物床复活时随完整 NBT 回到新实体） */
    private static UUID ensurePetId(LivingEntity pet) {
        UUID petId = getPetId(pet);
        if (petId != null) {
            return petId;
        }
        petId = UUID.randomUUID();
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(pet);
        tag.putString(PET_ID_TAG, petId.toString());
        CitadelEntityData.setCitadelTag(pet, tag);
        return petId;
    }

    // ==================== 档案更新（事件驱动） ====================

    /**
     * 更新宠物档案：懒初始化（首次驯服生成 PetId + 建档）+ 实时字段刷新。
     * 入口：EntityJoinLevelEvent（加载/维度切换/复活）与 LivingTickHandler 的 20 tick 低频路径。
     * 幂等：无变化时不写不标脏。
     */
    public static void updateRecord(LivingEntity pet) {
        if (pet.level().isClientSide() || !pet.isAlive() || !TameableUtils.isTamed(pet)) {
            return;
        }
        PHWorldData data = PHWorldData.get(pet.level());
        if (data == null) {
            return;
        }
        UUID petId = ensurePetId(pet);
        UUID ownerUUID = TameableUtils.getOwnerUUIDOf(pet);
        if (ownerUUID == null) {
            return;
        }
        PetRecord updated = new PetRecord(
                petId,
                ownerUUID,
                ForgeRegistries.ENTITY_TYPES.getKey(pet.getType()).toString(),
                pet.getName().getString(),
                pet.level().dimension().location().toString(),
                pet.getBlockX(), pet.getBlockY(), pet.getBlockZ(),
                pet.getUUID().toString(),
                pet.level().getGameTime(),
                true);
        PetRecord existing = data.getPetRecord(petId);
        if (existing != null && sameData(existing, updated) && !existing.inBall) {
            return;
        }
        // 宠物实体上线（含宝盒重建）：inBall 标记随之清除
        if (existing != null) {
            updated.inBall = false;
        }
        data.putPetRecord(updated);
    }

    /** 宠物被召回宝盒收存：档案标记 inBall，位置/维度改为宝盒落点（实体已 discard，之后按宝盒定位）。 */
    public static void onPetCapturedByRecallBall(LivingEntity pet, RecallBallEntity ball) {
        if (pet.level().isClientSide()) {
            return;
        }
        UUID petId = getPetId(pet);
        if (petId == null) {
            return;
        }
        PHWorldData data = PHWorldData.get(pet.level());
        if (data == null) {
            return;
        }
        PetRecord record = data.getPetRecord(petId);
        if (record == null) {
            return;
        }
        record.inBall = true;
        record.ballUuid = ball.getUUID().toString();
        record.ballKind = "recall";
        record.dimension = ball.level().dimension().location().toString();
        record.x = ball.getBlockX();
        record.y = ball.getBlockY();
        record.z = ball.getBlockZ();
        record.lastKnownTime = pet.level().getGameTime();
        data.putPetRecord(record);
    }

    /**
     * 宠物被宠物球（补兽网）收存：与宝盒同构地标记 inBall，位置改为「装宠物的物品」所在处。
     * 宠物球里的宠物是 ItemEntity（掉落物）或已在玩家背包，故入参按 {@link Entity} 取落点——
     * 无掉落物（直接进背包）时退化为玩家位置。不接这条链路时，档案会一直指向已 discard 的旧实体 UUID，
     * 罗盘显示「状态：未加载」且召回走强载队列必然超时失败。
     */
    public static void onPetCapturedByNet(LivingEntity pet, @Nullable Entity container) {
        if (pet.level().isClientSide()) {
            return;
        }
        UUID petId = getPetId(pet);
        if (petId == null) {
            return;
        }
        PHWorldData data = PHWorldData.get(pet.level());
        if (data == null) {
            return;
        }
        PetRecord record = data.getPetRecord(petId);
        if (record == null) {
            return;
        }
        record.inBall = true;
        if (container != null) {
            record.ballUuid = container.getUUID().toString();
            record.ballKind = "net";
            record.dimension = container.level().dimension().location().toString();
            record.x = container.getBlockX();
            record.y = container.getBlockY();
            record.z = container.getBlockZ();
        }
        record.lastKnownTime = pet.level().getGameTime();
        data.putPetRecord(record);
    }

    /** 宠物死亡：档案保留、alive 置 false（罗盘档案不因实体消失而删除）。 */
    public static void onPetDeath(LivingEntity pet) {
        if (pet.level().isClientSide() || !TameableUtils.isTamed(pet)) {
            return;
        }
        UUID petId = getPetId(pet);
        if (petId == null) {
            return;
        }
        PHWorldData data = PHWorldData.get(pet.level());
        if (data == null) {
            return;
        }
        PetRecord record = data.getPetRecord(petId);
        if (record == null) {
            return;
        }
        if (TameableUtils.getPetBedPos(pet) != null) {
            // 有宠物床：标记待复活，档案保留
            if (record.alive) {
                record.alive = false;
                data.putPetRecord(record);
            }
        } else {
            // 没绑宠物床：死亡即永久消失，移除档案（不应显示"等待宠物床复活"）
            data.removePetRecord(petId);
        }
    }

    private static boolean sameData(PetRecord a, PetRecord b) {
        return a.ownerId.equals(b.ownerId)
                && a.entityType.equals(b.entityType)
                && a.displayName.equals(b.displayName)
                && a.dimension.equals(b.dimension)
                && a.x == b.x && a.y == b.y && a.z == b.z
                && a.entityUuid.equals(b.entityUuid)
                && a.alive == b.alive;
    }

    // ==================== GUI 数据打包 ====================

    /** 玩家右键罗盘（C2S pet_compass_open）：按 owner 打包档案 + 动态状态 + 配置开关，S2C 发回 */
    public static void handleOpenRequest(ServerPlayer player) {
        if (!PetHomeMod.CONFIG.petCompassEnable.get()) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.disabled")));
            return;
        }
        PHWorldData data = PHWorldData.get(player.level());
        CompoundTag out = new CompoundTag();
        out.putBoolean("CfgToPet", PetHomeMod.CONFIG.petCompassTeleportPlayerToPet.get());
        out.putBoolean("CfgToPlayer", PetHomeMod.CONFIG.petCompassTeleportPetToPlayer.get());
        int count = 0;
        if (data != null) {
            List<PetRecord> records = data.getPetRecordsFor(player.getUUID());
            for (PetRecord record : records) {
                CompoundTag entry = new CompoundTag();
                entry.putString("PetId", record.petId.toString());
                entry.putString("Name", record.displayName);
                entry.putString("Type", record.entityType);
                entry.putString("Dim", record.dimension);
                entry.putString("EntityUuid", record.entityUuid);
                entry.putLong("LastTime", record.lastKnownTime);
                boolean loaded = false;
                boolean alive = record.alive;
                // 动态状态：实体在线则用实时位置（并顺手回写档案，保证「最后已知位置」新鲜）
                Entity entity = findEntityByEntityUuid(player, record.entityUuid);
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    // 所有权校验：宠物被放生/转让后清除旧主档案
                    UUID liveOwner = TameableUtils.getOwnerUUIDOf(living);
                    if (liveOwner == null || !liveOwner.equals(player.getUUID())) {
                        data.removePetRecord(record.petId);
                        continue;
                    }
                    loaded = true;
                    entry.putString("Dim", living.level().dimension().location().toString());
                    entry.putInt("X", living.getBlockX());
                    entry.putInt("Y", living.getBlockY());
                    entry.putInt("Z", living.getBlockZ());
                    entry.putInt("Dist", (int) living.distanceTo(player));
                    putLiveStatus(entry, living);
                } else {
                    // 不在线的死亡档案：宠物床复活请求可能已被移除（如床被破坏）——无请求即永久消失
                    if (!record.alive) {
                        boolean stillWaiting = false;
                        try {
                            stillWaiting = data.hasRespawnRequestFor(UUID.fromString(record.entityUuid));
                        } catch (IllegalArgumentException ignored) {
                        }
                        if (!stillWaiting) {
                            data.removePetRecord(record.petId);
                            continue;
                        }
                    }
                    entry.putInt("X", record.x);
                    entry.putInt("Y", record.y);
                    entry.putInt("Z", record.z);
                    if (alive) {
                        entry.putInt("Dist", (int) player.position().distanceTo(
                                new BlockPos(record.x, record.y, record.z).getCenter()));
                    }
                    // 宝盒中的宠物：按宝盒实体定位（宝盒可能被水流/活塞移动过），找到则刷新档案坐标
                    if (record.inBall) {
                        Entity ball = findEntityByEntityUuid(player, record.ballUuid);
                        if (ball != null && ball.isAlive()) {
                            record.dimension = ball.level().dimension().location().toString();
                            record.x = ball.getBlockX();
                            record.y = ball.getBlockY();
                            record.z = ball.getBlockZ();
                            data.putPetRecord(record);
                        }
                        entry.putString("Dim", record.dimension);
                        entry.putInt("X", record.x);
                        entry.putInt("Y", record.y);
                        entry.putInt("Z", record.z);
                        entry.putInt("Dist", (int) player.position().distanceTo(
                                new BlockPos(record.x, record.y, record.z).getCenter()));
                    }
                }
                entry.putBoolean("Loaded", loaded);
                entry.putBoolean("Alive", alive);
                entry.putBoolean("InBall", record.inBall);
                entry.putString("BallKind", record.ballKind);
                // 失效判定：未加载的条目里，坐标所在区块已加载却仍查不到实体 → 实体确实不在了（转让/被清除/跨版本存档），
                // 区别于「区块没载入所以暂时够不着」的普通未加载（那种召回能成功），失效条目才显示删除按钮
                entry.putBoolean("Stale", !loaded && alive && !record.inBall && isRecordStale(player, record));
                out.put("Pet" + count, entry);
                count++;
            }
        }
        out.putInt("Count", count);
        Networking.sendNonLocal(new PetCompassDataPacket(out), player);
    }

    /**
     * 档案是否失效（宠物实体确实已不在该坐标的已加载区块里）。
     * 判据：目标维度存在 + 坐标所在区块已加载（实体若还在必能被 {@code getEntity} 找到）→ 查不到即实体已消失。
     * 区块未加载时判为「暂时够不着」（不失效），因为召回的强载队列能把宠物拉回来。
     */
    private static boolean isRecordStale(ServerPlayer player, PetRecord record) {
        UUID uuid;
        try {
            uuid = UUID.fromString(record.entityUuid);
        } catch (IllegalArgumentException e) {
            return true;
        }
        MinecraftServer server = null;
        if (player != null && player.level() instanceof ServerLevel sl) {
            server = sl.getServer();
        }
        if (server == null) {
            return false;
        }
        ServerLevel level = PetCompassTeleport.resolveLevel(server, record.dimension);
        if (level == null) {
            return false;
        }
        if (level.getEntity(uuid) != null) {
            return false;
        }
        return level.isPositionEntityTicking(new BlockPos(record.x, record.y, record.z));
    }

    /**
     * 删除（遗忘）宠物档案：仅移除罗盘档案，不动实体本身——万一宠物其实还活着（如在别人那儿），删档只是不再显示。
     * 服务端校验档案所有权，防伪造包删他人档案。
     */
    public static void handleForgetRequest(ServerPlayer player, CompoundTag tag) {
        if (!PetHomeMod.CONFIG.petCompassEnable.get()) {
            return;
        }
        UUID petId;
        try {
            petId = UUID.fromString(tag.getString("PetId"));
        } catch (IllegalArgumentException e) {
            return;
        }
        PHWorldData data = PHWorldData.get(player.level());
        if (data == null) {
            return;
        }
        PetRecord record = data.getPetRecord(petId);
        if (record == null || !record.ownerId.equals(player.getUUID())) {
            return;
        }
        data.removePetRecord(petId);
        player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.forgotten")));
        handleOpenRequest(player);
    }

    /**
     * 在线宠物的实时状态（罗盘状态列）：血量、三态指令、附魔摘要。
     * 指令编号即 {@link IComandableMob#getCommand()} 原值（0 游走 / 1 停留 / 2 跟随），
     * 非本模组三态宠物（无 duck 接口）写 -1 由客户端隐藏该段；
     * 附魔摘要序列化「id路径:等级」分号串（复用 jade/信息面板同款语言键渲染），无附魔为空串。
     */
    private static void putLiveStatus(CompoundTag entry, LivingEntity living) {
        entry.putFloat("Health", living.getHealth());
        entry.putFloat("MaxHealth", living.getMaxHealth());
        int cmd = -1;
        if (living instanceof IComandableMob commandableMob) {
            cmd = commandableMob.getCommand();
        }
        entry.putInt("Cmd", cmd);
        StringBuilder enchants = new StringBuilder();
        Map<ResourceLocation, Integer> map = TameableUtils.getEnchants(living);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> e : map.entrySet()) {
                if (enchants.length() > 0) {
                    enchants.append(';');
                }
                enchants.append(e.getKey().getPath()).append(':').append(e.getValue());
            }
        }
        entry.putString("Enchants", enchants.toString());
    }

    @Nullable
    static Entity findEntityByEntityUuid(ServerPlayer player, String entityUuid) {
        try {
            UUID uuid = UUID.fromString(entityUuid);
            for (ServerLevel serverLevel : player.server.getAllLevels()) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity != null) {
                    return entity;
                }
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

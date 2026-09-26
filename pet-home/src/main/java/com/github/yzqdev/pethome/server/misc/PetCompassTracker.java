package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.network.PropertiesMessage;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.PetCompassItem;
import com.github.yzqdev.pethome.util.CitadelEntityData;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 宠物罗盘核心：
 * 1. PetId 永久身份 —— 首次驯服（懒初始化）时生成并写入宠物 citadel 数据（附件持久化），
 *    宠物床复活的完整 NBT 会把它带回新实体，PetId 跨死亡/复活/存档不变；
 *    Entity UUID 只是当前实体实例的定位手段。
 * 2. PetRecord 档案 —— 事件驱动更新（join/tick 低频/死亡），存于 PHWorldData（内存更新 + setDirty，
 *    由原版正常保存机制落盘，不每 tick 写盘）。
 * 3. GUI 数据 —— 玩家右键罗盘后按 owner 打包档案（含动态 isLoaded/实时位置），S2C 发给玩家。
 */
public class PetCompassTracker {

    /** 宠物永久身份在 citadel 数据里的键（各版本同名） */
    public static final String PET_ID_TAG = "PetId";

    // ==================== PetId 身份 ====================

    @Nullable
    public static UUID getPetId(LivingEntity pet) {
        CompoundTag tag = CitadelEntityData.getCitadelTag(pet);
        String id = tag.getStringOr(PET_ID_TAG, "");
        try {
            return id.isEmpty() ? null : UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 写入 PetId 到宠物 citadel 数据（随附件持久化；宠物床复活时随完整 NBT 回到新实体） */
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
     * 入口：EntityJoinLevelEvent（加载/维度切换/复活）与 EntityTickHandler 的 20 tick 低频路径。
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
                BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType()).toString(),
                pet.getName().getString(),
                pet.level().dimension().identifier().toString(),
                pet.getBlockX(), pet.getBlockY(), pet.getBlockZ(),
                pet.getUUID().toString(),
                pet.level().getGameTime(),
                true);
        PetRecord existing = data.getPetRecord(petId);
        if (existing != null && sameData(existing, updated)) {
            return;
        }
        data.putPetRecord(updated);
    }

    /** 宠物死亡：档案保留、alive 置 false（compass.md：罗盘不因 Entity UUID 消失而删档） */
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
        if (!PetHomeConfig.petCompassEnable) {
            player.sendSystemMessage(Component.translatable(LangDefinition.message("pet_compass.disabled")));
            return;
        }
        PHWorldData data = PHWorldData.get(player.level());
        CompoundTag out = new CompoundTag();
        out.putBoolean("CfgToPet", PetHomeConfig.petCompassTeleportPlayerToPet);
        out.putBoolean("CfgToPlayer", PetHomeConfig.petCompassTeleportPetToPlayer);
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
                    entry.putString("Dim", living.level().dimension().identifier().toString());
                    entry.putInt("X", living.getBlockX());
                    entry.putInt("Y", living.getBlockY());
                    entry.putInt("Z", living.getBlockZ());
                    entry.putInt("Dist", (int) living.distanceTo(player));
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
                }
                entry.putBoolean("Loaded", loaded);
                entry.putBoolean("Alive", alive);
                out.put("Pet" + count, entry);
                count++;
            }
        }
        out.putInt("Count", count);
        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, new PropertiesMessage(PHConstants.petCompassData, out, 0));
    }

    @Nullable
    static Entity findEntityByEntityUuid(ServerPlayer player, String entityUuid) {
        try {
            UUID uuid = UUID.fromString(entityUuid);
            return player.level() instanceof ServerLevel serverLevel ? serverLevel.getEntityInAnyDimension(uuid) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

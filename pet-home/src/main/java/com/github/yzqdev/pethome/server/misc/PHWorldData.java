package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class PHWorldData extends SavedData {

    private static final String NAME = "pet_home_world_data";
    private final List<RespawnRequest> respawnRequestList = new ArrayList<>();
    private final List<LanternRequest> lanternRequestList = new ArrayList<>();
    /** 宠物罗盘档案：petId（永久身份）→ PetRecord。宠物死亡/复活只改字段不删档。 */
    private final Map<UUID, PetRecord> petRecords = new HashMap<>();

    private PHWorldData() {
        super();
    }

    // 用于 Codec 的构造器
    private PHWorldData(List<RespawnRequest> respawnRequestList, List<LanternRequest> lanternRequestList) {
        this(respawnRequestList, lanternRequestList, Map.of());
    }

    private PHWorldData(List<RespawnRequest> respawnRequestList, List<LanternRequest> lanternRequestList, Map<String, PetRecord> petRecords) {
        super();
        this.respawnRequestList.addAll(respawnRequestList);
        this.lanternRequestList.addAll(lanternRequestList);
        petRecords.forEach((key, record) -> this.petRecords.put(UUID.fromString(key), record));
    }

    public static final Codec<LanternRequest> LANTERN_REQUEST_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.fieldOf("PetUUID").forGetter(LanternRequest::getPetUUID),
                    Codec.STRING.fieldOf("EntityType").forGetter(LanternRequest::getEntityTypeLoc),
                    UUIDUtil.CODEC.fieldOf("OwnerUUID").forGetter(LanternRequest::getOwnerUUID),
                    BlockPos.CODEC.fieldOf("ChunkPosition").forGetter(LanternRequest::getChunkPosition),
                    Codec.LONG.fieldOf("Timestamp").forGetter(LanternRequest::getTimestamp),
                    Codec.STRING.optionalFieldOf("EntityNametag", "").forGetter(LanternRequest::getNametag),
                    // Dimension 兼容旧档：该字段引入前的请求视为主世界（灯笼场景的绝大多数情况）
                    Codec.STRING.optionalFieldOf("Dimension", "minecraft:overworld").forGetter(LanternRequest::getDimension)
            ).apply(instance, LanternRequest::new)
    );

    public static final Codec<PHWorldData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RespawnRequest.CODEC.listOf().optionalFieldOf("RespawnList", List.of()).forGetter(d -> d.respawnRequestList),
                    LANTERN_REQUEST_CODEC.listOf().optionalFieldOf("LanternList", List.of()).forGetter(d -> d.lanternRequestList),
                    Codec.unboundedMap(Codec.STRING, PetRecord.CODEC).optionalFieldOf("PetRecords", Map.of()).forGetter(d -> {
                        Map<String, PetRecord> out = new HashMap<>();
                        d.petRecords.forEach((petId, record) -> out.put(petId.toString(), record));
                        return out;
                    })
            ).apply(instance, PHWorldData::new)
    );

    // 26.1: SavedDataType 构造函数为 (Identifier, Supplier<T>, Codec<T>, DataFixTypes)，
    // 第 4 个参数（数据修复类型）这里不需要，传 null
    public static final SavedDataType<PHWorldData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, NAME),
            PHWorldData::new,
            CODEC,
            null
    );

    public static PHWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
            if (overworld == null) return null;
            return overworld.getDataStorage().computeIfAbsent(TYPE);
        }
        return null;
    }

    public void addRespawnRequest(RespawnRequest request) {
        this.respawnRequestList.add(request);
        setDirty();
    }

    public void removeRespawnRequest(RespawnRequest request) {
        this.respawnRequestList.remove(request);
        setDirty();
    }

    public List<RespawnRequest> getRespawnRequestsFor(Level level, BlockPos pos) {
        List<RespawnRequest> list = new ArrayList<>();
        String dimension = level.dimension().toString();
        for (RespawnRequest request : this.respawnRequestList) {
            if (dimension.equals(request.dimension()) && pos.equals(request.bedPosition())) {
                list.add(request);
            }
        }
        return list;
    }

    public void addLanternRequest(LanternRequest request) {
        this.lanternRequestList.add(request);
        setDirty();
    }

    public void removeLanternRequest(LanternRequest request) {
        this.lanternRequestList.remove(request);
        setDirty();
    }

    public void removeMatchingLanternRequests(UUID reloaded) {
        this.lanternRequestList.removeIf(request -> request.getPetUUID().equals(reloaded));
        setDirty();
    }

    public List<LanternRequest> getLanternRequestsFor(UUID uuid) {
        List<LanternRequest> list = new ArrayList<>();
        for (LanternRequest request : this.lanternRequestList) {
            if (uuid.equals(request.getOwnerUUID())) {
                list.add(request);
            }
        }
        return list;
    }

    // —— 宠物罗盘档案（PetId 永久身份）——

    @org.jetbrains.annotations.Nullable
    public PetRecord getPetRecord(UUID petId) {
        return this.petRecords.get(petId);
    }

    /** 写入/更新档案（内部 update 语义：覆盖可变字段），标记存档脏 */
    public void putPetRecord(PetRecord record) {
        PetRecord existing = this.petRecords.get(record.petId);
        if (existing != null) {
            existing.updateFrom(record);
        } else {
            this.petRecords.put(record.petId, record);
        }
        setDirty();
    }

    /** 移除档案（宠物永久消失：死亡且无宠物床 / 放生 / 转让），标记存档脏 */
    public void removePetRecord(UUID petId) {
        if (this.petRecords.remove(petId) != null) {
            setDirty();
        }
    }

    public List<PetRecord> getPetRecordsFor(UUID ownerId) {
        List<PetRecord> list = new ArrayList<>();
        for (PetRecord record : this.petRecords.values()) {
            if (record.ownerId.equals(ownerId)) {
                list.add(record);
            }
        }
        list.sort(Comparator.comparing(r -> r.displayName));
        return list;
    }

    /** 宠物罗盘用：该宠物是否正等待宠物床复活（复活请求的实体 NBT 里带原 UUID，26.1 存档里 UUID 是 int 数组） */
    public boolean hasRespawnRequestFor(UUID petUUID) {
        for (RespawnRequest request : this.respawnRequestList) {
            Optional<int[]> uuidArray = request.entityData().getIntArray("UUID");
            if (uuidArray.isPresent() && uuidArray.get().length == 4
                    && petUUID.equals(UUIDUtil.uuidFromIntArray(uuidArray.get()))) {
                return true;
            }
        }
        return false;
    }
}
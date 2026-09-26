package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

public class PHWorldData extends SavedData {

    private static final String IDENTIFIER = "pet_home_world_data";
    private final List<RespawnRequest> respawnRequestList = new ArrayList<>();
    private final List<LanternRequest> lanternRequestList = new ArrayList<>();
    /** 宠物罗盘档案：petId（永久身份）→ PetRecord。宠物死亡/复活只改字段不删档。 */
    private final Map<UUID, PetRecord> petRecords = new HashMap<>();

    private PHWorldData() {
        super();
    }

    public static PHWorldData create() {
        return new PHWorldData();
    }

    public static PHWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
            DimensionDataStorage storage = overworld.getDataStorage();
            // 只读路径不标脏：原先每次 get 都 setDirty，实体 join 等高频读点会让存档周期性白写盘
            return storage.computeIfAbsent(new Factory<>(PHWorldData::create, PHWorldData::load), IDENTIFIER);
        }
        return null;
    }

    public static PHWorldData load(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        PHWorldData data = new PHWorldData();
        if (nbt.contains("RespawnList")) {
            RespawnRequest.CODEC.listOf().parse(NbtOps.INSTANCE, nbt.get("RespawnList"))
                    .resultOrPartial(error -> {
                        PetHomeMod.LOGGER.error("RespawnList load failed: {}", error);

                    })
                    .ifPresent(list -> data.respawnRequestList.addAll(list));
        }
        if (nbt.contains("LanternList")) {
            ListTag listtag = nbt.getList("LanternList", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompound(i);
                // Dimension 兼容旧档：该字段引入前的请求视为主世界（灯笼场景的绝大多数情况）
                data.lanternRequestList.add(new LanternRequest(innerTag.getUUID("PetUUID"), innerTag.getString("EntityType"), innerTag.getUUID("OwnerUUID"), new BlockPos(innerTag.getInt("X"), innerTag.getInt("Y"), innerTag.getInt("Z")), innerTag.getLong("Timestamp"), innerTag.getString("EntityNametag"), innerTag.contains("Dimension") ? innerTag.getString("Dimension") : "minecraft:overworld"));
            }
        }
        if (nbt.contains("PetRecords")) {
            CompoundTag recordsTag = nbt.getCompound("PetRecords");
            for (String key : recordsTag.getAllKeys()) {
                PetRecord record = PetRecord.load(recordsTag.getCompound(key));
                if (record != null) {
                    data.petRecords.put(record.petId, record);
                }
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider provider) {

        if (!this.respawnRequestList.isEmpty()) {
            RespawnRequest.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.respawnRequestList)
                    .resultOrPartial(s -> {
                        PetHomeMod.LOGGER.error("RespawnList  save failed: {}",s);
                    })
                    .ifPresent(tag -> compound.put("RespawnList", tag));
        }
        if (!this.lanternRequestList.isEmpty()) {
            ListTag listTag = new ListTag();
            for (LanternRequest request : lanternRequestList) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("PetUUID", request.getPetUUID());
                tag.putString("EntityType", request.getEntityTypeLoc());
                tag.putUUID("OwnerUUID", request.getOwnerUUID());
                tag.putLong("Timestamp", request.getTimestamp());
                tag.putString("EntityNametag", request.getNametag());
                tag.putInt("X", request.getChunkPosition().getX());
                tag.putInt("Y", request.getChunkPosition().getY());
                tag.putInt("Z", request.getChunkPosition().getZ());
                tag.putString("Dimension", request.getDimension());
                listTag.add(tag);
            }
            compound.put("LanternList", listTag);
        }
        if (!this.petRecords.isEmpty()) {
            CompoundTag recordsTag = new CompoundTag();
            for (PetRecord record : this.petRecords.values()) {
                recordsTag.put(record.petId.toString(), record.save());
            }
            compound.put("PetRecords", recordsTag);
        }
        return compound;
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

    @javax.annotation.Nullable
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
        list.sort(java.util.Comparator.comparing(r -> r.displayName));
        return list;
    }

    /** 宠物罗盘用：该宠物是否正等待宠物床复活（复活请求的实体 NBT 里带原 UUID，int 数组格式） */
    public boolean hasRespawnRequestFor(UUID petUUID) {
        for (RespawnRequest request : this.respawnRequestList) {
            int[] uuidArray = request.entityData().getIntArray("UUID");
            if (uuidArray.length == 4 && petUUID.equals(UUIDUtil.uuidFromIntArray(uuidArray))) {
                return true;
            }
        }
        return false;
    }
}

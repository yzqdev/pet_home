package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.server.NbtKeys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import java.util.*;

public class DIWorldData extends SavedData {

    private static final String IDENTIFIER = "pet_home_world_data";
    private final List<RespawnRequest> respawnRequestList = new ArrayList<>();
    /** 宠物罗盘档案：petId（永久身份）→ PetRecord。宠物死亡/复活只改字段不删档。 */
    private final java.util.Map<java.util.UUID, PetRecord> petRecords = new java.util.HashMap<>();
    private final List<LanternRequest> lanternRequestList = new ArrayList<>();

    private DIWorldData() {
        super();
    }

    public static DIWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);
            DimensionDataStorage storage = overworld.getDataStorage();
            // 只读路径不标脏：原先每次 get 都 setDirty，实体 join 等高频读点会让存档每 45 秒白写一次盘
            return storage.computeIfAbsent(DIWorldData::load, DIWorldData::new, IDENTIFIER);
        }
        return null;
    }

    public static DIWorldData load(CompoundTag nbt) {
        DIWorldData data = new DIWorldData();
        if (nbt.contains(NbtKeys.RESPAWN_LIST)) {
            ListTag listtag = nbt.getList(NbtKeys.RESPAWN_LIST, 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompound(i);
                data.respawnRequestList.add(new RespawnRequest(innerTag.getString(NbtKeys.ENTITY_TYPE), innerTag.getString(NbtKeys.DIMENSION_IN), innerTag.getCompound(NbtKeys.ENTITY_DATA),
                        new BlockPos(innerTag.getInt(NbtKeys.X), innerTag.getInt(NbtKeys.Y), innerTag.getInt(NbtKeys.Z)), innerTag.getLong(NbtKeys.TIMESTAMP), innerTag.getString(NbtKeys.ENTITY_NAMETAG)));
            }
        }
        if (nbt.contains(NbtKeys.LANTERN_LIST)) {
            ListTag listtag = nbt.getList(NbtKeys.LANTERN_LIST, 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompound(i);
                data.lanternRequestList.add(new LanternRequest(innerTag.getUUID(NbtKeys.PET_UUID), innerTag.getString(NbtKeys.ENTITY_TYPE), innerTag.getUUID(NbtKeys.OWNER_UUID), new BlockPos(innerTag.getInt(NbtKeys.X), innerTag.getInt(NbtKeys.Y), innerTag.getInt(NbtKeys.Z)), innerTag.getLong(NbtKeys.TIMESTAMP), innerTag.getString(NbtKeys.ENTITY_NAMETAG)));
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
    public CompoundTag save(CompoundTag compound) {
        if (!this.respawnRequestList.isEmpty()) {
            ListTag listTag = new ListTag();
            for(RespawnRequest request : respawnRequestList){
                CompoundTag tag = new CompoundTag();
                tag.putString(NbtKeys.ENTITY_TYPE, request.getEntityTypeLoc());
                tag.putString(NbtKeys.DIMENSION_IN, request.getDimension());
                tag.put(NbtKeys.ENTITY_DATA, request.getEntityData());
                tag.putInt(NbtKeys.X, request.getBedPosition().getX());
                tag.putInt(NbtKeys.Y, request.getBedPosition().getY());
                tag.putInt(NbtKeys.Z, request.getBedPosition().getZ());
                tag.putLong(NbtKeys.TIMESTAMP, request.getTimestamp());
                tag.putString(NbtKeys.ENTITY_NAMETAG, request.getNametag());
                listTag.add(tag);
            }
            compound.put(NbtKeys.RESPAWN_LIST, listTag);
        }
        if (!this.lanternRequestList.isEmpty()) {
            ListTag listTag = new ListTag();
            for(LanternRequest request : lanternRequestList){
                CompoundTag tag = new CompoundTag();
                tag.putUUID(NbtKeys.PET_UUID, request.getPetUUID());
                tag.putString(NbtKeys.ENTITY_TYPE, request.getEntityTypeLoc());
                tag.putUUID(NbtKeys.OWNER_UUID, request.getOwnerUUID());
                tag.putLong(NbtKeys.TIMESTAMP, request.getTimestamp());
                tag.putString(NbtKeys.ENTITY_NAMETAG, request.getNametag());
                tag.putInt(NbtKeys.X, request.getChunkPosition().getX());
                tag.putInt(NbtKeys.Y, request.getChunkPosition().getY());
                tag.putInt(NbtKeys.Z, request.getChunkPosition().getZ());
                listTag.add(tag);
            }
            compound.put(NbtKeys.LANTERN_LIST, listTag);
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

    public void addRespawnRequest(RespawnRequest request){
        this.respawnRequestList.add(request);
        setDirty();
    }

    public void removeRespawnRequest(RespawnRequest request){
        this.respawnRequestList.remove(request);
        setDirty();
    }
    public List<RespawnRequest> getRespawnRequestsFor(Level level, BlockPos pos){
        List<RespawnRequest> list = new ArrayList<>();
        String dimension = level.dimension().toString();
        for(RespawnRequest request : this.respawnRequestList){
            if(dimension.equals(request.getDimension()) && pos.equals(request.getBedPosition())){
                list.add(request);
            }
        }
        return list;
    }

    public void addLanternRequest(LanternRequest request){
        this.lanternRequestList.add(request);
        setDirty();
    }

    public void removeLanternRequest(LanternRequest request){
        this.lanternRequestList.remove(request);
        setDirty();
    }

    public void removeMatchingLanternRequests(UUID reloaded){
        this.lanternRequestList.removeIf(request -> request.getPetUUID().equals(reloaded));
        setDirty();
    }

    public List<LanternRequest> getLanternRequestsFor(UUID uuid){
        List<LanternRequest> list = new ArrayList<>();
        for(LanternRequest request : this.lanternRequestList){
            if(uuid.equals(request.getOwnerUUID())){
                list.add(request);
            }
        }
        return list;
    }

    /** 宠物罗盘用：该宠物是否正等待宠物床复活（复活请求的实体 NBT 里带原 UUID，int 数组格式） */
    public boolean hasRespawnRequestFor(UUID petUUID){
        for(RespawnRequest request : this.respawnRequestList){
            int[] uuidArray = request.getEntityData().getIntArray("UUID");
            if(uuidArray.length == 4 && petUUID.equals(UUIDUtil.uuidFromIntArray(uuidArray))){
                return true;
            }
        }
        return false;
    }

    // —— 宠物罗盘档案（PetId 永久身份）——

    @javax.annotation.Nullable
    public PetRecord getPetRecord(UUID petId){
        return this.petRecords.get(petId);
    }

    /** 写入/更新档案（内部 update 语义：覆盖可变字段），标记存档脏 */
    public void putPetRecord(PetRecord record){
        PetRecord existing = this.petRecords.get(record.petId);
        if(existing != null){
            existing.updateFrom(record);
        }else{
            this.petRecords.put(record.petId, record);
        }
        setDirty();
    }

    /** 移除档案（宠物永久消失：死亡且无宠物床 / 放生 / 转让），标记存档脏 */
    public void removePetRecord(UUID petId){
        if(this.petRecords.remove(petId) != null){
            setDirty();
        }
    }

    public List<PetRecord> getPetRecordsFor(UUID ownerId){
        List<PetRecord> list = new ArrayList<>();
        for(PetRecord record : this.petRecords.values()){
            if(record.ownerId.equals(ownerId)){
                list.add(record);
            }
        }
        list.sort(java.util.Comparator.comparing(r -> r.displayName));
        return list;
    }
}

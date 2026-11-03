package com.github.yzqdev.pethome.server.misc;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * 宠物罗盘的宠物档案：以 PetId（宠物永久逻辑身份）为主键，随宠物整个生命周期（含死亡/宠物床复活）不变。
 * Entity UUID 只是「当前实体实例」的定位手段——宠物床复活会生成新实体（新 Entity UUID），PetId 不变，
 * 罗盘始终以 PetId 识别宠物：Entity UUID 表示实体实例，PetId 表示宠物本身（实体重建后 UUID 变化而 PetId 不变）。
 */
public class PetRecord {

    /** 宠物永久身份（驯服时生成，存入宠物 citadel 数据，跨死亡/复活/存档不变） */
    public final UUID petId;
    /** 主人 UUID：隔离玩家数据，防止看到/操控他人宠物 */
    public UUID ownerId;
    /** 实体类型 id，如 minecraft:wolf */
    public String entityType;
    /** 当前显示名（自定义名优先，无则实体类型名）；宠物改名时同步 */
    public String displayName;
    /** 最后已知维度，如 minecraft:overworld */
    public String dimension;
    /** 最后已知坐标 */
    public int x;
    public int y;
    public int z;
    /** 当前实体实例 UUID（仅用于定位实体，非永久身份） */
    public String entityUuid;
    /** 最后一次成功更新位置的时间（level game time） */
    public long lastKnownTime;
    /** 是否存活（死亡等待宠物床复活时为 false，复活 join 时置回 true） */
    public boolean alive;
    /** 宠物被召回宝盒（total_recall）收存：实体已 discard，宝盒实体位于 (x,y,z)/dimension；宠物重建 join 时清除 */
    public boolean inBall;
    /** inBall 期间收存宠物的宝盒实体 UUID（定位宝盒用；"" = 未知/旧存档） */
    public String ballUuid = "";
    /** 收存容器种类：{@code recall} = 召回宝盒（TOTAL_RECALL），{@code net} = 生物球（捕网）；旧存档默认 recall */
    public String ballKind = "recall";

    public PetRecord(UUID petId, UUID ownerId, String entityType, String displayName, String dimension,
                     int x, int y, int z, String entityUuid, long lastKnownTime, boolean alive) {
        this(petId, ownerId, entityType, displayName, dimension, x, y, z, entityUuid, lastKnownTime, alive, false, "", "recall");
    }

    public PetRecord(UUID petId, UUID ownerId, String entityType, String displayName, String dimension,
                     int x, int y, int z, String entityUuid, long lastKnownTime, boolean alive,
                     boolean inBall, String ballUuid, String ballKind) {
        this.petId = petId;
        this.ownerId = ownerId;
        this.entityType = entityType;
        this.displayName = displayName;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityUuid = entityUuid;
        this.lastKnownTime = lastKnownTime;
        this.alive = alive;
        this.inBall = inBall;
        this.ballUuid = ballUuid;
        this.ballKind = ballKind;
    }

    /** 位置/维度等可变信息用本方法覆盖（petId 不可变） */
    public void updateFrom(PetRecord other) {
        this.ownerId = other.ownerId;
        this.entityType = other.entityType;
        this.displayName = other.displayName;
        this.dimension = other.dimension;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.entityUuid = other.entityUuid;
        this.lastKnownTime = other.lastKnownTime;
        this.alive = other.alive;
        this.inBall = other.inBall;
        this.ballUuid = other.ballUuid;
        this.ballKind = other.ballKind;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("PetId", this.petId.toString());
        tag.putString("OwnerId", this.ownerId.toString());
        tag.putString("EntityType", this.entityType);
        tag.putString("DisplayName", this.displayName);
        tag.putString("Dimension", this.dimension);
        tag.putInt("X", this.x);
        tag.putInt("Y", this.y);
        tag.putInt("Z", this.z);
        tag.putString("EntityUuid", this.entityUuid);
        tag.putLong("LastKnownTime", this.lastKnownTime);
        tag.putBoolean("Alive", this.alive);
        tag.putBoolean("InBall", this.inBall);
        tag.putString("BallUuid", this.ballUuid);
        tag.putString("BallKind", this.ballKind);
        return tag;
    }

    public static PetRecord load(CompoundTag tag) {
        try {
            return new PetRecord(
                    UUID.fromString(tag.getString("PetId")),
                    UUID.fromString(tag.getString("OwnerId")),
                    tag.getString("EntityType"),
                    tag.getString("DisplayName"),
                    tag.getString("Dimension"),
                    tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"),
                    tag.getString("EntityUuid"),
                    tag.getLong("LastKnownTime"),
                    tag.getBoolean("Alive"),
                    tag.getBoolean("InBall"),
                    tag.getString("BallUuid"),
                    tag.contains("BallKind") ? tag.getString("BallKind") : "recall");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

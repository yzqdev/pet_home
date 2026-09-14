package com.github.yzqdev.pethome.server;

/**
 * 项目自定义的 NBT 数据键名常量。
 * 统一管理，避免读写错字导致静默数据丢失。
 */
public final class NbtKeys {
    private NbtKeys() {
    }

    // —— 驯服宠物共享键（mixin + RecallBallEntity）——
    public static final String OWNER = "Owner";
    public static final String TAMED = "Tamed";
    public static final String DI_COMMAND = "DICommand";

    // —— 契约文书 / 交互 ——
    public static final String HAS_BOUND_ENTITY = "HasBoundEntity";
    public static final String BOUND_ENTITY = "BoundEntity";
    public static final String BOUND_ENTITY_NAME = "BoundEntityName";
    public static final String ENCHANTMENTS = "Enchantments";

    // —— 实体专用 ——
    public static final String FOLLOWER_UUID = "FollowerUUID";
    public static final String CREATOR_UUID = "CreatorUUID";
    public static final String LIFESPAN = "Lifespan";
    public static final String BLOCK_WIDTH = "BlockWidth";
    public static final String WALL_DIRECTION = "WallDirection";
    public static final String POPS_IN = "PopsIn";
    public static final String CONTAINED_ENTITY_TYPE = "ContainedEntityType";
    public static final String CONTAINED_DATA = "ContainedData";
    public static final String FINISHED = "Finished";

    // —— 方块实体 ——
    public static final String PLACER_UUID = "PlacerUUID";
    public static final String CHECK_AGAIN_IN = "CheckAgainIn";

    // —— 世界数据 DIWorldData ——
    public static final String TIMESTAMP = "Timestamp";
    public static final String ENTITY_TYPE = "EntityType";
    public static final String ENTITY_NAMETAG = "EntityNametag";
    public static final String ENTITY_DATA = "EntityData";
    public static final String DIMENSION_IN = "DimensionIn";
    public static final String RESPAWN_LIST = "RespawnList";
    public static final String LANTERN_LIST = "LanternList";
    public static final String PET_UUID = "PetUUID";
    public static final String OWNER_UUID = "OwnerUUID";
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";
}
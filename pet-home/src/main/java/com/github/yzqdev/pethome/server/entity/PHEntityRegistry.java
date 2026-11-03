package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * 实体注册：Fabric 原生 {@code Registry.register}（立即注册）。
 * 26.1: EntityType.Builder 的 setShouldReceiveVelocityUpdates / setUpdateInterval / setTrackingRange
 * 三个网络同步参数方法都已移除（26.1 改为按实体类型统一配置），这里只保留尺寸。
 */
public class PHEntityRegistry {

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    /** 连锁闪电（攻击附魔，在目标间逐跳传导）。 */
    public static final EntityType<ChainLightningEntity> CHAIN_LIGHTNING = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("chain_lightning"),
            build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune(), "chain_lightning"));
    /** 巨型泡泡（泡泡控制附魔，托起目标上浮）。 */
    public static final EntityType<GiantBubbleEntity> GIANT_BUBBLE = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("giant_bubble"),
            build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC).sized(1.2F, 1.8F).fireImmune(), "giant_bubble"));
    /** 念力墙（防御附魔，在宠物/主人与威胁之间生成阻挡墙）。 */
    public static final EntityType<PsychicWallEntity> PSYCHIC_WALL = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("psychic_wall"),
            build(EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC).sized(1F, 1F).fireImmune(), "psychic_wall"));
    /** 矿石高亮框（矿脉嗅探附魔，标记矿石位置供客户端画线框）。 */
    public static final EntityType<HighlightedBlockEntity> HIGHLIGHTED_BLOCK = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("highlighted_block"),
            build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).fireImmune(), "highlighted_block"));
    /** 羽毛钓竿抛射物（召回最近的宠物）。 */
    public static final EntityType<FeatherEntity> FEATHER = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("feather"),
            build(EntityType.Builder.<FeatherEntity>of(FeatherEntity::new, MobCategory.MISC).sized(0.2F, 0.2F).fireImmune(), "feather"));
    /** 往生球（紧急收回附魔 / 灯笼收容的宠物载体）。 */
    public static final EntityType<RecallBallEntity> RECALL_BALL = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("recall_ball"),
            build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC).sized(0.8F, 0.8F).fireImmune(), "recall_ball"));
    /** 捕网投掷物（发射器发射，命中即抓捕/释放）。 */
    public static final EntityType<NetEntity> NET_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("net_entity"),
            EntityType.Builder
                    .<NetEntity>of(NetEntity::new, MobCategory.MISC)
                    .sized(.6f, .6f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("net"))));
    /** disc_jockey ：跟随宠物的漂浮唱片机。 */
    public static final EntityType<FollowingJukeboxEntity> FOLLOWING_JUKEBOX = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("following_jukebox"),
            build(EntityType.Builder.of(FollowingJukeboxEntity::new, MobCategory.MISC).sized(0.65F, 0.65F).fireImmune(), "following_jukebox"));

    private static <T extends Entity> EntityType<T> build(EntityType.Builder<T> builder, String entityName) {
        return builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id(entityName)));
    }

    /** 空实现：仅保证本类被加载、完成上面的注册（Fabric 注册在类初始化期执行）。 */
    public static void init() {
    }
}

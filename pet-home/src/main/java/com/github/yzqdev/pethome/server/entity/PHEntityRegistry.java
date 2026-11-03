package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** 实体注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHEntityRegistry {

    /** 连锁闪电附魔的视觉效果实体（服务端生成、仅客户端表现）。 */
    public static final EntityType<ChainLightningEntity> CHAIN_LIGHTNING = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "chain_lightning"),
            build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune(), "chain_lightning"));
    /** 召回指令的视觉表现球：往生灯笼召回宠物时服务端生成，仅用于客户端动画表现。 */
    public static final EntityType<RecallBallEntity> RECALL_BALL = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "recall_ball"),
            build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F)
                    .fireImmune(), "recall_ball"));
    /** 矿脉嗅探附魔的探测羽毛：嗅探到矿脉时发射，飞向矿脉方位提示主人。 */
    public static final EntityType<FeatherEntity> FEATHER = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "feather"),
            build(EntityType.Builder.<FeatherEntity>of(FeatherEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .fireImmune(), "feather"));
    /** 巨型泡泡：附魔体系的泡泡效果实体，可困住目标使其漂浮。 */
    public static final EntityType<GiantBubbleEntity> GIANT_BUBBLE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "giant_bubble"),
            build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.8F)
                    .fireImmune(), "giant_bubble"));
    /** 跟随唱片机（DJ 附魔）：宠物携带的音乐实体，边移动边播放唱片并吸引周围生物。 */
    public static final EntityType<FollowingJukeboxEntity> FOLLOWING_JUKEBOX = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "following_jukebox"),
            build(EntityType.Builder.of(FollowingJukeboxEntity::new, MobCategory.MISC)
                    .sized(0.65F, 0.65F)
                    .fireImmune(), "following_jukebox"));
    /** 矿脉嗅探附魔的高亮方块：标记嗅探到的矿脉位置，供客户端渲染高亮轮廓。 */
    public static final EntityType<HighlightedBlockEntity> HIGHLIGHTED_BLOCK = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "highlighted_block"),
            build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .fireImmune(), "highlighted_block"));
    /** 念力墙附魔的墙体实体：阻挡敌人通过的隐形屏障，超时后自动消失。 */
    public static final EntityType<PsychicWallEntity> PSYCHIC_WALL = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "psychic_wall"),
            build(EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .fireImmune(), "psychic_wall"));
    /** 捕网实体：捕网/捕网发射器射出后困住生物的抛射物，追踪距离远、同步频率高。 */
    public static final EntityType<NetEntity> NET_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "net_entity"),
            EntityType.Builder
                    .<NetEntity>of(NetEntity::new, MobCategory.MISC)
                    .updateInterval(1)
                    .clientTrackingRange(128)
                    .sized(.6f, .6f)
                    .build("net"));

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> build(EntityType.Builder<T> builder, String entityName) {
        return builder.build(entityName);
    }

    public static void init() {
    }
}

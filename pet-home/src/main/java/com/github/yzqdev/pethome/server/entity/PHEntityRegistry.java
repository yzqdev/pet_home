package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** 实体注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHEntityRegistry {

    public static final EntityType<ChainLightningEntity> CHAIN_LIGHTNING = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "chain_lightning"),
            build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune(), "chain_lightning"));
    public static final EntityType<RecallBallEntity> RECALL_BALL = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "recall_ball"),
            build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F)
                    .fireImmune(), "recall_ball"));
    public static final EntityType<FeatherEntity> FEATHER = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "feather"),
            build(EntityType.Builder.<FeatherEntity>of(FeatherEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .fireImmune(), "feather"));
    public static final EntityType<GiantBubbleEntity> GIANT_BUBBLE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "giant_bubble"),
            build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.8F)
                    .fireImmune(), "giant_bubble"));
    public static final EntityType<FollowingJukeboxEntity> FOLLOWING_JUKEBOX = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "following_jukebox"),
            build(EntityType.Builder.of(FollowingJukeboxEntity::new, MobCategory.MISC)
                    .sized(0.65F, 0.65F)
                    .fireImmune(), "following_jukebox"));
    public static final EntityType<HighlightedBlockEntity> HIGHLIGHTED_BLOCK = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "highlighted_block"),
            build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .fireImmune(), "highlighted_block"));
    public static final EntityType<PsychicWallEntity> PSYCHIC_WALL = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "psychic_wall"),
            build(EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .fireImmune(), "psychic_wall"));
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

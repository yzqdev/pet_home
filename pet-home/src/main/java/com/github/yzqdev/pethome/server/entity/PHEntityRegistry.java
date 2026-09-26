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

    public static final EntityType<ChainLightningEntity> CHAIN_LIGHTNING = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("chain_lightning"),
            build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune(), "chain_lightning"));
    public static final EntityType<GiantBubbleEntity> GIANT_BUBBLE = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("giant_bubble"),
            build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC).sized(1.2F, 1.8F).fireImmune(), "giant_bubble"));
    public static final EntityType<PsychicWallEntity> PSYCHIC_WALL = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("psychic_wall"),
            build(EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC).sized(1F, 1F).fireImmune(), "psychic_wall"));
    public static final EntityType<HighlightedBlockEntity> HIGHLIGHTED_BLOCK = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("highlighted_block"),
            build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).fireImmune(), "highlighted_block"));
    public static final EntityType<FeatherEntity> FEATHER = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("feather"),
            build(EntityType.Builder.<FeatherEntity>of(FeatherEntity::new, MobCategory.MISC).sized(0.2F, 0.2F).fireImmune(), "feather"));
    public static final EntityType<RecallBallEntity> RECALL_BALL = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("recall_ball"),
            build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC).sized(0.8F, 0.8F).fireImmune(), "recall_ball"));
    public static final EntityType<NetEntity> NET_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE, id("net_entity"),
            EntityType.Builder
                    .<NetEntity>of(NetEntity::new, MobCategory.MISC)
                    .sized(.6f, .6f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("net"))));

    private static <T extends Entity> EntityType<T> build(EntityType.Builder<T> builder, String entityName) {
        return builder.build(ResourceKey.create(Registries.ENTITY_TYPE, id(entityName)));
    }

    public static void init() {
    }
}

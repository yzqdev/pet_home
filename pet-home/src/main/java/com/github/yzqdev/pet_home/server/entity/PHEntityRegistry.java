package com.github.yzqdev.pet_home.server.entity;


import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class PHEntityRegistry {

    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, PetHomeMod.MODID);
    public static final DeferredHolder<EntityType<?>,EntityType<ChainLightningEntity>> CHAIN_LIGHTNING = DEF_REG.register("chain_lightning", () -> build(EntityType.Builder.of(ChainLightningEntity::new, MobCategory.MISC).sized(0.5F, 0.5F) .fireImmune(), "chain_lightning"));
    public static final DeferredHolder<EntityType<?>,EntityType<GiantBubbleEntity>> GIANT_BUBBLE = DEF_REG.register("giant_bubble", () -> build(EntityType.Builder.of(GiantBubbleEntity::new, MobCategory.MISC).sized(1.2F, 1.8F).fireImmune(), "giant_bubble"));

    public static final DeferredHolder<EntityType<?>,EntityType<PsychicWallEntity>> PSYCHIC_WALL = DEF_REG.register("psychic_wall", () -> build(EntityType.Builder.of(PsychicWallEntity::new, MobCategory.MISC).sized(1F, 1F) .fireImmune(), "psychic_wall"));
    public static final DeferredHolder<EntityType<?>,EntityType<HighlightedBlockEntity>> HIGHLIGHTED_BLOCK = DEF_REG.register("highlighted_block", () -> build(EntityType.Builder.of(HighlightedBlockEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).fireImmune(), "highlighted_block"));
    public static final DeferredHolder<EntityType<?>,EntityType<FeatherEntity>> FEATHER = DEF_REG.register("feather", () -> build(EntityType.Builder.of(FeatherEntity::new, MobCategory.MISC).sized(0.2F, 0.2F).fireImmune(), "feather"));
    public static final DeferredHolder<EntityType<?>,EntityType<RecallBallEntity>> RECALL_BALL = DEF_REG.register("recall_ball", () -> build(EntityType.Builder.of(RecallBallEntity::new, MobCategory.MISC).sized(0.8F, 0.8F) .fireImmune(), "recall_ball"));
    public static DeferredHolder<EntityType<?>,EntityType<NetEntity>> NET_ENTITY=DEF_REG.register("net_entity",()-> EntityType.Builder
            .<NetEntity>of( NetEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setUpdateInterval(1)
            .setTrackingRange(128)
            .sized(.6f, .6f)
            .build("net")   );
    private static final EntityType build(EntityType.Builder builder, String entityName) {
        ResourceLocation nameLoc =  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, entityName);
        return (EntityType) builder.build(entityName);
    }
}

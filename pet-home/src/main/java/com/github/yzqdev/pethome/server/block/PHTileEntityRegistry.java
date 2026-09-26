package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;


public class PHTileEntityRegistry {

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final BlockEntityType<PetBedBlockEntity> PET_BED = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("pet_bed"),
            BlockEntityType.Builder.of(PetBedBlockEntity::new, PHBlockRegistry.PET_BED_BLOCKS.values().toArray(new Block[0])).build(null));
    public static final BlockEntityType<WaywardLanternBlockEntity> WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("wayward_lantern"),
            BlockEntityType.Builder.of(WaywardLanternBlockEntity::new, PHBlockRegistry.WAYWARD_LANTERN).build(null));
    public static final BlockEntityType<DrumBlockEntity> DRUM = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("drum"),
            BlockEntityType.Builder.of(DrumBlockEntity::new, PHBlockRegistry.DRUM).build(null));

    public static void init() {
    }
}

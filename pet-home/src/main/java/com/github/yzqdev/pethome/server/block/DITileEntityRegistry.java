package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DITileEntityRegistry {

    public static final BlockEntityType<PetBedBlockEntity> PET_BED = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "pet_bed"),
            build(BlockEntityType.Builder.of(PetBedBlockEntity::new,
                    DIBlockRegistry.WHITE_PET_BED, DIBlockRegistry.ORANGE_PET_BED, DIBlockRegistry.MAGENTA_PET_BED, DIBlockRegistry.LIGHT_BLUE_PET_BED, DIBlockRegistry.YELLOW_PET_BED, DIBlockRegistry.LIME_PET_BED, DIBlockRegistry.PINK_PET_BED, DIBlockRegistry.GRAY_PET_BED, DIBlockRegistry.LIGHT_GRAY_PET_BED, DIBlockRegistry.CYAN_PET_BED, DIBlockRegistry.PURPLE_PET_BED, DIBlockRegistry.BLUE_PET_BED, DIBlockRegistry.BROWN_PET_BED, DIBlockRegistry.GREEN_PET_BED, DIBlockRegistry.RED_PET_BED, DIBlockRegistry.BLACK_PET_BED
            )));
    public static final BlockEntityType<DrumBlockEntity> DRUM = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "drum"),
            build(BlockEntityType.Builder.of(DrumBlockEntity::new,
                    DIBlockRegistry.DRUM
            )));

    public static final BlockEntityType<WaywardLanternBlockEntity> WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "wayward_lantern"),
            build(BlockEntityType.Builder.of(WaywardLanternBlockEntity::new,
                    DIBlockRegistry.WAYWARD_LANTERN
            )));

    public static BlockEntityType build(BlockEntityType.Builder builder) {
        return builder.build(null);
    }

    public static void init() {
    }
}

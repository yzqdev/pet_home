package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PHTileEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PetHomeMod.MODID);
    public static final RegistryObject<BlockEntityType<PetBedBlockEntity>> PET_BED = DEF_REG.register("pet_bed", () -> build(BlockEntityType.Builder.of(PetBedBlockEntity::new,
            PHBlockRegistry.WHITE_PET_BED.get(), PHBlockRegistry.ORANGE_PET_BED.get(), PHBlockRegistry.MAGENTA_PET_BED.get(), PHBlockRegistry.LIGHT_BLUE_PET_BED.get(), PHBlockRegistry.YELLOW_PET_BED.get(), PHBlockRegistry.LIME_PET_BED.get(), PHBlockRegistry.PINK_PET_BED.get(), PHBlockRegistry.GRAY_PET_BED.get(), PHBlockRegistry.LIGHT_GRAY_PET_BED.get(), PHBlockRegistry.CYAN_PET_BED.get(), PHBlockRegistry.PURPLE_PET_BED.get(), PHBlockRegistry.BLUE_PET_BED.get(), PHBlockRegistry.BROWN_PET_BED.get(), PHBlockRegistry.GREEN_PET_BED.get(), PHBlockRegistry.RED_PET_BED.get(), PHBlockRegistry.BLACK_PET_BED.get()
    )));
    public static final RegistryObject<BlockEntityType<DrumBlockEntity>> DRUM = DEF_REG.register("drum", () -> build(BlockEntityType.Builder.of(DrumBlockEntity::new,
            PHBlockRegistry.DRUM.get()
    )));

    public static final RegistryObject<BlockEntityType<WaywardLanternBlockEntity>> WAYWARD_LANTERN = DEF_REG.register("wayward_lantern", () -> build(BlockEntityType.Builder.of(WaywardLanternBlockEntity::new,
            PHBlockRegistry.WAYWARD_LANTERN.get()
    )));

    public static BlockEntityType build(BlockEntityType.Builder builder){
        return  builder.build(null);
    }
}

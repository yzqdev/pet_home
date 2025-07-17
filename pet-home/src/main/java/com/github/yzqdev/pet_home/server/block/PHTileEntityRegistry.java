package com.github.yzqdev.pet_home.server.block;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class PHTileEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> DEF_REG = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PetHomeMod.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PetBedBlockEntity>> PET_BED = DEF_REG.register("pet_bed", () -> build(BlockEntityType.Builder.of(PetBedBlockEntity::new,   PHBlockRegistry.PET_BED_BLOCKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)
    )));
//    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<DrumBlockEntity>> DRUM = DEF_REG.register("drum", () -> build(BlockEntityType.Builder.of(DrumBlockEntity::new,
//            DIBlockRegistry.DRUM.get()
//    )));
//
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<WaywardLanternBlockEntity>> WAYWARD_LANTERN = DEF_REG.register("wayward_lantern", () -> build(BlockEntityType.Builder.of(WaywardLanternBlockEntity::new,
            PHBlockRegistry.WAYWARD_LANTERN.get()
    )));

    public static BlockEntityType build(BlockEntityType.Builder builder) {

        return (BlockEntityType) builder.build(null);
    }
}

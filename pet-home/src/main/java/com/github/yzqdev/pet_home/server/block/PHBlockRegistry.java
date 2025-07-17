package com.github.yzqdev.pet_home.server.block;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.item.DIBlockItem;
import com.github.yzqdev.pet_home.server.item.PetbedItem;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.function.Supplier;

public class PHBlockRegistry {

    public static final DeferredRegister.Blocks DEF_REG = DeferredRegister.createBlocks(PetHomeMod.MODID);

    public static final HashMap<DyeColor, DeferredHolder<Item, PetbedItem>> PetBedItems = new HashMap<>();
    public static final HashMap<DyeColor, DeferredHolder<Block, Block>> PET_BED_BLOCKS = new HashMap<>();

        public static final DeferredHolder<Block,Block> WAYWARD_LANTERN = registerBlockAndItem("wayward_lantern", () -> new WaywardLanternBlock());

    public static DeferredHolder<Block,Block> registerBlockAndItem(String name, Supplier<Block> block){
        DeferredHolder<Block,Block> blockObj = DEF_REG.register(name, block);
        PHItemRegistry.DEF_REG.register(name, () -> new DIBlockItem(blockObj, new Item.Properties()));
        return blockObj;
    }
    static {
        for (DyeColor color : DyeColors.COLORS.keySet()) {
            DeferredHolder<Block, Block> blockObj = DEF_REG.register("pet_bed_" + color.name().toLowerCase(), () -> new PetBedBlock(color.name().toLowerCase(), color));
            PET_BED_BLOCKS.put(color, blockObj);
            PetBedItems.put( color, PHItemRegistry.DEF_REG.register("pet_bed_" + color.name().toLowerCase(), () -> new PetbedItem(blockObj, new Item.Properties(), color)));
        }
    }

}

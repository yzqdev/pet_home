package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.DIBlockItem;
import com.github.yzqdev.pethome.server.item.PetbedItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.function.Supplier;


public class PHBlockRegistry {

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final HashMap<DyeColor, Item> PetBedItems = new HashMap<>();
    public static final HashMap<DyeColor, Block> PET_BED_BLOCKS = new HashMap<>();

    public static final Block WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK, id("wayward_lantern"), new WaywardLanternBlock());
    public static final Item WAYWARD_LANTERN_ITEM = Registry.register(BuiltInRegistries.ITEM, id("wayward_lantern"), new DIBlockItem(WAYWARD_LANTERN, new Item.Properties()));

    public static final Block DRUM = Registry.register(BuiltInRegistries.BLOCK, id("drum"), new DrumBlock());
    public static final Item DRUM_ITEM = Registry.register(BuiltInRegistries.ITEM, id("drum"), new DIBlockItem(DRUM, new Item.Properties()));

    /** 注册方块并附带 DIBlockItem */
    public static Block registerBlockAndItem(String name, Supplier<Block> supplier) {
        Block block = Registry.register(BuiltInRegistries.BLOCK, id(name), supplier.get());
        Registry.register(BuiltInRegistries.ITEM, id(name), new DIBlockItem(block, new Item.Properties()));
        return block;
    }

    static {
        for (DyeColor color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            Block block = Registry.register(BuiltInRegistries.BLOCK, id(name), new PetBedBlock(color.name().toLowerCase(), color));
            PET_BED_BLOCKS.put(color, block);
            PetBedItems.put(color, Registry.register(BuiltInRegistries.ITEM, id(name), new PetbedItem(block, new Item.Properties(), color)));
        }
    }

    public static void init() {
    }
}

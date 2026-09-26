package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.DIBlockItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.PetbedItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class PHBlockRegistry {

    public static final DeferredRegister.Blocks DEF_REG = DeferredRegister.createBlocks(PetHomeMod.MODID);

    public static final HashMap<DyeColor, DeferredHolder<Item, PetbedItem>> PetBedItems = new HashMap<>();
    public static final HashMap<DyeColor, DeferredHolder<Block, Block>> PET_BED_BLOCKS = new HashMap<>();


    public static final DeferredHolder<Block, Block> WAYWARD_LANTERN = registerBlockAndItem("wayward_lantern",
            WaywardLanternBlock::new, WaywardLanternBlock::makeProperties);


    public static final DeferredHolder<Block, Block> DRUM = DEF_REG.registerBlock("drum", DrumBlock::new, DrumBlock::makeProperties);
    public static final DeferredItem<Item> DRUM_ITEM = PHItemRegistry.DEF_REG.registerItem("drum",
            props -> new DIBlockItem(DRUM, props), Item.Properties::new);

    public static DeferredHolder<Block, Block> registerBlockAndItem(String name, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredHolder<Block, Block> blockObj = DEF_REG.registerBlock(name, factory, properties);
        PHItemRegistry.DEF_REG.registerItem(name, props -> new DIBlockItem(blockObj, props), Item.Properties::new);
        return blockObj;
    }

    static {
        for (DyeColor color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            DeferredHolder<Block, Block> blockObj = DEF_REG.registerBlock(name,
                    props -> new PetBedBlock(props, color),
                    () -> PetBedBlock.makeProperties(color));
            PET_BED_BLOCKS.put(color, blockObj);
            PetBedItems.put(color, PHItemRegistry.DEF_REG.registerItem(name, props -> new PetbedItem(blockObj, props, color), Item.Properties::new));
        }
    }

}

package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.DIBlockItem;
import com.github.yzqdev.pethome.server.item.PetbedItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 方块注册：Fabric 原生 {@code Registry.register}（立即注册）。
 * 26.1 要求 Block 的 {@link BlockBehaviour.Properties} 携带注册 id，由 {@link #bprops} 统一处理。
 */
public class PHBlockRegistry {

    private static BlockBehaviour.Properties bprops(String name, Supplier<BlockBehaviour.Properties> props) {
        return props.get().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(),
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name)));
    }

    private static Item.Properties iprops(String name) {
        return new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(),
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name)));
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final HashMap<DyeColor, Item> PetBedItems = new HashMap<>();
    public static final HashMap<DyeColor, Block> PET_BED_BLOCKS = new HashMap<>();

    public static final Block WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK, id("wayward_lantern"),
            new WaywardLanternBlock(bprops("wayward_lantern", WaywardLanternBlock::makeProperties)));
    public static final Item WAYWARD_LANTERN_ITEM = Registry.register(BuiltInRegistries.ITEM, id("wayward_lantern"),
            new DIBlockItem(WAYWARD_LANTERN, iprops("wayward_lantern")));

    // 26.1: 鼓的物品与方块分开注册（datagen 需要为物品生成 items/ 定义）
    public static final Block DRUM = Registry.register(BuiltInRegistries.BLOCK, id("drum"),
            new DrumBlock(bprops("drum", DrumBlock::makeProperties)));
    public static final Item DRUM_ITEM = Registry.register(BuiltInRegistries.ITEM, id("drum"),
            new DIBlockItem(DRUM, iprops("drum")));

    /** 注册方块并附带 DIBlockItem（宠物床即走此路径的变体，见下方静态块） */
    public static Block registerBlockAndItem(String name, Function<BlockBehaviour.Properties, ? extends Block> factory, Supplier<BlockBehaviour.Properties> properties) {
        Block block = Registry.register(BuiltInRegistries.BLOCK, id(name), factory.apply(bprops(name, properties)));
        Registry.register(BuiltInRegistries.ITEM, id(name), new DIBlockItem(block, iprops(name)));
        return block;
    }

    static {
        for (DyeColor color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            Block block = Registry.register(BuiltInRegistries.BLOCK, id(name),
                    new PetBedBlock(bprops(name, () -> PetBedBlock.makeProperties(color)), color));
            PET_BED_BLOCKS.put(color, block);
            PetBedItems.put(color, Registry.register(BuiltInRegistries.ITEM, id(name),
                    new PetbedItem(block, iprops(name), color)));
        }
    }

    public static void init() {
    }
}

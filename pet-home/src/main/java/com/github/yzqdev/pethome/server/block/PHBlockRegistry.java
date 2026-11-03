package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.github.yzqdev.pethome.server.item.PHBlockItem;

import java.util.function.Supplier;

/** 方块注册：Fabric 原生 {@code Registry.register}（立即注册），方块与同名方块物品（{@link PHBlockItem}）一并注册。 */
public class PHBlockRegistry {


    // ===== 宠物床 pet_bed_*（16 色）：记录宠物重生点，宠物死亡后次日按床铺位置复活，床方块实体持有复活请求队列 =====

    public static final Block WHITE_PET_BED = registerBlockAndItem("pet_bed_white", () -> new PetBedBlock("white", DyeColor.WHITE));
    public static final Block ORANGE_PET_BED = registerBlockAndItem("pet_bed_orange", () -> new PetBedBlock("orange", DyeColor.ORANGE));
    public static final Block MAGENTA_PET_BED = registerBlockAndItem("pet_bed_magenta", () -> new PetBedBlock("magenta", DyeColor.MAGENTA));
    public static final Block LIGHT_BLUE_PET_BED = registerBlockAndItem("pet_bed_light_blue", () -> new PetBedBlock("light_blue", DyeColor.LIGHT_BLUE));
    public static final Block YELLOW_PET_BED = registerBlockAndItem("pet_bed_yellow", () -> new PetBedBlock("yellow", DyeColor.YELLOW));
    public static final Block LIME_PET_BED = registerBlockAndItem("pet_bed_lime", () -> new PetBedBlock("lime", DyeColor.LIME));
    public static final Block PINK_PET_BED = registerBlockAndItem("pet_bed_pink", () -> new PetBedBlock("pink", DyeColor.PINK));
    public static final Block GRAY_PET_BED = registerBlockAndItem("pet_bed_gray", () -> new PetBedBlock("gray", DyeColor.GRAY));
    public static final Block LIGHT_GRAY_PET_BED = registerBlockAndItem("pet_bed_light_gray", () -> new PetBedBlock("light_gray", DyeColor.LIGHT_GRAY));
    public static final Block CYAN_PET_BED = registerBlockAndItem("pet_bed_cyan", () -> new PetBedBlock("cyan", DyeColor.CYAN));
    public static final Block PURPLE_PET_BED = registerBlockAndItem("pet_bed_purple", () -> new PetBedBlock("purple", DyeColor.PURPLE));
    public static final Block BLUE_PET_BED = registerBlockAndItem("pet_bed_blue", () -> new PetBedBlock("blue", DyeColor.BLUE));
    public static final Block BROWN_PET_BED = registerBlockAndItem("pet_bed_brown", () -> new PetBedBlock("brown", DyeColor.BROWN));
    public static final Block GREEN_PET_BED = registerBlockAndItem("pet_bed_green", () -> new PetBedBlock("green", DyeColor.GREEN));
    public static final Block RED_PET_BED = registerBlockAndItem("pet_bed_red", () -> new PetBedBlock("red", DyeColor.RED));
    public static final Block BLACK_PET_BED = registerBlockAndItem("pet_bed_black", () -> new PetBedBlock("black", DyeColor.BLACK));

    /** 指令鼓：右键循环切换指令（游走/停留/跟随），对范围内自己的宠物批量生效，也支持红石触发。 */
    public static final Block DRUM = registerBlockAndItem("drum", () -> new DrumBlock());

    /** 往生灯笼：宠物因区块卸载离开世界时被收进灯笼，之后按指令召回。 */
    public static final Block WAYWARD_LANTERN = registerBlockAndItem("wayward_lantern", () -> new WaywardLanternBlock());


    /** 注册方块并同时注册其方块物品（无独立物品类，属性默认）。
     * @return 已注册的方块实例（供常量持有） */
    public static Block registerBlockAndItem(String name, Supplier<Block> block){
        Block blockObj = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(PetHomeMod.MODID, name), block.get());
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, name), new PHBlockItem(blockObj, new Item.Properties()));
        return blockObj;
    }

    public static void init() {
    }

    /** datagen 用：颜色名（与注册名后缀一致）→ 宠物床方块。 */
    public static java.util.Map<String, Block> petBedsByColor() {
        java.util.Map<String, Block> map = new java.util.LinkedHashMap<>();
        map.put("white", WHITE_PET_BED);
        map.put("orange", ORANGE_PET_BED);
        map.put("magenta", MAGENTA_PET_BED);
        map.put("light_blue", LIGHT_BLUE_PET_BED);
        map.put("yellow", YELLOW_PET_BED);
        map.put("lime", LIME_PET_BED);
        map.put("pink", PINK_PET_BED);
        map.put("gray", GRAY_PET_BED);
        map.put("light_gray", LIGHT_GRAY_PET_BED);
        map.put("cyan", CYAN_PET_BED);
        map.put("purple", PURPLE_PET_BED);
        map.put("blue", BLUE_PET_BED);
        map.put("brown", BROWN_PET_BED);
        map.put("green", GREEN_PET_BED);
        map.put("red", RED_PET_BED);
        map.put("black", BLACK_PET_BED);
        return map;
    }
}

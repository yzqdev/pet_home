package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.List;

public class PHItemRegistry {

    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(),
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name)));
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name), item);
    }

    public static final Item COLLAR_TAG = register("collar_tag", new CollarTagItem(props("collar_tag")));
    public static final Item FEATHER_ON_A_STICK = register("feather_on_a_stick", new FeatherOnAStickItem(props("feather_on_a_stick")));
    public static final Item ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem(props("rotten_apple")));
    public static final Item SINISTER_CARROT = register("sinister_carrot", new SinisterCarrotItem(props("sinister_carrot")));
    public static final Item DEFLECTION_SHIELD = register("deflection_shield", new InventoryOnlyItem(props("deflection_shield")));
    public static final Item MAGNET = register("magnet", new InventoryOnlyItem(props("magnet")));

    public static final Item DEED_OF_OWNERSHIP = register("deed_of_ownership", new DeedOfOwnershipItem(props("deed_of_ownership")));
    public static final Item PET_COMPASS = register("pet_compass", new PetCompassItem(props("pet_compass").stacksTo(1)));
    public static final Item NET_ITEM = register("net", new NetItem(Type.EMPTY, props("net")));
    public static final Item NET_HAS_ITEM = register("net_has_item", new NetItem(Type.HAS_MOB, props("net_has_item")));
    public static final Item NET_LAUNCHER_ITEM = register("net_launcher", new NetLauncherItem(props("net_launcher").durability(60).enchantable(14)));

    public static void init() {
    }

    /**
     * 本模组注册的全部物品（含以方块形式注册的宠物床 / 鼓），供数据生成遍历。
     * 直接从注册表按命名空间过滤，避免依赖各类静态字段的初始化顺序。
     */
    public static List<Item> allItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(PetHomeMod.MODID))
                .toList();
    }
}

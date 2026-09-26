package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** 物品注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHItemRegistry {

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, id(name), item);
    }

    public static final Item COLLAR_TAG = register("collar_tag", new CollarTagItem());
    public static final Item FEATHER_ON_A_STICK = register("feather_on_a_stick", new FeatherOnAStickItem());
    public static final Item ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem());
    public static final Item SINISTER_CARROT = register("sinister_carrot", new SinisterCarrotItem());
    public static final Item DEFLECTION_SHIELD = register("deflection_shield", new InventoryOnlyItem(new Item.Properties()));
    public static final Item MAGNET = register("magnet", new InventoryOnlyItem(new Item.Properties()));

    public static final Item DEED_OF_OWNERSHIP = register("deed_of_ownership", new DeedOfOwnershipItem());
    public static final Item PET_COMPASS = register("pet_compass", new PetCompassItem());
    public static final Item NET_ITEM = register("net", new NetItem(Type.EMPTY));
    public static final Item NET_HAS_ITEM = register("net_has_item", new NetItem(Type.HAS_MOB));
    public static final Item NET_LAUNCHER_ITEM = register("net_launcher", new NetLauncherItem(new Item.Properties().durability(60)));

    public static void init() {
    }
}

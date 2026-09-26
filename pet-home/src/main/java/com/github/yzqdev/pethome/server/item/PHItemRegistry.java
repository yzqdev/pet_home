package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class PHItemRegistry {


    public static final Item COLLAR_TAG = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "collar_tag"), new CollarTagItem());
    public static final Item FEATHER_ON_A_STICK = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "feather_on_a_stick"), new FeatherOnAStickItem());
    public static final Item ROTTEN_APPLE = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "rotten_apple"), new RottenAppleItem());
    public static final Item SINISTER_CARROT = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "sinister_carrot"), new SinisterCarrotItem());
    public static final Item DEFLECTION_SHIELD = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "deflection_shield"), new InventoryOnlyItem(new Item.Properties()));
    public static final Item MAGNET = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "magnet"), new InventoryOnlyItem(new Item.Properties()));
    public static final Item DEED_OF_OWNERSHIP = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "deed_of_ownership"), new DeedOfOwnershipItem());
    public static final Item PET_COMPASS = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "pet_compass"), new PetCompassItem());
    public static Item NET_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net"), new NetItem(Type.EMPTY));
    public static Item NET_HAS_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net_has_item"), new NetItem(Type.HAS_MOB));
    public static Item NET_LAUNCHER_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net_launcher"), new NetLauncherItem(new Item.Properties().durability(60)));



    public static void init() {
    }
}

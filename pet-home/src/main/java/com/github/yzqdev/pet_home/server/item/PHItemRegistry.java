package com.github.yzqdev.pet_home.server.item;

 
import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PHItemRegistry {

    public static final DeferredRegister.Items DEF_REG = DeferredRegister.createItems(  PetHomeMod.MODID);

    public static final DeferredItem<Item> COLLAR_TAG = DEF_REG.register("collar_tag", () -> new CollarTagItem());
    public static final DeferredItem<Item> FEATHER_ON_A_STICK = DEF_REG.register("feather_on_a_stick", () -> new FeatherOnAStickItem());
    public static final DeferredItem<Item> ROTTEN_APPLE = DEF_REG.register("rotten_apple", () -> new RottenAppleItem());
    public static final DeferredItem<Item> SINISTER_CARROT = DEF_REG.register("sinister_carrot", () -> new SinisterCarrotItem());
    public static final DeferredItem<Item> DEFLECTION_SHIELD = DEF_REG.register("deflection_shield", () -> new InventoryOnlyItem(new Item.Properties()));
    public static final DeferredItem<Item> MAGNET = DEF_REG.register("magnet", () -> new InventoryOnlyItem(new Item.Properties()));

    public static final DeferredItem<Item> DEED_OF_OWNERSHIP = DEF_REG.register("deed_of_ownership", () ->  new DeedOfOwnershipItem());
    public static DeferredItem<Item> NET_ITEM = DEF_REG.register("net", () -> new NetItem(Type.EMPTY));
    public static DeferredItem<Item> NET_HAS_ITEM = DEF_REG.register("net_has_item", () -> new NetItem(Type.HAS_MOB));
    public static DeferredItem<Item> NET_LAUNCHER_ITEM = DEF_REG.register("net_launcher", () -> new NetLauncherItem(new Item.Properties().durability(60)));
}

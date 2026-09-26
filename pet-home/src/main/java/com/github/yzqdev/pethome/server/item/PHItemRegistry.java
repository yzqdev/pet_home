package com.github.yzqdev.pethome.server.item;


import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PHItemRegistry {

    public static final DeferredRegister.Items DEF_REG = DeferredRegister.createItems(PetHomeMod.MODID);

    // 26.1: Item 的 Properties 必须携带注册 id，registerItem 会自动 setId
    public static final DeferredItem<Item> COLLAR_TAG = DEF_REG.registerItem("collar_tag", CollarTagItem::new, Item.Properties::new);
    public static final DeferredItem<Item> FEATHER_ON_A_STICK = DEF_REG.registerItem("feather_on_a_stick", FeatherOnAStickItem::new, Item.Properties::new);
    public static final DeferredItem<Item> ROTTEN_APPLE = DEF_REG.registerItem("rotten_apple", RottenAppleItem::new, Item.Properties::new);
    public static final DeferredItem<Item> SINISTER_CARROT = DEF_REG.registerItem("sinister_carrot", SinisterCarrotItem::new, Item.Properties::new);
    public static final DeferredItem<Item> DEFLECTION_SHIELD = DEF_REG.registerItem("deflection_shield", InventoryOnlyItem::new, Item.Properties::new);
    public static final DeferredItem<Item> MAGNET = DEF_REG.registerItem("magnet", InventoryOnlyItem::new, Item.Properties::new);

    public static final DeferredItem<Item> DEED_OF_OWNERSHIP = DEF_REG.registerItem("deed_of_ownership", DeedOfOwnershipItem::new, Item.Properties::new);
    public static final DeferredItem<Item> PET_COMPASS = DEF_REG.registerItem("pet_compass", PetCompassItem::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> NET_ITEM = DEF_REG.registerItem("net", props -> new NetItem(Type.EMPTY, props), Item.Properties::new);
    public static final DeferredItem<Item> NET_HAS_ITEM = DEF_REG.registerItem("net_has_item", props -> new NetItem(Type.HAS_MOB, props), Item.Properties::new);
    public static final DeferredItem<Item> NET_LAUNCHER_ITEM = DEF_REG.registerItem("net_launcher", NetLauncherItem::new, () -> new Item.Properties().durability(60).enchantable(14));
}

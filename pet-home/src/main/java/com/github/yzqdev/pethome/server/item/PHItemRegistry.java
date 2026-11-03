package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * 物品注册表：项圈标签、羽毛杖、烂苹果、阴森胡萝卜、所有权契约、宠物罗盘、
 * 捕网 / 捕网发射器，以及两个仅展示用的 {@code InventoryOnlyItem}。
 * 宠物床与指令鼓的物品在 {@code PHBlockRegistry} 随方块注册（{@code BlockItem}）。
 * {@code init()} 仅用于触发类加载完成注册。
 */
public class PHItemRegistry {

    /**
     * 26.3 起物品必须显式指定注册键：为每个物品生成 {@code pet_home:<name>} 的
     * {@code ResourceKey} 供 {@code Item.Properties#setId} 使用。
     */
    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(),
                Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name)));
    }

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name), item);
    }

    /** 项圈标签：附魔的真正载体——右键宠物把附魔从物品转移到宠物本体。 */
    public static final Item COLLAR_TAG = register("collar_tag", new CollarTagItem(props("collar_tag")));
    /** 羽毛杖：鹦鹉的指挥道具。 */
    public static final Item FEATHER_ON_A_STICK = register("feather_on_a_stick", new FeatherOnAStickItem(props("feather_on_a_stick")));
    /** 烂苹果：苹果掉落物到期概率转化而来，把马转化为僵尸马。 */
    public static final Item ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem(props("rotten_apple")));
    /** 阴森胡萝卜：自家兔子转为邪恶兔、僵尸马转为骷髅马。 */
    public static final Item SINISTER_CARROT = register("sinister_carrot", new SinisterCarrotItem(props("sinister_carrot")));
    /** 仅物品栏展示用："偏折"附魔护盾图标。 */
    public static final Item DEFLECTION_SHIELD = register("deflection_shield", new InventoryOnlyItem(props("deflection_shield")));
    /** 仅物品栏展示用："磁力"附魔图标。 */
    public static final Item MAGNET = register("magnet", new InventoryOnlyItem(props("magnet")));

    /** 所有权契约：右键宠物解除驯服关系（清附魔、解绑宠物床）。 */
    public static final Item DEED_OF_OWNERSHIP = register("deed_of_ownership", new DeedOfOwnershipItem(props("deed_of_ownership")));
    /** 宠物罗盘：定位 / 召回宠物，不可堆叠（绑定单一 PetId）。 */
    public static final Item PET_COMPASS = register("pet_compass", new PetCompassItem(props("pet_compass").stacksTo(1)));
    /** 空捕网（Type.EMPTY）：右键生物抓捕。 */
    public static final Item NET_ITEM = register("net", new NetItem(Type.EMPTY, props("net")));
    /** 已装生物的捕网（Type.HAS_MOB）：生物 NBT 存在 ENTITY_HOLDER 组件里，不进创造物品栏。 */
    public static final Item NET_HAS_ITEM = register("net_has_item", new NetItem(Type.HAS_MOB, props("net_has_item")));
    /** 捕网发射器：远程发射捕网，耐久 60、可附魔。 */
    public static final Item NET_LAUNCHER_ITEM = register("net_launcher", new NetLauncherItem(props("net_launcher").durability(60).enchantable(14)));
    /** 宠物哨：右键召回全部宠物（罗盘档案里的存活宠物），100 tick 冷却。 */
    public static final Item WHISTLE = register("whistle", new WhistleItem(props("whistle").stacksTo(1)));
    /** 自由之铃：对自家宠物使用放生（服务端 10s 二次确认，见 PlayerInteractEntityHandler#handleFreedomBell）。 */
    public static final Item FREEDOM_BELL = register("freedom_bell", new FreedomBellItem(props("freedom_bell").stacksTo(1)));

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

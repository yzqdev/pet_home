package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** 物品注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHItemRegistry {


    /** 项圈标签：附魔的物品载体，右键自家宠物把书中附魔整体转移到宠物本体（附魔真正的载体是宠物）。 */
    public static final Item COLLAR_TAG = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "collar_tag"), new CollarTagItem());
    /** 羽毛钓竿：抛出羽毛把最近的宠物拉到身边（驯兽师村民出售）。 */
    public static final Item FEATHER_ON_A_STICK = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "feather_on_a_stick"), new FeatherOnAStickItem());
    /** 烂苹果：对马使用把马转换为僵尸马；苹果掉落物过期有概率转化而来。 */
    public static final Item ROTTEN_APPLE = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "rotten_apple"), new RottenAppleItem());
    /** 阴森胡萝卜：对自家兔子使用转为邪恶兔；对僵尸马使用转为骷髅马。 */
    public static final Item SINISTER_CARROT = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "sinister_carrot"), new SinisterCarrotItem());
    /** 偏移结界附魔的展示物品：仅存档/展示用，不进入创造物品栏。 */
    public static final Item DEFLECTION_SHIELD = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "deflection_shield"), new InventoryOnlyItem(new Item.Properties()));
    /** 磁力附魔的展示物品：仅存档/展示用，不进入创造物品栏。 */
    public static final Item MAGNET = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "magnet"), new InventoryOnlyItem(new Item.Properties()));
    /** 所有权契约：两段式转让宠物所有权（先对自家宠物绑定，再对同一只宠物右键转交）。 */
    public static final Item DEED_OF_OWNERSHIP = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "deed_of_ownership"), new DeedOfOwnershipItem());
    /** 宠物罗盘：指向指定宠物，支持档案管理与召回请求（服务端 PetCompassTracker 维护档案）。 */
    public static final Item WHISTLE = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "whistle"), new WhistleItem());

  /** 自由之铃：放生宠物的消耗品（10s 二次确认），72fa6ca3 新增。 */
  public static final Item FREEDOM_BELL = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "freedom_bell"), new FreedomBellItem());

  public static final Item PET_COMPASS = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "pet_compass"), new PetCompassItem());
    /** 空捕网：右键生物捕获（生物数据存入物品），对应 {@link Type#EMPTY}。 */
    public static Item NET_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net"), new NetItem(Type.EMPTY));
    /** 装有生物的捕网：右键释放被困生物，对应 {@link Type#HAS_MOB}。 */
    public static Item NET_HAS_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net_has_item"), new NetItem(Type.HAS_MOB));
    /** 捕网发射器：远距离发射捕网（60 耐久）。 */
    public static Item NET_LAUNCHER_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PetHomeMod.MODID, "net_launcher"), new NetLauncherItem(new Item.Properties().durability(60)));



    public static void init() {
    }
}

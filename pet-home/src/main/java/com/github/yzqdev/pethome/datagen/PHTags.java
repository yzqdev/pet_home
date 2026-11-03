package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 标签键常量：项圈标签可附魔物品、驯兽师工作点 POI、宠物商店笼/鱼缸实体、
 * 宠物床物品、可交易附魔书、宠物捕捉黑名单等（tag 内容由各 TagsProvider 生成）。
 */
public class PHTags {
    public static final TagKey<Item> COLLAR_TAG_tagkey = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag_key"));
    public static final TagKey<PoiType> animal_tamer_tag = TagKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer_tag_key"));
    public static final TagKey<EntityType<?>> petstore_cage_0 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_0"));
    public static final TagKey<EntityType<?>> petstore_cage_1 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_1"));
    public static final TagKey<EntityType<?>> petstore_cage_2 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_2"));
    public static final TagKey<EntityType<?>> petstore_cage_3 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_3"));
    public static final TagKey<EntityType<?>> petstore_fishtank = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_fishtank"));


    public static final TagKey<Item> PetBedKey = TagKey.create(BuiltInRegistries.ITEM.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_beds"));
    public static final TagKey<Enchantment> TradableEnchantmentKey = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "trade_enchantment_book"));
    public static final TagKey<Enchantment> INFUSE_EXTRA = TagKey.create(Registries.ENCHANTMENT, Identifier.parse("enchantinginfuser:infuse_extra"));
    /** EI 高级灌注器可用附魔标签（26.1 运行时 EI 读取 in_advanced_enchanting_infuser，infuse_extra 已废弃）。 */
    public static final TagKey<Enchantment> InfuserAdvancedKey = TagKey.create(Registries.ENCHANTMENT, Identifier.parse("enchantinginfuser:in_advanced_enchanting_infuser"));
    /** pet-home 全量附魔标签（含宝藏/诅咒），供 enchanting-infuser 高级灌注器引用。 */
    public static final TagKey<Enchantment> AllPetEnchantmentKey = TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_home_enchantments"));

    public static TagKey<EntityType<?>> infamy_target_attracted = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "infamy_target_attracted"));
    public static final TagKey<EntityType<?>> blacklisted = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "blacklisted"));
}

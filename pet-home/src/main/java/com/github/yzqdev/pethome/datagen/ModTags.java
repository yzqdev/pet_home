package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModTags {
    public static final TagKey<Item> COLLAR_TAG_tagkey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag_key"));
    public static final TagKey<PoiType> animal_tamer_tag = TagKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer_tag_key"));
    public static final TagKey<EntityType<?>> petstore_cage_0 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_0"));
    public static final TagKey<EntityType<?>> petstore_cage_1 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_1"));
    public static final TagKey<EntityType<?>> petstore_cage_2 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_2"));
    public static final TagKey<EntityType<?>> petstore_cage_3 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_3"));
    public static final TagKey<EntityType<?>> petstore_fishtank = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_fishtank"));


    public static final TagKey<Item> PetBedKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_beds"));
    public static final TagKey<Enchantment> TradableEnchantmentKey = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "trade_enchantment_book"));
    public static final TagKey<Enchantment> INFUSE_EXTRA = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("enchantinginfuser:infuse_extra"));
    /** EI 高级灌注器可用附魔标签（infuse_extra 已废弃，运行时 EI 读 in_advanced_enchanting_infuser）。 */
    public static final TagKey<Enchantment> InfuserAdvancedKey = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("enchantinginfuser:in_advanced_enchanting_infuser"));
    /** pet-home 全量附魔标签（含宝藏/诅咒），供 enchanting-infuser 高级灌注器引用。 */
    public static final TagKey<Enchantment> AllPetEnchantmentKey = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_home_enchantments"));

    public static TagKey<EntityType<?>> infamy_target_attracted = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "infamy_target_attracted"));
    public static final TagKey<EntityType<?>> blacklisted = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "blacklisted"));
}

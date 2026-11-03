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

public class PHTags {
    public static final TagKey<Item> COLLAR_TAG_tagkey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag_key"));
    public static final TagKey<PoiType> animal_tamer_tag = TagKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer_tag_key"));
    public static final TagKey<EntityType<?>> petstore_cage_0 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_0"));
    public static final TagKey<EntityType<?>> petstore_cage_1 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_1"));
    public static final TagKey<EntityType<?>> petstore_cage_2 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_2"));
    public static final TagKey<EntityType<?>> petstore_cage_3 = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_3"));
    public static final TagKey<EntityType<?>> petstore_fishtank = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_fishtank"));


    public static final TagKey<Item> PetBedKey = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_beds"));
    public static final TagKey<Enchantment> TradableEnchantmentKey = TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "trade_enchantment_book"));


    public static TagKey<EntityType<?>> infamy_target_attracted= TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "infamy_target_attracted"));

    /** 不能装备附魔项圈标签的生物黑名单（默认为空，供 datapack 扩展）。 */
    public static final TagKey<EntityType<?>> refusesCollarTags = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "refuses_collar_tags"));
    /** 不能睡宠物床的生物黑名单（默认为空，供 datapack 扩展）。 */
    public static final TagKey<EntityType<?>> refusesPetBeds = TagKey.create(BuiltInRegistries.ENTITY_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "refuses_pet_beds"));
    /** 驯服青蛙用的食物。 */
    public static final TagKey<Item> tameFrogsWith = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "tame_frogs_with"));
}

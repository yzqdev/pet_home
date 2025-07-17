package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Arrays;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> AMPHIBIOUS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "amphibious"));
    public static final ResourceKey<Enchantment> BLIGHT_CURSE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "blight_curse"));
    public static final ResourceKey<Enchantment> BUBBLING = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bubbling"));
    public static final ResourceKey<Enchantment> CHAIN_LIGHTNING = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chain_lightning"));
    public static final ResourceKey<Enchantment> HEALTH_BOOST = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "health_boost"));
    public static final ResourceKey<Enchantment> IMMUNITY_FRAME = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "immunity_frame"));
    public static final ResourceKey<Enchantment> FIREPROOF = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "fireproof"));
    public static final ResourceKey<Enchantment> DEFLECTION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "deflection"));
    public static final ResourceKey<Enchantment> SonicBoom = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "sonic_boom"));
    public static final ResourceKey<Enchantment> SPEEDSTER = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "speedster"));
    public static final ResourceKey<Enchantment> XP_Transfer = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "xp_transfer"));
    public static final ResourceKey<Enchantment> HEALING_AURA = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "healing_aura"));
    public static final ResourceKey<Enchantment> HEALTH_SIPHON = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "health_siphon"));
    public static final ResourceKey<Enchantment> REJUVENATION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "rejuvenation"));
    public static final ResourceKey<Enchantment> VAMPIRE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "vampire"));
    public static final ResourceKey<Enchantment> LINKED_INVENTORY = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "linked_inventory"));
    public static final ResourceKey<Enchantment> PSYCHIC_WALL = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "psychic_wall"));
    public static final ResourceKey<Enchantment> SHEPHERD = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "herding"));
    public static final ResourceKey<Enchantment> MAGNETIC = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "magnetic"));
    public static final ResourceKey<Enchantment> INFAMY_CURSE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "infamy_curse"));
    public static final ResourceKey<Enchantment> GLUTTONOUS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "gluttonous"));
    public static final ResourceKey<Enchantment> INTIMIDATION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "intimidation"));
    public static final ResourceKey<Enchantment> ORE_SCENTING = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "ore_scenting"));
    public static final ResourceKey<Enchantment> POISON_RESISTANCE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "poison_resistance"));
    public static final ResourceKey<Enchantment> FROST_FANG = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "frost_fang"));
    public static final ResourceKey<Enchantment> WARPING_BITE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "warping_bite"));
    public static final ResourceKey<Enchantment> SHADOW_HANDS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "shadow_hands"));
    public static final ResourceKey<Enchantment> TETHERED_TELEPORT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "tethered_teleport"));
    public static final ResourceKey<Enchantment> IMMATURITY_CURSE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "immaturity_curse"));
    public static final ResourceKey<Enchantment> BLAZING_PROTECTION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "blazing_protection"));
    public static final ResourceKey<Enchantment> TOTAL_RECALL = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "total_recall"));
    public static final ResourceKey<Enchantment> DEFUSAL = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "defusal"));
    public static final ResourceKey<Enchantment> VOID_CLOUD = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "void_cloud"));
    public static final ResourceKey<Enchantment> INSIGHT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "insight"));
    public static final ResourceKey<Enchantment> CHAOS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "chaos"));
    public static final ResourceKey<Enchantment> SHARE = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "share"));
    public static final ResourceKey<Enchantment> NIGHT_VISION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "night_vision"));
    public static final ResourceKey<Enchantment> PARALYSIS = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "paralysis"));
    public static final ResourceKey<Enchantment> TOUGH = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "tough"));  public static final ResourceKey<Enchantment> VIOLENT = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "violent"));


    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);
        register(context, CHAIN_LIGHTNING, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                        5,
                        2,
                        Enchantment.dynamicCost(8, 7),
                        Enchantment.dynamicCost(25, 7),
                        2))
                .exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.FROST_FANG), enchantments.getOrThrow(ModEnchantments.BUBBLING), enchantments.getOrThrow(ModEnchantments.MAGNETIC), enchantments.getOrThrow(ModEnchantments.SHADOW_HANDS)))
        );
        register(context, TOTAL_RECALL, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, REJUVENATION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, HEALING_AURA, HEALTH_SIPHON, XP_Transfer))
        );
        register(context, DEFUSAL, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(14, 7),
                2)).exclusiveWith(checkCompatible(enchantments, DEFLECTION))
        );
        register(context, WARPING_BITE, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                        5,
                        1,
                        Enchantment.dynamicCost(7, 7),
                        Enchantment.dynamicCost(25, 7),
                        2))
                .exclusiveWith(checkCompatible(enchantments, FROST_FANG, BUBBLING))
        );
        register(context, IMMUNITY_FRAME, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.DEFLECTION), enchantments.getOrThrow(ModEnchantments.BLAZING_PROTECTION)))
        );
        register(context, HEALTH_BOOST, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.HEALTH_SIPHON)))
        );
        register(context, FIREPROOF, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.POISON_RESISTANCE), enchantments.getOrThrow(ModEnchantments.FROST_FANG), enchantments.getOrThrow(ModEnchantments.AMPHIBIOUS)))
        );
        register(context, DEFLECTION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.IMMUNITY_FRAME), enchantments.getOrThrow(ModEnchantments.DEFUSAL), enchantments.getOrThrow(ModEnchantments.PSYCHIC_WALL), enchantments.getOrThrow(ModEnchantments.BLAZING_PROTECTION)))
        );
        register(context, AMPHIBIOUS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(6, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.FIREPROOF), enchantments.getOrThrow(ModEnchantments.BLAZING_PROTECTION)))
        );
        register(context, SPEEDSTER, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, XP_Transfer, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(3, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, REJUVENATION))
        );
        register(context, HEALING_AURA, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                2,
                Enchantment.dynamicCost(12, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, VAMPIRE, HEALTH_SIPHON, REJUVENATION))
        );
        register(context, HEALTH_SIPHON, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(12, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.HEALTH_BOOST), enchantments.getOrThrow(ModEnchantments.VAMPIRE), enchantments.getOrThrow(ModEnchantments.GLUTTONOUS), enchantments.getOrThrow(ModEnchantments.HEALING_AURA)))
        );
        register(context, VAMPIRE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                2,
                Enchantment.dynamicCost(10, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, MAGNETIC, HEALTH_SIPHON, GLUTTONOUS, HEALING_AURA))
        );
        register(context, BUBBLING, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                2,
                Enchantment.dynamicCost(10, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.CHAIN_LIGHTNING), enchantments.getOrThrow(ModEnchantments.FROST_FANG), enchantments.getOrThrow(ModEnchantments.SHADOW_HANDS), enchantments.getOrThrow(ModEnchantments.WARPING_BITE)))
        );
        register(context, LINKED_INVENTORY, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, PSYCHIC_WALL, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(9, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, MAGNETIC, DEFLECTION, SHEPHERD))
        );
        register(context, SHEPHERD, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                2,
                Enchantment.dynamicCost(5, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.MAGNETIC), enchantments.getOrThrow(ModEnchantments.ORE_SCENTING), enchantments.getOrThrow(ModEnchantments.PSYCHIC_WALL), enchantments.getOrThrow(ModEnchantments.BLIGHT_CURSE)))
        );
        register(context, MAGNETIC, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(11, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.CHAIN_LIGHTNING), enchantments.getOrThrow(ModEnchantments.SHEPHERD), enchantments.getOrThrow(ModEnchantments.VAMPIRE), enchantments.getOrThrow(ModEnchantments.SHADOW_HANDS), enchantments.getOrThrow(ModEnchantments.PSYCHIC_WALL), enchantments.getOrThrow(ModEnchantments.INTIMIDATION)))
        );
        register(context, INFAMY_CURSE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(11, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, GLUTTONOUS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, VAMPIRE, HEALTH_SIPHON))
        );
        register(context, INTIMIDATION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                2,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, MAGNETIC, WARPING_BITE))
        );
        register(context, ORE_SCENTING, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(9, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, SHEPHERD))
        );
        register(context, POISON_RESISTANCE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.FIREPROOF)))
        );
        register(context, SHADOW_HANDS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                4,
                Enchantment.dynamicCost(8, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, CHAIN_LIGHTNING, MAGNETIC, BUBBLING, BLAZING_PROTECTION))
        );
        register(context, FROST_FANG, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(7, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(HolderSet.direct(enchantments.getOrThrow(ModEnchantments.CHAIN_LIGHTNING), enchantments.getOrThrow(ModEnchantments.FIREPROOF), enchantments.getOrThrow(ModEnchantments.WARPING_BITE), enchantments.getOrThrow(ModEnchantments.BLAZING_PROTECTION)))
        );
        register(context, TETHERED_TELEPORT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, SonicBoom, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, MAGNETIC, WARPING_BITE))
        );
        register(context, IMMATURITY_CURSE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, BLAZING_PROTECTION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, IMMUNITY_FRAME, DEFLECTION, AMPHIBIOUS, FROST_FANG, SHADOW_HANDS))
        );
        register(context, BLIGHT_CURSE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, SHEPHERD))

        );
        register(context, VOID_CLOUD, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))

        );
        register(context, INSIGHT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))

        );
        register(context, CHAOS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, PARALYSIS))

        );
        register(context, SHARE, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))

        );
        register(context, NIGHT_VISION, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );
        register(context, PARALYSIS, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                3,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2)).exclusiveWith(checkCompatible(enchantments, CHAOS))
        );
        register(context, TOUGH, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                4,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );        register(context, VIOLENT, Enchantment.enchantment(Enchantment.definition(
                items.getOrThrow(ModTags.COLLAR_TAG_tagkey),
                5,
                1,
                Enchantment.dynamicCost(4, 7),
                Enchantment.dynamicCost(25, 7),
                2))
        );

    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key,
                                 Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }

    @SafeVarargs
    private static HolderSet<Enchantment> checkCompatible(HolderGetter<Enchantment> enchantGetter, ResourceKey<Enchantment>... enchants) {

        return HolderSet.direct(Arrays.stream(enchants).map(enchantGetter::getOrThrow).toList());
    }
}

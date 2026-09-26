package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEnchantTagProvider extends EnchantmentTagsProvider {
    public ModEnchantTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PetHomeMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.TREASURE).addOptional(ModEnchantments.BUBBLING)
                .addOptional(ModEnchantments.VAMPIRE)
                .addOptional(ModEnchantments.BLAZING_PROTECTION)
                .addOptional(ModEnchantments.ORE_SCENTING)
                .addOptional(ModEnchantments.SonicBoom)
                .addOptional(ModEnchantments.SHARE)
                .addOptional(ModEnchantments.PARALYSIS)
                .replace(false)
        ;
        tag(EnchantmentTags.CURSE).addOptional(ModEnchantments.BLIGHT_CURSE).addOptional(ModEnchantments.INFAMY_CURSE).addOptional(ModEnchantments.IMMATURITY_CURSE).replace(false);
        this.tag(ModTags.TradableEnchantmentKey)
                .addOptional(ModEnchantments.AMPHIBIOUS)
                .addOptional(ModEnchantments.HEALING_AURA)
                .addOptional(ModEnchantments.CHAIN_LIGHTNING)
                .addOptional(ModEnchantments.XP_Transfer)
                .addOptional(ModEnchantments.LINKED_INVENTORY)
                .addOptional(ModEnchantments.HEALTH_BOOST)
                .addOptional(ModEnchantments.IMMUNITY_FRAME)
                .addOptional(ModEnchantments.DEFLECTION)
                .addOptional(ModEnchantments.FIREPROOF)
                .addOptional(ModEnchantments.IMMATURITY_CURSE)
                .addOptional(ModEnchantments.DEFUSAL)
                .addOptional(ModEnchantments.TOTAL_RECALL)
                .addOptional(ModEnchantments.SPEEDSTER)
                .addOptional(ModEnchantments.HEALTH_SIPHON)
                .addOptional(ModEnchantments.PSYCHIC_WALL)
                .addOptional(ModEnchantments.SHEPHERD)
                .addOptional(ModEnchantments.MAGNETIC)
                .addOptional(ModEnchantments.INFAMY_CURSE)
                .addOptional(ModEnchantments.GLUTTONOUS)
                .addOptional(ModEnchantments.INTIMIDATION)
                .addOptional(ModEnchantments.POISON_RESISTANCE)
                .addOptional(ModEnchantments.FROST_FANG)
                .addOptional(ModEnchantments.WARPING_BITE)
                .addOptional(ModEnchantments.SHADOW_HANDS)
                .addOptional(ModEnchantments.TETHERED_TELEPORT)
                .addOptional(ModEnchantments.REJUVENATION)
                .addOptional(ModEnchantments.BLIGHT_CURSE)
                .addOptional(ModEnchantments.SonicBoom)
                .addOptional(ModEnchantments.VOID_CLOUD)
                .addOptional(ModEnchantments.INSIGHT)
                .addOptional(ModEnchantments.CHAOS)
                .addOptional(ModEnchantments.NIGHT_VISION)
                .addOptional(ModEnchantments.VIOLENT)


        ;
        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addOptional(ModEnchantments.AMPHIBIOUS)
                .addOptional(ModEnchantments.CHAIN_LIGHTNING)
                .addOptional(ModEnchantments.DEFLECTION)
                .addOptional(ModEnchantments.FIREPROOF)
                .addOptional(ModEnchantments.FROST_FANG)
                .addOptional(ModEnchantments.GLUTTONOUS)
                .addOptional(ModEnchantments.HEALTH_SIPHON)
                .addOptional(ModEnchantments.HEALTH_BOOST)
                .addOptional(ModEnchantments.HEALING_AURA)
                .addOptional(ModEnchantments.INFAMY_CURSE)
                .addOptional(ModEnchantments.INTIMIDATION)
                .addOptional(ModEnchantments.IMMUNITY_FRAME)
                .addOptional(ModEnchantments.LINKED_INVENTORY)
                .addOptional(ModEnchantments.MAGNETIC)
                .addOptional(ModEnchantments.PSYCHIC_WALL)
                .addOptional(ModEnchantments.SHEPHERD)
                .addOptional(ModEnchantments.SPEEDSTER)
                .addOptional(ModEnchantments.POISON_RESISTANCE)
                .addOptional(ModEnchantments.WARPING_BITE)
                .addOptional(ModEnchantments.TETHERED_TELEPORT)
                .addOptional(ModEnchantments.XP_Transfer)
                .addOptional(ModEnchantments.BLIGHT_CURSE)
                .addOptional(ModEnchantments.DEFUSAL)
                .addOptional(ModEnchantments.TOTAL_RECALL)
                .addOptional(ModEnchantments.REJUVENATION)
                .addOptional(ModEnchantments.VOID_CLOUD)

                .addOptional(ModEnchantments.INSIGHT)

                .replace(false)

        ;
        this.tag(ModTags.INFUSE_EXTRA).addTag(ModTags.TradableEnchantmentKey);
    }

    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

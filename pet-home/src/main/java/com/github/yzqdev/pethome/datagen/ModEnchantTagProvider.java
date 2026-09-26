package com.github.yzqdev.pethome.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ModEnchantTagProvider extends FabricTagProvider.EnchantmentTagProvider {

    public ModEnchantTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        getOrCreateTagBuilder(EnchantmentTags.TREASURE).addOptional(ModEnchantments.BUBBLING.location())
                .addOptional(ModEnchantments.VAMPIRE.location())
                .addOptional(ModEnchantments.BLAZING_PROTECTION.location())
                .addOptional(ModEnchantments.ORE_SCENTING.location())
                .addOptional(ModEnchantments.SonicBoom.location())
                .addOptional(ModEnchantments.SHARE.location())
                .addOptional(ModEnchantments.PARALYSIS.location())
        ;
        getOrCreateTagBuilder(EnchantmentTags.CURSE).addOptional(ModEnchantments.BLIGHT_CURSE.location()).addOptional(ModEnchantments.INFAMY_CURSE.location()).addOptional(ModEnchantments.IMMATURITY_CURSE.location());
        getOrCreateTagBuilder(ModTags.TradableEnchantmentKey)
                .addOptional(ModEnchantments.AMPHIBIOUS.location())
                .addOptional(ModEnchantments.HEALING_AURA.location())
                .addOptional(ModEnchantments.CHAIN_LIGHTNING.location())
                .addOptional(ModEnchantments.XP_Transfer.location())
                .addOptional(ModEnchantments.LINKED_INVENTORY.location())
                .addOptional(ModEnchantments.HEALTH_BOOST.location())
                .addOptional(ModEnchantments.IMMUNITY_FRAME.location())
                .addOptional(ModEnchantments.DEFLECTION.location())
                .addOptional(ModEnchantments.FIREPROOF.location())
                .addOptional(ModEnchantments.IMMATURITY_CURSE.location())
                .addOptional(ModEnchantments.DEFUSAL.location())
                .addOptional(ModEnchantments.TOTAL_RECALL.location())
                .addOptional(ModEnchantments.SPEEDSTER.location())
                .addOptional(ModEnchantments.HEALTH_SIPHON.location())
                .addOptional(ModEnchantments.PSYCHIC_WALL.location())
                .addOptional(ModEnchantments.SHEPHERD.location())
                .addOptional(ModEnchantments.MAGNETIC.location())
                .addOptional(ModEnchantments.INFAMY_CURSE.location())
                .addOptional(ModEnchantments.GLUTTONOUS.location())
                .addOptional(ModEnchantments.INTIMIDATION.location())
                .addOptional(ModEnchantments.POISON_RESISTANCE.location())
                .addOptional(ModEnchantments.FROST_FANG.location())
                .addOptional(ModEnchantments.WARPING_BITE.location())
                .addOptional(ModEnchantments.SHADOW_HANDS.location())
                .addOptional(ModEnchantments.TETHERED_TELEPORT.location())
                .addOptional(ModEnchantments.REJUVENATION.location())
                .addOptional(ModEnchantments.BLIGHT_CURSE.location())
                .addOptional(ModEnchantments.SonicBoom.location())
                .addOptional(ModEnchantments.VOID_CLOUD.location())
                .addOptional(ModEnchantments.INSIGHT.location())
                .addOptional(ModEnchantments.CHAOS.location())
                .addOptional(ModEnchantments.NIGHT_VISION.location())
                .addOptional(ModEnchantments.VIOLENT.location())


        ;
        getOrCreateTagBuilder(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addOptional(ModEnchantments.AMPHIBIOUS.location())
                .addOptional(ModEnchantments.CHAIN_LIGHTNING.location())
                .addOptional(ModEnchantments.DEFLECTION.location())
                .addOptional(ModEnchantments.FIREPROOF.location())
                .addOptional(ModEnchantments.FROST_FANG.location())
                .addOptional(ModEnchantments.GLUTTONOUS.location())
                .addOptional(ModEnchantments.HEALTH_SIPHON.location())
                .addOptional(ModEnchantments.HEALTH_BOOST.location())
                .addOptional(ModEnchantments.HEALING_AURA.location())
                .addOptional(ModEnchantments.INFAMY_CURSE.location())
                .addOptional(ModEnchantments.INTIMIDATION.location())
                .addOptional(ModEnchantments.IMMUNITY_FRAME.location())
                .addOptional(ModEnchantments.LINKED_INVENTORY.location())
                .addOptional(ModEnchantments.MAGNETIC.location())
                .addOptional(ModEnchantments.PSYCHIC_WALL.location())
                .addOptional(ModEnchantments.SHEPHERD.location())
                .addOptional(ModEnchantments.SPEEDSTER.location())
                .addOptional(ModEnchantments.POISON_RESISTANCE.location())
                .addOptional(ModEnchantments.WARPING_BITE.location())
                .addOptional(ModEnchantments.TETHERED_TELEPORT.location())
                .addOptional(ModEnchantments.XP_Transfer.location())
                .addOptional(ModEnchantments.BLIGHT_CURSE.location())
                .addOptional(ModEnchantments.DEFUSAL.location())
                .addOptional(ModEnchantments.TOTAL_RECALL.location())
                .addOptional(ModEnchantments.REJUVENATION.location())
                .addOptional(ModEnchantments.VOID_CLOUD.location())

                .addOptional(ModEnchantments.INSIGHT.location())

        ;
        getOrCreateTagBuilder(ModTags.INFUSE_EXTRA).addTag(ModTags.TradableEnchantmentKey);
    }

    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

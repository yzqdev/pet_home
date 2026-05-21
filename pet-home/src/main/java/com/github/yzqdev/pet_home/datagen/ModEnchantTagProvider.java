package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEnchantTagProvider extends EnchantmentTagsProvider {
    public ModEnchantTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.NON_TREASURE)
                .add(ModEnchantments.BUBBLING.location())
                .add(ModEnchantments.VAMPIRE.location())
                .add(ModEnchantments.BLAZING_PROTECTION.location())
                .add(ModEnchantments.ORE_SCENTING.location())
                .add(ModEnchantments.SonicBoom.location())
                .add(ModEnchantments.SHARE.location())
                .add(ModEnchantments.PARALYSIS.location())
                .replace(false);

        tag(EnchantmentTags.CURSE)
                .add(ModEnchantments.BLIGHT_CURSE.location())
                .add(ModEnchantments.INFAMY_CURSE.location())
                .add(ModEnchantments.IMMATURITY_CURSE.location())
                .replace(false);

        this.tag(ModTags.TradableEnchantmentKey)
                .add(ModEnchantments.AMPHIBIOUS.location())
                .add(ModEnchantments.HEALING_AURA.location())
                .add(ModEnchantments.CHAIN_LIGHTNING.location())
                .add(ModEnchantments.XP_Transfer.location())
                .add(ModEnchantments.LINKED_INVENTORY.location())
                .add(ModEnchantments.HEALTH_BOOST.location())
                .add(ModEnchantments.IMMUNITY_FRAME.location())
                .add(ModEnchantments.DEFLECTION.location())
                .add(ModEnchantments.FIREPROOF.location())
                .add(ModEnchantments.IMMATURITY_CURSE.location())
                .add(ModEnchantments.DEFUSAL.location())
                .add(ModEnchantments.TOTAL_RECALL.location())
                .add(ModEnchantments.SPEEDSTER.location())
                .add(ModEnchantments.HEALTH_SIPHON.location())
                .add(ModEnchantments.PSYCHIC_WALL.location())
                .add(ModEnchantments.SHEPHERD.location())
                .add(ModEnchantments.MAGNETIC.location())
                .add(ModEnchantments.INFAMY_CURSE.location())
                .add(ModEnchantments.GLUTTONOUS.location())
                .add(ModEnchantments.INTIMIDATION.location())
                .add(ModEnchantments.POISON_RESISTANCE.location())
                .add(ModEnchantments.FROST_FANG.location())
                .add(ModEnchantments.WARPING_BITE.location())
                .add(ModEnchantments.SHADOW_HANDS.location())
                .add(ModEnchantments.TETHERED_TELEPORT.location())
                .add(ModEnchantments.REJUVENATION.location())
                .add(ModEnchantments.BLIGHT_CURSE.location())
                .add(ModEnchantments.SonicBoom.location())
                .add(ModEnchantments.VOID_CLOUD.location())
                .add(ModEnchantments.INSIGHT.location())
                .add(ModEnchantments.CHAOS.location())
                .add(ModEnchantments.NIGHT_VISION.location())
                .add(ModEnchantments.VIOLENT.location());

        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .add(ModEnchantments.AMPHIBIOUS.location())
                .add(ModEnchantments.CHAIN_LIGHTNING.location())
                .add(ModEnchantments.DEFLECTION.location())
                .add(ModEnchantments.FIREPROOF.location())
                .add(ModEnchantments.FROST_FANG.location())
                .add(ModEnchantments.GLUTTONOUS.location())
                .add(ModEnchantments.HEALTH_SIPHON.location())
                .add(ModEnchantments.HEALTH_BOOST.location())
                .add(ModEnchantments.HEALING_AURA.location())
                .add(ModEnchantments.INFAMY_CURSE.location())
                .add(ModEnchantments.INTIMIDATION.location())
                .add(ModEnchantments.IMMUNITY_FRAME.location())
                .add(ModEnchantments.LINKED_INVENTORY.location())
                .add(ModEnchantments.MAGNETIC.location())
                .add(ModEnchantments.PSYCHIC_WALL.location())
                .add(ModEnchantments.SHEPHERD.location())
                .add(ModEnchantments.SPEEDSTER.location())
                .add(ModEnchantments.POISON_RESISTANCE.location())
                .add(ModEnchantments.WARPING_BITE.location())
                .add(ModEnchantments.TETHERED_TELEPORT.location())
                .add(ModEnchantments.XP_Transfer.location())
                .add(ModEnchantments.BLIGHT_CURSE.location())
                .add(ModEnchantments.DEFUSAL.location())
                .add(ModEnchantments.TOTAL_RECALL.location())
                .add(ModEnchantments.REJUVENATION.location())
                .add(ModEnchantments.VOID_CLOUD.location())
                .add(ModEnchantments.INSIGHT.location())
                .replace(false);
        
        this.tag(ModTags.INFUSE_EXTRA).addTag(ModTags.TradableEnchantmentKey);
    }

    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

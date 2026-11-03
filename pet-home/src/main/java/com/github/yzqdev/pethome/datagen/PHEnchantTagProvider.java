package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PHEnchantTagProvider extends EnchantmentTagsProvider {
    public PHEnchantTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.TREASURE).addOptional(PHEnchantments.BUBBLING.location())
                .addOptional(PHEnchantments.VAMPIRE.location())
                .addOptional(PHEnchantments.BLAZING_PROTECTION.location())
                .addOptional(PHEnchantments.ORE_SCENTING.location())
                .addOptional(PHEnchantments.SonicBoom.location())
                .addOptional(PHEnchantments.SHARE.location())
                .addOptional(PHEnchantments.PARALYSIS.location())
                // muffled（自 1.20 移植的 LootOnly）：只能经战利品表获取
                .addOptional(PHEnchantments.MUFFLED.location())
                .replace(false)
        ;
        tag(EnchantmentTags.CURSE).addOptional(PHEnchantments.BLIGHT_CURSE.location()).addOptional(PHEnchantments.INFAMY_CURSE.location()).addOptional(PHEnchantments.IMMATURITY_CURSE.location()).replace(false);
        this.tag(PHTags.TradableEnchantmentKey)
                .addOptional(PHEnchantments.AMPHIBIOUS.location())
                .addOptional(PHEnchantments.HEALING_AURA.location())
                .addOptional(PHEnchantments.CHAIN_LIGHTNING.location())
                .addOptional(PHEnchantments.XP_Transfer.location())
                .addOptional(PHEnchantments.LINKED_INVENTORY.location())
                .addOptional(PHEnchantments.HEALTH_BOOST.location())
                .addOptional(PHEnchantments.IMMUNITY_FRAME.location())
                .addOptional(PHEnchantments.DEFLECTION.location())
                .addOptional(PHEnchantments.FIREPROOF.location())
                .addOptional(PHEnchantments.IMMATURITY_CURSE.location())
                .addOptional(PHEnchantments.DEFUSAL.location())
                .addOptional(PHEnchantments.TOTAL_RECALL.location())
                .addOptional(PHEnchantments.SPEEDSTER.location())
                .addOptional(PHEnchantments.HEALTH_SIPHON.location())
                .addOptional(PHEnchantments.PSYCHIC_WALL.location())
                .addOptional(PHEnchantments.SHEPHERD.location())
                .addOptional(PHEnchantments.MAGNETIC.location())
                .addOptional(PHEnchantments.INFAMY_CURSE.location())
                .addOptional(PHEnchantments.GLUTTONOUS.location())
                .addOptional(PHEnchantments.INTIMIDATION.location())
                .addOptional(PHEnchantments.POISON_RESISTANCE.location())
                .addOptional(PHEnchantments.FROST_FANG.location())
                .addOptional(PHEnchantments.WARPING_BITE.location())
                .addOptional(PHEnchantments.SHADOW_HANDS.location())
                .addOptional(PHEnchantments.TETHERED_TELEPORT.location())
                .addOptional(PHEnchantments.REJUVENATION.location())
                .addOptional(PHEnchantments.BLIGHT_CURSE.location())
                .addOptional(PHEnchantments.SonicBoom.location())
                .addOptional(PHEnchantments.INSIGHT.location())
                .addOptional(PHEnchantments.CHAOS.location())
                .addOptional(PHEnchantments.NIGHT_VISION.location())
                .addOptional(PHEnchantments.VIOLENT.location())
                // 自 1.20 移植：charisma 只能驯兽师交易（不进附魔台）；disc_jockey 为普通附魔（附魔台/交易均可）
                .addOptional(PHEnchantments.CHARISMA.location())
                .addOptional(PHEnchantments.DISC_JOCKEY.location())


        ;
        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addOptional(PHEnchantments.AMPHIBIOUS.location())
                .addOptional(PHEnchantments.CHAIN_LIGHTNING.location())
                .addOptional(PHEnchantments.DEFLECTION.location())
                .addOptional(PHEnchantments.FIREPROOF.location())
                .addOptional(PHEnchantments.FROST_FANG.location())
                .addOptional(PHEnchantments.GLUTTONOUS.location())
                .addOptional(PHEnchantments.HEALTH_SIPHON.location())
                .addOptional(PHEnchantments.HEALTH_BOOST.location())
                .addOptional(PHEnchantments.HEALING_AURA.location())
                .addOptional(PHEnchantments.INFAMY_CURSE.location())
                .addOptional(PHEnchantments.INTIMIDATION.location())
                .addOptional(PHEnchantments.IMMUNITY_FRAME.location())
                .addOptional(PHEnchantments.LINKED_INVENTORY.location())
                .addOptional(PHEnchantments.MAGNETIC.location())
                .addOptional(PHEnchantments.PSYCHIC_WALL.location())
                .addOptional(PHEnchantments.SHEPHERD.location())
                .addOptional(PHEnchantments.SPEEDSTER.location())
                .addOptional(PHEnchantments.POISON_RESISTANCE.location())
                .addOptional(PHEnchantments.WARPING_BITE.location())
                .addOptional(PHEnchantments.TETHERED_TELEPORT.location())
                .addOptional(PHEnchantments.XP_Transfer.location())
                .addOptional(PHEnchantments.BLIGHT_CURSE.location())
                .addOptional(PHEnchantments.DEFUSAL.location())
                .addOptional(PHEnchantments.TOTAL_RECALL.location())
                .addOptional(PHEnchantments.REJUVENATION.location())

                .addOptional(PHEnchantments.INSIGHT.location())
                // 自 1.20 移植：disc_jockey 普通附魔台可得；charisma 仅驯兽师交易、muffled 宝藏限定，二者不进附魔台
                .addOptional(PHEnchantments.DISC_JOCKEY.location())

                .replace(false)

        ;
        this.tag(PHTags.InfuserAdvancedKey).addTag(PHTags.AllPetEnchantmentKey);

        // 本模组全部附魔（含宝藏/诅咒）聚合标签：EI 高级灌注器经 InfuserAdvancedKey 引用，
        // 宝藏附魔由此获得灌注器获取渠道
        this.tag(PHTags.AllPetEnchantmentKey)
                .addOptional(PHEnchantments.TOUGH.location())
                .addOptional(PHEnchantments.BUBBLING.location())
                .addOptional(PHEnchantments.VAMPIRE.location())
                .addOptional(PHEnchantments.BLAZING_PROTECTION.location())
                .addOptional(PHEnchantments.ORE_SCENTING.location())
                .addOptional(PHEnchantments.SonicBoom.location())
                .addOptional(PHEnchantments.SHARE.location())
                .addOptional(PHEnchantments.PARALYSIS.location())
                .addOptional(PHEnchantments.BLIGHT_CURSE.location())
                .addOptional(PHEnchantments.INFAMY_CURSE.location())
                .addOptional(PHEnchantments.IMMATURITY_CURSE.location())
                .addOptional(PHEnchantments.AMPHIBIOUS.location())
                .addOptional(PHEnchantments.HEALING_AURA.location())
                .addOptional(PHEnchantments.CHAIN_LIGHTNING.location())
                .addOptional(PHEnchantments.XP_Transfer.location())
                .addOptional(PHEnchantments.LINKED_INVENTORY.location())
                .addOptional(PHEnchantments.HEALTH_BOOST.location())
                .addOptional(PHEnchantments.IMMUNITY_FRAME.location())
                .addOptional(PHEnchantments.DEFLECTION.location())
                .addOptional(PHEnchantments.FIREPROOF.location())
                .addOptional(PHEnchantments.DEFUSAL.location())
                .addOptional(PHEnchantments.TOTAL_RECALL.location())
                .addOptional(PHEnchantments.SPEEDSTER.location())
                .addOptional(PHEnchantments.HEALTH_SIPHON.location())
                .addOptional(PHEnchantments.PSYCHIC_WALL.location())
                .addOptional(PHEnchantments.SHEPHERD.location())
                .addOptional(PHEnchantments.MAGNETIC.location())
                .addOptional(PHEnchantments.GLUTTONOUS.location())
                .addOptional(PHEnchantments.INTIMIDATION.location())
                .addOptional(PHEnchantments.POISON_RESISTANCE.location())
                .addOptional(PHEnchantments.FROST_FANG.location())
                .addOptional(PHEnchantments.WARPING_BITE.location())
                .addOptional(PHEnchantments.SHADOW_HANDS.location())
                .addOptional(PHEnchantments.TETHERED_TELEPORT.location())
                .addOptional(PHEnchantments.REJUVENATION.location())
                .addOptional(PHEnchantments.VOID_CLOUD.location())
                .addOptional(PHEnchantments.INSIGHT.location())
                .addOptional(PHEnchantments.CHAOS.location())
                .addOptional(PHEnchantments.NIGHT_VISION.location())
                .addOptional(PHEnchantments.VIOLENT.location())
                .addOptional(PHEnchantments.CHARISMA.location())
                .addOptional(PHEnchantments.DISC_JOCKEY.location())
                .addOptional(PHEnchantments.MUFFLED.location());
    }

    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

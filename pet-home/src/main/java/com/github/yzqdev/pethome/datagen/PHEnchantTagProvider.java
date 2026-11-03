package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PHEnchantTagProvider extends EnchantmentTagsProvider {
    public PHEnchantTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, PetHomeMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.TREASURE).addOptional(PHEnchantments.BUBBLING)
                .addOptional(PHEnchantments.VAMPIRE)
                .addOptional(PHEnchantments.BLAZING_PROTECTION)
                .addOptional(PHEnchantments.ORE_SCENTING)
                .addOptional(PHEnchantments.SonicBoom)
                .addOptional(PHEnchantments.SHARE)
                .addOptional(PHEnchantments.PARALYSIS)
                // muffled（自 1.20 移植的 LootOnly）：只能经战利品表获取
                .addOptional(PHEnchantments.MUFFLED)
                .replace(false)
        ;
        tag(EnchantmentTags.CURSE).addOptional(PHEnchantments.BLIGHT_CURSE).addOptional(PHEnchantments.INFAMY_CURSE).addOptional(PHEnchantments.IMMATURITY_CURSE).replace(false);
        this.tag(PHTags.TradableEnchantmentKey)
                .addOptional(PHEnchantments.AMPHIBIOUS)
                .addOptional(PHEnchantments.HEALING_AURA)
                .addOptional(PHEnchantments.CHAIN_LIGHTNING)
                .addOptional(PHEnchantments.XP_Transfer)
                .addOptional(PHEnchantments.LINKED_INVENTORY)
                .addOptional(PHEnchantments.HEALTH_BOOST)
                .addOptional(PHEnchantments.IMMUNITY_FRAME)
                .addOptional(PHEnchantments.DEFLECTION)
                .addOptional(PHEnchantments.FIREPROOF)
                .addOptional(PHEnchantments.IMMATURITY_CURSE)
                .addOptional(PHEnchantments.DEFUSAL)
                .addOptional(PHEnchantments.TOTAL_RECALL)
                .addOptional(PHEnchantments.SPEEDSTER)
                .addOptional(PHEnchantments.HEALTH_SIPHON)
                .addOptional(PHEnchantments.PSYCHIC_WALL)
                .addOptional(PHEnchantments.SHEPHERD)
                .addOptional(PHEnchantments.MAGNETIC)
                .addOptional(PHEnchantments.INFAMY_CURSE)
                .addOptional(PHEnchantments.GLUTTONOUS)
                .addOptional(PHEnchantments.INTIMIDATION)
                .addOptional(PHEnchantments.POISON_RESISTANCE)
                .addOptional(PHEnchantments.FROST_FANG)
                .addOptional(PHEnchantments.WARPING_BITE)
                .addOptional(PHEnchantments.SHADOW_HANDS)
                .addOptional(PHEnchantments.TETHERED_TELEPORT)
                .addOptional(PHEnchantments.REJUVENATION)
                .addOptional(PHEnchantments.BLIGHT_CURSE)
                .addOptional(PHEnchantments.SonicBoom)
                .addOptional(PHEnchantments.INSIGHT)
                .addOptional(PHEnchantments.CHAOS)
                .addOptional(PHEnchantments.NIGHT_VISION)
                .addOptional(PHEnchantments.VIOLENT)
                // 自 1.20 移植：charisma 只能驯兽师交易（不进附魔台）；disc_jockey 为普通附魔（附魔台/交易均可）
                .addOptional(PHEnchantments.CHARISMA)
                .addOptional(PHEnchantments.DISC_JOCKEY)


        ;
        this.tag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .addOptional(PHEnchantments.AMPHIBIOUS)
                .addOptional(PHEnchantments.CHAIN_LIGHTNING)
                .addOptional(PHEnchantments.DEFLECTION)
                .addOptional(PHEnchantments.FIREPROOF)
                .addOptional(PHEnchantments.FROST_FANG)
                .addOptional(PHEnchantments.GLUTTONOUS)
                .addOptional(PHEnchantments.HEALTH_SIPHON)
                .addOptional(PHEnchantments.HEALTH_BOOST)
                .addOptional(PHEnchantments.HEALING_AURA)
                .addOptional(PHEnchantments.INFAMY_CURSE)
                .addOptional(PHEnchantments.INTIMIDATION)
                .addOptional(PHEnchantments.IMMUNITY_FRAME)
                .addOptional(PHEnchantments.LINKED_INVENTORY)
                .addOptional(PHEnchantments.MAGNETIC)
                .addOptional(PHEnchantments.PSYCHIC_WALL)
                .addOptional(PHEnchantments.SHEPHERD)
                .addOptional(PHEnchantments.SPEEDSTER)
                .addOptional(PHEnchantments.POISON_RESISTANCE)
                .addOptional(PHEnchantments.WARPING_BITE)
                .addOptional(PHEnchantments.TETHERED_TELEPORT)
                .addOptional(PHEnchantments.XP_Transfer)
                .addOptional(PHEnchantments.BLIGHT_CURSE)
                .addOptional(PHEnchantments.DEFUSAL)
                .addOptional(PHEnchantments.TOTAL_RECALL)
                .addOptional(PHEnchantments.REJUVENATION)

                .addOptional(PHEnchantments.INSIGHT)
                // 自 1.20 移植：disc_jockey 普通附魔台可得；charisma 仅驯兽师交易、muffled 宝藏限定，二者不进附魔台
                .addOptional(PHEnchantments.DISC_JOCKEY)

                .replace(false)

        ;
        this.tag(PHTags.InfuserAdvancedKey).addTag(PHTags.AllPetEnchantmentKey);

        // 本模组全部附魔（含宝藏/诅咒）聚合标签：EI 高级灌注器经 InfuserAdvancedKey 引用，
        // 宝藏附魔由此获得灌注器获取渠道
        this.tag(PHTags.AllPetEnchantmentKey)
                .addOptional(PHEnchantments.TOUGH)
                .addOptional(PHEnchantments.BUBBLING)
                .addOptional(PHEnchantments.VAMPIRE)
                .addOptional(PHEnchantments.BLAZING_PROTECTION)
                .addOptional(PHEnchantments.ORE_SCENTING)
                .addOptional(PHEnchantments.SonicBoom)
                .addOptional(PHEnchantments.SHARE)
                .addOptional(PHEnchantments.PARALYSIS)
                .addOptional(PHEnchantments.BLIGHT_CURSE)
                .addOptional(PHEnchantments.INFAMY_CURSE)
                .addOptional(PHEnchantments.IMMATURITY_CURSE)
                .addOptional(PHEnchantments.AMPHIBIOUS)
                .addOptional(PHEnchantments.HEALING_AURA)
                .addOptional(PHEnchantments.CHAIN_LIGHTNING)
                .addOptional(PHEnchantments.XP_Transfer)
                .addOptional(PHEnchantments.LINKED_INVENTORY)
                .addOptional(PHEnchantments.HEALTH_BOOST)
                .addOptional(PHEnchantments.IMMUNITY_FRAME)
                .addOptional(PHEnchantments.DEFLECTION)
                .addOptional(PHEnchantments.FIREPROOF)
                .addOptional(PHEnchantments.DEFUSAL)
                .addOptional(PHEnchantments.TOTAL_RECALL)
                .addOptional(PHEnchantments.SPEEDSTER)
                .addOptional(PHEnchantments.HEALTH_SIPHON)
                .addOptional(PHEnchantments.PSYCHIC_WALL)
                .addOptional(PHEnchantments.SHEPHERD)
                .addOptional(PHEnchantments.MAGNETIC)
                .addOptional(PHEnchantments.GLUTTONOUS)
                .addOptional(PHEnchantments.INTIMIDATION)
                .addOptional(PHEnchantments.POISON_RESISTANCE)
                .addOptional(PHEnchantments.FROST_FANG)
                .addOptional(PHEnchantments.WARPING_BITE)
                .addOptional(PHEnchantments.SHADOW_HANDS)
                .addOptional(PHEnchantments.TETHERED_TELEPORT)
                .addOptional(PHEnchantments.REJUVENATION)
                .addOptional(PHEnchantments.VOID_CLOUD)
                .addOptional(PHEnchantments.INSIGHT)
                .addOptional(PHEnchantments.CHAOS)
                .addOptional(PHEnchantments.NIGHT_VISION)
                .addOptional(PHEnchantments.VIOLENT)
                .addOptional(PHEnchantments.CHARISMA)
                .addOptional(PHEnchantments.DISC_JOCKEY)
                .addOptional(PHEnchantments.MUFFLED);
    }

    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

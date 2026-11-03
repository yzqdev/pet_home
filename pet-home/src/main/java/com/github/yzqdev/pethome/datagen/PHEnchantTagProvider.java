package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * 附魔标签：泡泡控制 / 类吸血鬼 / 烈焰保护 / 矿物之味 / 音爆 / 分享 / 麻痹进宝藏附魔
 * （{@code minecraft:treasure}）；三个诅咒附魔进 {@code minecraft:curse}；
 * 可交易附魔进 {@code trade_enchantment_book}（驯兽师附魔书交易池）并补充附魔台可用标签；
 * 最后把可交易标签整体并入 Enchanting Infuser 高级灌注台标签（联动兼容）。
 */
public class PHEnchantTagProvider extends EnchantmentTagsProvider {
    /** 构造 provider（Fabric datagen 标准签名）。 */
    public PHEnchantTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    /** 生成上述附魔标签。 */
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.TREASURE).addOptional(PHEnchantments.BUBBLING)
                .addOptional(PHEnchantments.VAMPIRE)
                .addOptional(PHEnchantments.BLAZING_PROTECTION)
                .addOptional(PHEnchantments.ORE_SCENTING)
                .addOptional(PHEnchantments.SonicBoom)
                .addOptional(PHEnchantments.SHARE)
                .addOptional(PHEnchantments.PARALYSIS)
                .addOptional(PHEnchantments.MUFFLED)
                .setReplace(false)
        ;
        tag(EnchantmentTags.CURSE).addOptional(PHEnchantments.BLIGHT_CURSE).addOptional(PHEnchantments.INFAMY_CURSE).addOptional(PHEnchantments.IMMATURITY_CURSE).setReplace(false);
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
                //  charisma 只能驯兽师交易（TradeOnly，不进附魔台）；
                // disc_jockey 为普通附魔（附魔台/交易均可）；muffled 宝藏限定不进交易
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
                //  disc_jockey 普通附魔台可得；charisma 仅驯兽师交易、muffled 宝藏限定，二者不进附魔台
                .addOptional(PHEnchantments.DISC_JOCKEY)
                .setReplace(false)

        ;
        // EI 26.3 灌注器可用附魔只读此标签；EI 自身生成物仅含 #minecraft:in_enchanting_table，
        // 同名标签文件跨模组运行时并集合并，此处引用本模组全量附魔标签（含宝藏/诅咒，40 项）
        // ——宝藏附魔由此获得灌注器获取渠道
        this.tag(PHTags.InfuserAdvancedKey).addTag(PHTags.AllPetEnchantmentKey);

        // 本模组全部附魔（含宝藏/诅咒）聚合标签：EI 灌注器经 InfuserAdvancedKey 引用，
        // 也便于其他集成方一次性引用 pet-home 附魔全集
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

    /** provider 名称（datagen 日志显示用）。 */
    @Override
    public String getName() {
        return "mod enchantment tags";
    }
}

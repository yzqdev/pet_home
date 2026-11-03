package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 战利品注入的掉落类型：与 1.20.1 solaris（Forge）侧同名同构，也与 26.x 各侧同构。 每个常量绑定一种掉落（附魔书或特殊物品）与对应配置概率；概率经 {@link
 * Supplier} 每次 roll 时读取配置，保持配置热生效；新增掉落物只需加一个常量 + {@link DILootModifier} 里一条 {@code ENTRIES} 映射。
 *
 * <p>本侧没有 NeoForge 的 {@code loot_modifiers} JSON 机制（ Fabric 走 {@code LootTableEvents.MODIFY}
 * 运行时注入），故不注册 {@code CODEC}。
 */
public enum PHLootType {
    SINISTER_CARROT(null, () -> PetHomeMod.CONFIG.sinisterCarrotLootChance.get()),
    BUBBLING(PHEnchantmentRegistry.BUBBLING, () -> PetHomeMod.CONFIG.bubblingLootChance.get()),
    VAMPIRISM(PHEnchantmentRegistry.VAMPIRE, () -> PetHomeMod.CONFIG.vampirismLootChance.get()),
    VOID_CLOUD(PHEnchantmentRegistry.VOID_CLOUD, () -> PetHomeMod.CONFIG.voidCloudLootChance.get()),
    ORE_SCENTING(PHEnchantmentRegistry.ORE_SCENTING, () -> PetHomeMod.CONFIG.oreScentingLootChance.get()),
    MUFFLED(PHEnchantmentRegistry.MUFFLED, () -> PetHomeMod.CONFIG.muffledLootChance.get()),
    BLAZING_PROTECTION(PHEnchantmentRegistry.BLAZING_PROTECTION, () -> PetHomeMod.CONFIG.blazingProtectionLootChance.get()),
    SHARE(PHEnchantmentRegistry.SHARE, () -> PetHomeMod.CONFIG.shareLootChance.get()),
    SONIC_BOOM(PHEnchantmentRegistry.SONIC_BOOM, () -> PetHomeMod.CONFIG.sonicBoomLootChance.get()),
    PARALYSIS(PHEnchantmentRegistry.PARALYSIS, () -> PetHomeMod.CONFIG.paralysisLootChance.get()),
    TOUGH(PHEnchantmentRegistry.TOUGH, () -> PetHomeMod.CONFIG.toughLootChance.get());

    /** 掉落物为附魔书时绑定的附魔；特殊物品掉落（如阴森胡萝卜）为 null */
    private final Enchantment enchantment;

    private final Supplier<Double> chance;

    PHLootType(Enchantment enchantment, Supplier<Double> chance) {
        this.enchantment = enchantment;
        this.chance = chance;
    }

    public Enchantment enchantment() {
        return this.enchantment;
    }

    public double chance() {
        return this.chance.get();
    }

    /** 是否为附魔书掉落（false = 特殊物品掉落，如阴森胡萝卜） */
    public boolean isEnchantedBook() {
        return this.enchantment != null;
    }
}

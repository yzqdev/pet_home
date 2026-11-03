package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 全局战利品修改器的掉落类型：每个常量绑定一种掉落（附魔书或特殊物品）与对应配置概率。 概率经 {@link Supplier} 每次 roll 时读取配置，保持配置热生效；新增掉落物只需加一个常量 + 对应 loot_modifiers JSON。
 * 序列化名为小写下划线（与 26.x 侧同名同序，除 1.20 独有的 void_cloud）。
 */
public enum PHLootType implements StringRepresentable {
    SINISTER_CARROT("sinister_carrot", null, () -> PetHomeMod.CONFIG.sinisterCarrotLootChance.get()),
    BUBBLING("bubbling", PHEnchantmentRegistry.BUBBLING, () -> PetHomeMod.CONFIG.bubblingLootChance.get()),
    VAMPIRISM("vampirism", PHEnchantmentRegistry.VAMPIRE, () -> PetHomeMod.CONFIG.vampirismLootChance.get()),
    VOID_CLOUD("void_cloud", PHEnchantmentRegistry.VOID_CLOUD, () -> PetHomeMod.CONFIG.voidCloudLootChance.get()),
    ORE_SCENTING("ore_scenting", PHEnchantmentRegistry.ORE_SCENTING, () -> PetHomeMod.CONFIG.oreScentingLootChance.get()),
    MUFFLED("muffled", PHEnchantmentRegistry.MUFFLED, () -> PetHomeMod.CONFIG.muffledLootChance.get()),
    BLAZING_PROTECTION("blazing_protection", PHEnchantmentRegistry.BLAZING_PROTECTION, () -> PetHomeMod.CONFIG.blazingProtectionLootChance.get()),
    SHARE("share", PHEnchantmentRegistry.SHARE, () -> PetHomeMod.CONFIG.shareLootChance.get()),
    SONIC_BOOM("sonic_boom", PHEnchantmentRegistry.SONIC_BOOM, () -> PetHomeMod.CONFIG.sonicBoomLootChance.get()),
    PARALYSIS("paralysis", PHEnchantmentRegistry.PARALYSIS, () -> PetHomeMod.CONFIG.paralysisLootChance.get()),
    TOUGH("tough", PHEnchantmentRegistry.TOUGH, () -> PetHomeMod.CONFIG.toughLootChance.get());

    public static final EnumCodec<PHLootType> CODEC = StringRepresentable.fromEnum(PHLootType::values);

    private final String name;

    /** 掉落物为附魔书时绑定的附魔；特殊物品掉落（如胡萝卜）为 null */
    private final Enchantment enchantment;

    private final Supplier<Double> chance;

    PHLootType(String name, Enchantment enchantment, Supplier<Double> chance) {
        this.name = name;
        this.enchantment = enchantment;
        this.chance = chance;
    }

    @Override
    public String getSerializedName() {
        return this.name;
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

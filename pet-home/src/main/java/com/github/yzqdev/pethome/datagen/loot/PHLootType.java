package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import java.util.function.DoubleSupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 全局战利品的掉落类型：与 NeoForge 侧 {@code PHLootType} 同名同构。 每个常量绑定一种掉落（附魔书或特殊物品）与对应配置概率；概率经 {@link
 * DoubleSupplier} 每次 roll 时读取配置，保持配置热生效；新增掉落物只需加一个常量 + {@link PetHomeLootInjector} 里一条 {@code
 * ENTRIES} 映射。
 *
 * <p>与 NeoForge 侧的差异：NF 侧该枚举还负责全局战利品修改器 JSON 的序列化（注册 {@code CODEC}、实现 {@link
 * StringRepresentable#getSerializedName()} 供变形器 type 字段使用）；Fabric 侧没有该 JSON 机制，改由 {@link
 * PetHomeLootInjector} 在每张战利品表构建时运行时注入，故此处不注册 CODEC。
 */
public enum PHLootType implements StringRepresentable {
    SINISTER_CARROT("sinister_carrot", null, () -> PetHomeConfig.sinisterCarrotLootChance),
    BUBBLING("bubbling", PHEnchantments.BUBBLING, () -> PetHomeConfig.bubblingLootChance),
    VAMPIRISM("vampirism", PHEnchantments.VAMPIRE, () -> PetHomeConfig.vampirismLootChance),
    VOID_CLOUD("void_cloud", PHEnchantments.VOID_CLOUD, () -> PetHomeConfig.voidCloudLootChance),
    SHARE("share", PHEnchantments.SHARE, () -> PetHomeConfig.shareLootChance),
    ORE_SCENTING("ore_scenting", PHEnchantments.ORE_SCENTING, () -> PetHomeConfig.oreScentingLootChance),
    SONIC_BOOM("sonic_boom", PHEnchantments.SonicBoom, () -> PetHomeConfig.sonicBoomLootChance),
    BLAZING_PROTECTION("blazing_protection", PHEnchantments.BLAZING_PROTECTION,
            () -> PetHomeConfig.blazingProtectionLootChance),
    PARALYSIS("paralysis", PHEnchantments.PARALYSIS, () -> PetHomeConfig.paralysisLootChance),
    TOUGH("tough", PHEnchantments.TOUGH, () -> PetHomeConfig.toughLootChance),
    MUFFLED("muffled", PHEnchantments.MUFFLED, () -> PetHomeConfig.muffledLootChance);

    private final String name;

    /** 掉落物为附魔书时绑定的附魔 key；特殊物品掉落（如胡萝卜）为 null */
    private final ResourceKey<Enchantment> enchantmentKey;

    private final DoubleSupplier chance;

    PHLootType(String name, ResourceKey<Enchantment> enchantmentKey, DoubleSupplier chance) {
        this.name = name;
        this.enchantmentKey = enchantmentKey;
        this.chance = chance;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public ResourceKey<Enchantment> enchantmentKey() {
        return this.enchantmentKey;
    }

    public double chance() {
        return this.chance.getAsDouble();
    }

    /** 是否为附魔书掉落（false = 特殊物品掉落，如阴森胡萝卜） */
    public boolean isEnchantedBook() {
        return this.enchantmentKey != null;
    }
}

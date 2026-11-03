package com.github.yzqdev.pethome.datagen.loot;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import java.util.function.DoubleSupplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 全局战利品的掉落类型：与 NeoForge 侧 {@code PHLootType} 同构。 每个常量绑定一种掉落（附魔书或特殊物品）与对应配置概率；概率经 {@link
 * DoubleSupplier} 每次 roll 时读取配置，保持配置热生效；新增掉落物只需加一个常量 + {@link PHLootRegistry} 里一条映射。
 *
 * <p>本侧原先是用 {@code int} 下标 0~9 加 {@code switch} 表达的，可读性差且漏掉 muffled（配置有、没接）；改为枚举后与其它
 * Fabric 侧一致。注：NeoForge 侧该枚举还负责全局战利品修改器 JSON 的序列化（注册 {@code CODEC}）， Fabric 侧没有该 JSON
 * 机制（走 {@link PHLootRegistry} 运行时注入），故此处不注册 CODEC。
 */
public enum PHLootType {
  SINISTER_CARROT(null, () -> PetHomeConfig.sinisterCarrotLootChance),
  BUBBLING(PHEnchantments.BUBBLING, () -> PetHomeConfig.bubblingLootChance),
  VAMPIRISM(PHEnchantments.VAMPIRE, () -> PetHomeConfig.vampirismLootChance),
  VOID_CLOUD(PHEnchantments.VOID_CLOUD, () -> PetHomeConfig.voidCloudLootChance),
  SHARE(PHEnchantments.SHARE, () -> PetHomeConfig.shareLootChance),
  ORE_SCENTING(PHEnchantments.ORE_SCENTING, () -> PetHomeConfig.oreScentingLootChance),
  SONIC_BOOM(PHEnchantments.SonicBoom, () -> PetHomeConfig.sonicBoomLootChance),
  BLAZING_PROTECTION(
      PHEnchantments.BLAZING_PROTECTION, () -> PetHomeConfig.blazingProtectionLootChance),
  PARALYSIS(PHEnchantments.PARALYSIS, () -> PetHomeConfig.paralysisLootChance),
  TOUGH(PHEnchantments.TOUGH, () -> PetHomeConfig.toughLootChance),
  MUFFLED(PHEnchantments.MUFFLED, () -> PetHomeConfig.muffledLootChance);

  /** 掉落物为附魔书时绑定的附魔 key；特殊物品掉落（如阴森胡萝卜）为 null */
  private final ResourceKey<Enchantment> enchantmentKey;

  private final DoubleSupplier chance;

  PHLootType(ResourceKey<Enchantment> enchantmentKey, DoubleSupplier chance) {
    this.enchantmentKey = enchantmentKey;
    this.chance = chance;
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

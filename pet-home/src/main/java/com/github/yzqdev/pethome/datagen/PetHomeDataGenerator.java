package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.loot.PHBlockLootTableProvider;
import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

/**
 * datagen 入口（{@code fabric.mod.json} 的 {@code fabric-datagen} entrypoint）。 {@code runDatagen} 时注册
 * 11 个 provider（动态注册表 / 各类 tag / 配方 / 模型 / 双语 lang / 战利品）， 产出写 {@code src/main/generated}；动态注册表（附魔
 * + 伤害类型）经 {@link #buildRegistry} 的 {@link RegistrySetBuilder} 引导，产出 {@code
 * data/pet_home/enchantment|damage_type} 下的 JSON。
 */
public class PetHomeDataGenerator implements DataGeneratorEntrypoint {

  public static final RegistrySetBuilder BUILDER =
      new RegistrySetBuilder()
          .add(Registries.ENCHANTMENT, PHEnchantments::bootstrap)
          .add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap);

  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
    FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

    pack.addProvider(PHDatapackProvider::new);
    pack.addProvider(PHBlockTagsProvider::new);
    pack.addProvider(PHRecipeProvider::new);
    pack.addProvider(PHItemProvider::new);
    pack.addProvider(PHPoiTagProvider::new);
    pack.addProvider(PHEntityTagsProvider::new);
    pack.addProvider(PHEnchantTagProvider::new);
    pack.addProvider(PHItemTagProvider::new);
    pack.addProvider(PHEnLangProvider::new);
    pack.addProvider(PHZhLangProvider::new);
    pack.addProvider(PHLootProvider::new);
    pack.addProvider(PHBlockLootTableProvider::new);
    pack.addProvider(PHParticleProvider::new);
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {
    registryBuilder.add(Registries.ENCHANTMENT, PHEnchantments::bootstrap);
    registryBuilder.add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap);
  }

  @Override
  public String getEffectiveModId() {
    return PetHomeMod.MODID;
  }
}

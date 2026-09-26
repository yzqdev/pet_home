package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class PHDataEntry implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // —— 动态注册表（datapack 的注册表内容）——
        pack.addProvider(DataPackGenerator::new);

        // —— lang ——
        pack.addProvider(ModEnLangProvider::new);
        pack.addProvider(ModZhLangProvider::new);

        // —— 模型 ——
        pack.addProvider(PHModelProvider::new);

        // —— 粒子 ——
        pack.addProvider(PHParticleProvider::new);

        // —— 标签 ——
        pack.addProvider(PHItemTagsProvider::new);
        pack.addProvider(ModBlockTagsProvider::new);
        pack.addProvider(ModEntityTagsProvider::new);
        pack.addProvider(ModPoiTagProvider::new);
        pack.addProvider(ModEnchantTagProvider::new);
        pack.addProvider(ModVillagerTradeTagProvider::new);

        // —— 配方 / 战利品表 ——
        pack.addProvider(ModRecipeProvider.Runner::new);
        pack.addProvider(PHLootProvider::new);
        pack.addProvider(PHBlockLootTableProvider::new);
    }


    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
                .add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap)
                .add(Registries.VILLAGER_TRADE, PHVillagerTrade::bootstrapVillagerTrade)
                .add(Registries.TRADE_SET, PHVillagerTrade::bootstrapTradeSet);
    }
}

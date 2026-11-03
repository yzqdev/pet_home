package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

/**
 * datagen 入口（{@code fabric-datagen} entrypoint，{@code runDatagen} 触发）。
 * {@link #onInitializeDataGenerator} 注册全部 provider（动态注册表/lang/模型/粒子/标签/配方/战利品）；
 * {@link #buildRegistry} 提供动态注册表内容（40 附魔、伤害类型、村民交易/交易集）。
 * 刻意不注册 worldgen 注册表：宠物商店结构是手写资源，{@code addAll} 会把原版结构重复输出。
 */
public class PHDataEntry implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // —— 动态注册表（datapack 的注册表内容）——
        pack.addProvider(DataPackGenerator::new);

        // —— lang ——
        pack.addProvider(PHEnLangProvider::new);
        pack.addProvider(PHZhLangProvider::new);

        // —— 模型 ——
        pack.addProvider(PHModelProvider::new);

        // —— 粒子 ——
        pack.addProvider(PHParticleProvider::new);

        // —— 标签 ——
        pack.addProvider(PHItemTagsProvider::new);
        pack.addProvider(PHBlockTagsProvider::new);
        pack.addProvider(PHEntityTagsProvider::new);
        pack.addProvider(PHPoiTagProvider::new);
        pack.addProvider(PHEnchantTagProvider::new);
        pack.addProvider(PHVillagerTradeTagProvider::new);

        // —— 配方 / 战利品表 ——
        pack.addProvider(PHRecipeProvider.Runner::new);
        pack.addProvider(PHLootProvider::new);
        pack.addProvider(PHBlockLootTableProvider::new);
    }


    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, PHEnchantments::bootstrap)
                .add(Registries.DAMAGE_TYPE, DamageTypeModifier::bootstrap)
                .add(Registries.VILLAGER_TRADE, PHVillagerTrade::bootstrapVillagerTrade)
                .add(Registries.TRADE_SET, PHVillagerTrade::bootstrapTradeSet);
    }
}

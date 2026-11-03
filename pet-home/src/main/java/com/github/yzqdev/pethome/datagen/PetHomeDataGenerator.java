package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * datagen 入口（fabric-datagen entrypoint）：注册标签 / 配方 / 物品模型 / 双语 lang 各 provider。
 * 1.20 的附魔是真实 Enchantment 实例而非数据驱动（无动态注册表输出），方块模型等其余资产走静态资源。
 */
public class PetHomeDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(PHBlockTagsProvider::new);
        pack.addProvider(PHEntityTagsProvider::new);
        pack.addProvider(PHPoiTagProvider::new);
        pack.addProvider(PHRecipeProvider::new);
        pack.addProvider(PHItemModelProvider::new);
        pack.addProvider(PHBlockStateProvider::new);
        pack.addProvider(PHParticleProvider::new);
        pack.addProvider(PHEnLangProvider::new);
        pack.addProvider(PHZhLangProvider::new);
    }

    @Override
    public String getEffectiveModId() {
        return PetHomeMod.MODID;
    }
}

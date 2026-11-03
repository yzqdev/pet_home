package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.loot.GlobalLootModifier;
import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/11 13:49
 * @modified By:
 */
@EventBusSubscriber(modid = PetHomeMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherDataGen(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 数据包资源（loot table / recipe / advancement 等由各 Provider 生成）
        var datapack = new PHDatapackProvider(packOutput, lookupProvider);
        var datapackLookProvider = datapack.getRegistryProvider();
        generator.addProvider(event.includeServer(), datapack);

        var blockTagProvider = new PHBlockTagsProvider(packOutput, lookupProvider, PetHomeMod.MODID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new PHRecipeProvider(packOutput, lookupProvider));
        // 方块状态/方块模型 provider 须先于 PHItemProvider 注册（鼓的物品模型引用其方块模型）
        generator.addProvider(event.includeServer(), new PHBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new PHItemProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new PHPoiTagProvider(packOutput, lookupProvider, existingFileHelper));


        generator.addProvider(event.includeServer(), new PHEntityTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new PHEnchantTagProvider(packOutput, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(), new GlobalLootModifier(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new PHItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), PetHomeMod.MODID, existingFileHelper));

        generator.addProvider(event.includeServer(), new PHEnLangProvider(packOutput, PetHomeMod.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new PHZhLangProvider(packOutput, PetHomeMod.MODID, "zh_cn"));

        generator.addProvider(event.includeServer(), new PHLootProvider(packOutput, datapackLookProvider));

        generator.addProvider(event.includeClient(), new PHParticleProvider(packOutput, existingFileHelper));

    }
}

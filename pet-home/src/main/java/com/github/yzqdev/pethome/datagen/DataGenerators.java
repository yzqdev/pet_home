package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.loot.GlobalLootModifier;
import com.github.yzqdev.pethome.datagen.loot.PHLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/11 13:49
 * @modified By:
 */
@EventBusSubscriber(modid = PetHomeMod.MODID, value = Dist.CLIENT)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherDataGen(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // 数据包资源（loot table / recipe / advancement 等由各 Provider 生成）
        var datapack = new PHDatapackProvider(packOutput, lookupProvider);
        var datapackLookProvider = datapack.getRegistryProvider();


        event.createProvider(p -> datapack);
        event.createProvider(p -> new PHBlockTagsProvider(packOutput, lookupProvider, PetHomeMod.MODID));
        event.createProvider(p -> new PHRecipeProvider.Runner(packOutput, lookupProvider));
        event.createProvider(p -> new PHItemProvider(packOutput, PetHomeMod.MODID));
        event.createProvider(p -> new PHPoiTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new PHEntityTagsProvider(packOutput, lookupProvider));
        event.createProvider(p -> new PHEnchantTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new GlobalLootModifier(packOutput, lookupProvider));
        event.createProvider(p -> new PHItemTagProvider(packOutput, lookupProvider, PetHomeMod.MODID));
        event.createProvider(p -> new PHVillagerTradeTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new PHParticleProvider(packOutput));
        event.createProvider(p -> new PHEnLangProvider(packOutput, PetHomeMod.MODID, "en_us"));
        event.createProvider(p -> new PHZhLangProvider(packOutput, PetHomeMod.MODID, "zh_cn"));
        event.createProvider(p -> new PHLootProvider(packOutput, datapackLookProvider));
    }
}

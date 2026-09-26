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

        //datapack
        var datapack = new ModDatapackProvider(packOutput, lookupProvider);
        var datapackLookProvider = datapack.getRegistryProvider();


        event.createProvider(p -> datapack);
        event.createProvider(p -> new ModBlockTagsProvider(packOutput, lookupProvider, PetHomeMod.MODID));
        event.createProvider(p -> new ModRecipeProvider.Runner(packOutput, lookupProvider));
        event.createProvider(p -> new ModItemProvider(packOutput, PetHomeMod.MODID));
        event.createProvider(p -> new ModPoiTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new ModEntityTagsProvider(packOutput, lookupProvider));
        event.createProvider(p -> new ModEnchantTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new GlobalLootModifier(packOutput, lookupProvider));
        event.createProvider(p -> new ModItemTagProvider(packOutput, lookupProvider, PetHomeMod.MODID));
        event.createProvider(p -> new ModVillagerTradeTagProvider(packOutput, lookupProvider));
        event.createProvider(p -> new ModEnLangProvider(packOutput, PetHomeMod.MODID, "en_us"));
        event.createProvider(p -> new ModZhLangProvider(packOutput, PetHomeMod.MODID, "zh_cn"));
        event.createProvider(p -> new PHLootProvider(packOutput, datapackLookProvider));
    }
}

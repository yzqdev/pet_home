package com.github.yzqdev.pet_home.datagen;

import com.github.yzqdev.pet_home.PetHomeMod;

import com.github.yzqdev.pet_home.datagen.loot.GlobalLootModifier;
import com.github.yzqdev.pet_home.datagen.loot.PHLootProvider;
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

        //datapack
        var datapack = new ModDatapackProvider(packOutput, lookupProvider);
        var datapackLookProvider = datapack.getRegistryProvider();
        generator.addProvider(event.includeServer(), datapack);

        var blockTagProvider = new ModBlockTagsProvider(packOutput, lookupProvider, PetHomeMod.MODID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagProvider);
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModItemProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModPoiTagProvider(packOutput, lookupProvider, existingFileHelper));


        generator.addProvider(event.includeServer(), new ModEntityTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModEnchantTagProvider(packOutput, lookupProvider, existingFileHelper));

        generator.addProvider(event.includeServer(), new GlobalLootModifier(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), PetHomeMod.MODID, existingFileHelper));

        generator.addProvider(event.includeServer(), new ModEnLangProvider(packOutput, PetHomeMod.MODID, "en_us"));
        generator.addProvider(event.includeServer(), new ModZhLangProvider(packOutput, PetHomeMod.MODID, "zh_cn"));

        generator.addProvider(event.includeServer(), new PHLootProvider(packOutput, datapackLookProvider));

    }
}

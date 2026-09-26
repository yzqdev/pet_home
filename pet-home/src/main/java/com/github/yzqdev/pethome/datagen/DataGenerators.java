package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = PetHomeMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        // 实体标签（petstore_cage_0..3 / petstore_fishtank 等）此前从未注册生成，
        // 导致宠物店笼子/鱼缸拿到的标签为空、不刷动物
        generator.addProvider(event.includeServer(), new ModEntityTagsProvider(packOutput, lookupProvider, existingFileHelper));
        // 物品模型属于 assets，门控应走 includeClient
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModPoiTagProvider(packOutput,lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput ));
        generator.addProvider(event.includeServer(),new ModEnLangProvider(packOutput,PetHomeMod.MODID,"en_us"));
        generator.addProvider(event.includeServer(),new ModZhLangProvider(packOutput,PetHomeMod.MODID,"zh_cn"));
    }
}

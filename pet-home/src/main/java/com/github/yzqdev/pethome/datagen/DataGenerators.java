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

        // 数据包资源（loot table / recipe / advancement 等由各 Provider 生成）
        var datapack = new PHDatapackProvider(packOutput, lookupProvider);
        var datapackLookProvider = datapack.getRegistryProvider();


        generator.addProvider(event.includeServer(), datapack);

        var blockTagProvider = new PHBlockTagsProvider(packOutput, lookupProvider, PetHomeMod.MODID, existingFileHelper);

        generator.addProvider(event.includeServer(), blockTagProvider);
        // 物品标签（pet_beds / tame_frogs_with 等），copy 方块标签需传 blockTagProvider 的 contentsCache
        generator.addProvider(event.includeServer(), new PHItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), PetHomeMod.MODID, existingFileHelper));
        // 实体标签（petstore_cage_0..3 / petstore_fishtank 等）此前从未注册生成，
        // 导致宠物店笼子/鱼缸拿到的标签为空、不刷动物
        generator.addProvider(event.includeServer(), new PHEntityTagsProvider(packOutput, lookupProvider, existingFileHelper));
        // 物品模型属于 assets，门控应走 includeClient
        generator.addProvider(event.includeClient(), new PHItemModelProvider(packOutput, existingFileHelper));
        // 方块状态/方块模型（鼓三态、宠物床 16 色、灯笼）
        generator.addProvider(event.includeClient(), new PHBlockStateProvider(packOutput, existingFileHelper));
        // 粒子描述 JSON
        generator.addProvider(event.includeClient(), new PHParticleProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new PHPoiTagProvider(packOutput,lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new PHRecipeProvider(packOutput ));
        generator.addProvider(event.includeServer(),new PHEnLangProvider(packOutput,PetHomeMod.MODID,"en_us"));
        generator.addProvider(event.includeServer(),new PHZhLangProvider(packOutput,PetHomeMod.MODID,"zh_cn"));
    }
}

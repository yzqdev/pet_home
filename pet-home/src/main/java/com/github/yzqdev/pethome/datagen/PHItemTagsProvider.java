package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

/**
 * 物品标签数据生成：项圈标签可附魔物品（仅项圈标签本体）、宠物床物品集合、
 * 捕网发射器可吃耐久附魔。
 */
public class PHItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
    public PHItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.valueLookupBuilder(PHTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG );
        this.valueLookupBuilder(PHTags.PetBedKey).add(PHBlockRegistry.PetBedItems.values().stream()
                .toArray(Item[]::new));
        valueLookupBuilder(ItemTags.DURABILITY_ENCHANTABLE).add(PHItemRegistry.NET_LAUNCHER_ITEM);
    }
}

package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * @author yzqde
 * @date time 2024/12/17 1:44
 * @modified By:
 */
public class PHItemTagProvider extends ItemTagsProvider {

    public PHItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(PHTags.COLLAR_TAG_tagkey).add(PHItemRegistry.COLLAR_TAG.getKey());
        this.tag(PHTags.PetBedKey).add(PHBlockRegistry.PetBedItems.values().stream().map(i -> i.get()).toArray(Item[]::new));
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(PHItemRegistry.NET_LAUNCHER_ITEM.get());
        // 驯服青蛙用的食物
        this.tag(PHTags.tameFrogsWith).add(Items.SPIDER_EYE);
    }

    @Override
    public String getName() {
        return "mod item tags";
    }
}

package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class PHItemTagProvider extends ItemTagsProvider {
    public PHItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 宠物床染色配方的原料 tag
        this.tag(PHTags.PetBedKey)
                .add(PHBlockRegistry.WHITE_PET_BED.get().asItem())
                .add(PHBlockRegistry.ORANGE_PET_BED.get().asItem())
                .add(PHBlockRegistry.MAGENTA_PET_BED.get().asItem())
                .add(PHBlockRegistry.LIGHT_BLUE_PET_BED.get().asItem())
                .add(PHBlockRegistry.YELLOW_PET_BED.get().asItem())
                .add(PHBlockRegistry.LIME_PET_BED.get().asItem())
                .add(PHBlockRegistry.PINK_PET_BED.get().asItem())
                .add(PHBlockRegistry.GRAY_PET_BED.get().asItem())
                .add(PHBlockRegistry.LIGHT_GRAY_PET_BED.get().asItem())
                .add(PHBlockRegistry.CYAN_PET_BED.get().asItem())
                .add(PHBlockRegistry.PURPLE_PET_BED.get().asItem())
                .add(PHBlockRegistry.BLUE_PET_BED.get().asItem())
                .add(PHBlockRegistry.BROWN_PET_BED.get().asItem())
                .add(PHBlockRegistry.GREEN_PET_BED.get().asItem())
                .add(PHBlockRegistry.RED_PET_BED.get().asItem())
                .add(PHBlockRegistry.BLACK_PET_BED.get().asItem());
        // 驯服青蛙用的食物
        this.tag(PHTags.tameFrogsWith).add(Items.SPIDER_EYE);
    }
}

package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VillagerTradesTagsProvider;

import java.util.concurrent.CompletableFuture;

/** 驯兽师各级交易的 villager_trade tag（TradeSet 通过 tag 引用交易） */
public class ModVillagerTradeTagProvider extends VillagerTradesTagsProvider {

    public ModVillagerTradeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(PHVillagerTrade.TAMER_LVL1).add(
                PHVillagerTrade.TAMER_1_BONE, PHVillagerTrade.TAMER_1_EGG, PHVillagerTrade.TAMER_1_COD, PHVillagerTrade.TAMER_1_BOOK);
        this.tag(PHVillagerTrade.TAMER_LVL2).add(
                PHVillagerTrade.TAMER_2_TROPICAL_FISH_BUCKET, PHVillagerTrade.TAMER_2_BUY_CARROT, PHVillagerTrade.TAMER_2_BUY_STICK,
                PHVillagerTrade.TAMER_2_APPLE, PHVillagerTrade.TAMER_2_BOOK);
        this.tag(PHVillagerTrade.TAMER_LVL3).add(
                PHVillagerTrade.TAMER_3_ROTTEN_APPLE, PHVillagerTrade.TAMER_3_TADPOLE_BUCKET, PHVillagerTrade.TAMER_3_COLLAR_TAG,
                PHVillagerTrade.TAMER_3_ENCHANTED_COLLAR, PHVillagerTrade.TAMER_3_BOOK);
        this.tag(PHVillagerTrade.TAMER_LVL4).add(
                PHVillagerTrade.TAMER_4_AXOLOTL_BUCKET, PHVillagerTrade.TAMER_4_TURTLE_EGG,
                PHVillagerTrade.TAMER_4_ENCHANTED_COLLAR, PHVillagerTrade.TAMER_4_BOOK);
        this.tag(PHVillagerTrade.TAMER_LVL5).add(
                PHVillagerTrade.TAMER_5_GOLDEN_CARROT, PHVillagerTrade.TAMER_5_GOLDEN_APPLE, PHVillagerTrade.TAMER_5_COLLAR_TAG,
                PHVillagerTrade.TAMER_5_ENCHANTED_COLLAR, PHVillagerTrade.TAMER_5_BOOK);
    }
}

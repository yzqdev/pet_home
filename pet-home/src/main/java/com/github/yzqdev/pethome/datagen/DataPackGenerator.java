package com.github.yzqdev.pethome.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

/**
 * 动态注册表数据生成入口：把附魔、伤害类型、村民交易、交易集合四个动态注册表
 * 的全部内容输出到 {@code data/}（附魔与驯兽师交易均为此产生）。
 */
public class DataPackGenerator extends FabricDynamicRegistryProvider {

    public DataPackGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }


    @Override
    public String getName() {
        return "DataGenerator";
    }


    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
        entries.addAll(registries.lookupOrThrow(Registries.DAMAGE_TYPE));
        entries.addAll(registries.lookupOrThrow(Registries.VILLAGER_TRADE));
        entries.addAll(registries.lookupOrThrow(Registries.TRADE_SET));
    }


}

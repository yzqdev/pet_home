package com.github.yzqdev.pethome.server.entity;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.datagen.PHVillagerTrade;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Predicate;

public class PHVillagerRegistry {

    public static final DeferredRegister<VillagerProfession> DEF_REG = DeferredRegister.create(Registries.VILLAGER_PROFESSION, PetHomeMod.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> ANIMAL_TAMER = DEF_REG.register("animal_tamer", () -> buildVillagerProfession());
    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.getKey());
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.getKey());
        };

        Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel = new Int2ObjectOpenHashMap<>();
        tradeSetsByLevel.put(1, PHVillagerTrade.TAMER_TRADE_SET_LEVEL_1);
        tradeSetsByLevel.put(2, PHVillagerTrade.TAMER_TRADE_SET_LEVEL_2);
        tradeSetsByLevel.put(3, PHVillagerTrade.TAMER_TRADE_SET_LEVEL_3);
        tradeSetsByLevel.put(4, PHVillagerTrade.TAMER_TRADE_SET_LEVEL_4);
        tradeSetsByLevel.put(5, PHVillagerTrade.TAMER_TRADE_SET_LEVEL_5);

        return new VillagerProfession(Component.translatable(LangDefinition.ENTITY_ANIMAL_TAMER), heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), PHSoundRegistry.PET_BED_USE.get(), tradeSetsByLevel);
    }
}

package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;

import java.util.function.Predicate;

/** 村民职业注册：Fabric 原生 {@code Registry.register}。 */
public class PHVillagerRegistry {

    public static final VillagerProfession ANIMAL_TAMER = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer"), buildVillagerProfession());

    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.key());
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.key());
        };

        // 26.1 的村民交易是数据驱动的 TradeSet：等级 -> pet_home:tamer/level_N
        // （对应 NeoForge 侧 PHVillagerTrade 里的 ResourceKey 常量；这里内联以避免
        //  server 包依赖仍是 NeoForge 实现的 datagen 包）
        Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            tradeSetsByLevel.put(level, ResourceKey.create(Registries.TRADE_SET,
                    Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "tamer/level_" + level)));
        }

        return new VillagerProfession(Component.translatable(LangDefinition.ENTITY_ANIMAL_TAMER), heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), PHSoundRegistry.PET_BED_USE, tradeSetsByLevel);
    }

    public static void init() {
    }
}

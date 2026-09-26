package com.github.yzqdev.pethome.server.entity;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.function.Predicate;


public class PHVillagerRegistry {

    public static final VillagerProfession ANIMAL_TAMER = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION,
            ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "animal_tamer"), buildVillagerProfession());

    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.key());
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.key());
        };
        return new VillagerProfession("animal_tamer", heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), PHSoundRegistry.PET_BED_USE);
    }

    public static void init() {
    }
}

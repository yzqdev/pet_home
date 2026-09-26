package com.github.yzqdev.pethome.server.entity;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.DIPOIRegistry;
import com.github.yzqdev.pethome.server.misc.DISoundRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Predicate;

public class DIVillagerRegistry {


    public static final VillagerProfession ANIMAL_TAMER = Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, new ResourceLocation(PetHomeMod.MODID, "animal_tamer"), buildVillagerProfession());
    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(DIPOIRegistry.PET_BED_ID);
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(DIPOIRegistry.PET_BED_ID);
        };
        return new VillagerProfession("animal_tamer", heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), DISoundRegistry.PET_BED_USE);
    }

    public static void init() {
    }
}

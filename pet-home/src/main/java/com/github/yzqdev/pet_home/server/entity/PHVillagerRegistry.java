package com.github.yzqdev.pet_home.server.entity;



import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.misc.PHPOIRegistry;
import com.github.yzqdev.pet_home.server.misc.PHSoundRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.function.Predicate;

public class PHVillagerRegistry {

    public static final DeferredRegister<VillagerProfession> DEF_REG = DeferredRegister.create(Registries.VILLAGER_PROFESSION, PetHomeMod.MODID);

    public static final DeferredHolder<VillagerProfession,VillagerProfession> ANIMAL_TAMER = DEF_REG.register("animal_tamer", () -> buildVillagerProfession());
    public static boolean registeredHouses = false;

    private static VillagerProfession buildVillagerProfession() {
        Predicate<Holder<PoiType>> heldJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.getKey());
        };
        Predicate<Holder<PoiType>> acquirableJobSite = (poiType) -> {
            return poiType.is(PHPOIRegistry.PET_BED.getKey());
        };
        return new VillagerProfession("animal_tamer", heldJobSite, acquirableJobSite, ImmutableSet.of(), ImmutableSet.of(), PHSoundRegistry.PET_BED_USE.get());
    }
}

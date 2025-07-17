package com.github.yzqdev.pet_home.worldgen;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class PHVillagePieceRegistry {

    public static final DeferredRegister<StructurePoolElementType<?>> DEF_REG = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, PetHomeMod.MODID);

    public static final DeferredHolder<StructurePoolElementType<?>,StructurePoolElementType<PetshopStructurePoolElement>> PETSHOP = DEF_REG.register("petshop", () -> () -> PetshopStructurePoolElement.CODEC);

}

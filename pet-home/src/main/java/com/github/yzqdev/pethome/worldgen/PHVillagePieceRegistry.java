package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;


public class PHVillagePieceRegistry {

    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP = Registry.register(
            BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petshop"),
            () -> PetshopStructurePoolElement.CODEC);

    public static void init() {
    }
}

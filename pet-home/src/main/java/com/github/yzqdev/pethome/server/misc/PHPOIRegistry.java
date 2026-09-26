package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Set;


public class PHPOIRegistry {

    public static final Holder.Reference<PoiType> PET_BED = Registry.registerForHolder(BuiltInRegistries.POINT_OF_INTEREST_TYPE,
            ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_bed")),
            new PoiType(getBeds(), 1, 1));

    public static Set<net.minecraft.world.level.block.state.BlockState> getBeds() {
        return PHBlockRegistry.PET_BED_BLOCKS.values().stream().flatMap((petbed) -> {
            return petbed.getStateDefinition().getPossibleStates().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    public static void init() {
    }
}

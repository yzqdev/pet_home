package com.github.yzqdev.pet_home.server.misc;


import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.server.block.PHBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


import java.util.Set;

public class PHPOIRegistry {

    public static final DeferredRegister<PoiType> DEF_REG = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, PetHomeMod.MODID);
    public static final DeferredHolder<PoiType,PoiType> PET_BED = DEF_REG.register("pet_bed", () -> new PoiType(getBeds(), 1, 1));

    public static Set<BlockState> getBeds() {
        return  PHBlockRegistry.PET_BED_BLOCKS.values()  .stream().flatMap((petbed) -> {
            return petbed.get().getStateDefinition().getPossibleStates().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }
}

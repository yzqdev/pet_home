package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class PHPOIRegistry {

    public static final DeferredRegister<PoiType> DEF_REG = DeferredRegister.create(ForgeRegistries.POI_TYPES,
            PetHomeMod.MODID);
    public static final RegistryObject<PoiType> PET_BED = DEF_REG.register("pet_bed",
            () -> new PoiType(getBeds(), 1, 1));

    public static Set<BlockState> getBeds() {
        return ImmutableSet.of(PHBlockRegistry.WHITE_PET_BED.get(), PHBlockRegistry.ORANGE_PET_BED.get(),
                        PHBlockRegistry.MAGENTA_PET_BED.get(), PHBlockRegistry.LIGHT_BLUE_PET_BED.get(),
                        PHBlockRegistry.YELLOW_PET_BED.get(), PHBlockRegistry.LIME_PET_BED.get(),
                        PHBlockRegistry.PINK_PET_BED.get(), PHBlockRegistry.GRAY_PET_BED.get(),
                        PHBlockRegistry.LIGHT_GRAY_PET_BED.get(), PHBlockRegistry.CYAN_PET_BED.get(),
                        PHBlockRegistry.PURPLE_PET_BED.get(), PHBlockRegistry.BLUE_PET_BED.get(),
                        PHBlockRegistry.BROWN_PET_BED.get(), PHBlockRegistry.GREEN_PET_BED.get(),
                        PHBlockRegistry.RED_PET_BED.get(), PHBlockRegistry.BLACK_PET_BED.get())
                .stream()
                .flatMap((bedState) -> {
                    return bedState.getStateDefinition()
                            .getPossibleStates()
                            .stream();
                })
                .collect(ImmutableSet.toImmutableSet());
    }
}

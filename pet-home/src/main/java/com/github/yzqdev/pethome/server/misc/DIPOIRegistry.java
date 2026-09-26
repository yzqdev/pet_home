package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DIBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Set;

public class DIPOIRegistry {

    public static final ResourceLocation PET_BED_ID = new ResourceLocation(PetHomeMod.MODID, "pet_bed");

    /**
     * 用 registerForHolder 注册：登记「方块状态 -> POI」映射时必须传带 ResourceKey 的 Holder，
     * 否则村民职业的 {@code poiType.is(PET_BED_ID)} 判定会失败（Holder.Direct 无 key）。
     */
    public static final Holder.Reference<PoiType> PET_BED_HOLDER = Registry.registerForHolder(BuiltInRegistries.POINT_OF_INTEREST_TYPE,
            PET_BED_ID,
            new PoiType(getBeds(), 1, 1));

    public static final PoiType PET_BED = PET_BED_HOLDER.value();

    public static Set<BlockState> getBeds() {
        return ImmutableSet.of(DIBlockRegistry.WHITE_PET_BED, DIBlockRegistry.ORANGE_PET_BED,
                        DIBlockRegistry.MAGENTA_PET_BED, DIBlockRegistry.LIGHT_BLUE_PET_BED,
                        DIBlockRegistry.YELLOW_PET_BED, DIBlockRegistry.LIME_PET_BED,
                        DIBlockRegistry.PINK_PET_BED, DIBlockRegistry.GRAY_PET_BED,
                        DIBlockRegistry.LIGHT_GRAY_PET_BED, DIBlockRegistry.CYAN_PET_BED,
                        DIBlockRegistry.PURPLE_PET_BED, DIBlockRegistry.BLUE_PET_BED,
                        DIBlockRegistry.BROWN_PET_BED, DIBlockRegistry.GREEN_PET_BED,
                        DIBlockRegistry.RED_PET_BED, DIBlockRegistry.BLACK_PET_BED)
                .stream()
                .flatMap((p_27389_) -> {
                    return p_27389_.getStateDefinition()
                            .getPossibleStates()
                            .stream();
                })
                .collect(ImmutableSet.toImmutableSet());
    }

    public static void init() {
    }
}

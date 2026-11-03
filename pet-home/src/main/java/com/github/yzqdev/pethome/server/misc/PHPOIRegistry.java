package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Set;

/** 兴趣点（POI）注册：把 16 色宠物床登记为 POI，供驯兽师村民职业判定工作方块。 */
public class PHPOIRegistry {

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
        return ImmutableSet.of(PHBlockRegistry.WHITE_PET_BED, PHBlockRegistry.ORANGE_PET_BED,
                        PHBlockRegistry.MAGENTA_PET_BED, PHBlockRegistry.LIGHT_BLUE_PET_BED,
                        PHBlockRegistry.YELLOW_PET_BED, PHBlockRegistry.LIME_PET_BED,
                        PHBlockRegistry.PINK_PET_BED, PHBlockRegistry.GRAY_PET_BED,
                        PHBlockRegistry.LIGHT_GRAY_PET_BED, PHBlockRegistry.CYAN_PET_BED,
                        PHBlockRegistry.PURPLE_PET_BED, PHBlockRegistry.BLUE_PET_BED,
                        PHBlockRegistry.BROWN_PET_BED, PHBlockRegistry.GREEN_PET_BED,
                        PHBlockRegistry.RED_PET_BED, PHBlockRegistry.BLACK_PET_BED)
                .stream()
                .flatMap((bedState) -> {
                    return bedState.getStateDefinition()
                            .getPossibleStates()
                            .stream();
                })
                .collect(ImmutableSet.toImmutableSet());
    }

    public static void init() {
    }
}

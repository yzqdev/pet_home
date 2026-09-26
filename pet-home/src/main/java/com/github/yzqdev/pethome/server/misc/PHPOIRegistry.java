package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * 兴趣点（POI）注册：Fabric 原生 {@code Registry.register}。
 * 这里保留 {@link Holder.Reference}（{@code Registry.registerForHolder} 的返回值），因为
 * 村民职业判定与 datagen 标签都消费 {@code PoiType} 的 {@code ResourceKey}。
 */
public class PHPOIRegistry {

    public static final Holder.Reference<PoiType> PET_BED = Registry.registerForHolder(BuiltInRegistries.POINT_OF_INTEREST_TYPE,
            ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "pet_bed")),
            new PoiType(getBeds(), 1, 1));

    public static Set<BlockState> getBeds() {
        return PHBlockRegistry.PET_BED_BLOCKS.values().stream().flatMap((petbed) -> {
            return petbed.getStateDefinition().getPossibleStates().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    public static void init() {
    }
}

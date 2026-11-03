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


/**
 * 兴趣点（POI）注册：把全部 16 色宠物床方块状态注册为 {@code pet_bed} 兴趣点
 * （票证 1、搜索范围 1），村民可识别宠物床为可寻路目标。
 */
public class PHPOIRegistry {

    public static final Holder.Reference<PoiType> PET_BED = Registry.registerForHolder(BuiltInRegistries.POINT_OF_INTEREST_TYPE,
            ResourceKey.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "pet_bed")),
            new PoiType(getBeds(), 1, 1));

    /** 收集 16 色宠物床的全部方块状态（含朝向/含水变体）作为 POI 匹配集。 */
    public static Set<net.minecraft.world.level.block.state.BlockState> getBeds() {
        return PHBlockRegistry.PET_BED_BLOCKS.values().stream().flatMap((petbed) -> {
            return petbed.getStateDefinition().getPossibleStates().stream();
        }).collect(ImmutableSet.toImmutableSet());
    }

    /** 空实现：仅用于触发本类静态初始化（注册发生在这时）。 */
    public static void init() {
    }
}

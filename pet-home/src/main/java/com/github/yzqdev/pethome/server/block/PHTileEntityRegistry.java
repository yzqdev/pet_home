package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.stream.Collectors;


/**
 * 方块实体注册：宠物床（对应 16 个 {@code PET_BED_BLOCKS} 全集）、指挥鼓、往生灯笼三种，
 * Fabric 原生 {@code Registry.register}（立即注册）。
 */
public class PHTileEntityRegistry {

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final BlockEntityType<PetBedBlockEntity> PET_BED = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("pet_bed"),
            new BlockEntityType<>(PetBedBlockEntity::new, PHBlockRegistry.PET_BED_BLOCKS.values().stream()
                    .collect(Collectors.toSet())));
    public static final BlockEntityType<DrumBlockEntity> DRUM = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("drum"),
            new BlockEntityType<>(DrumBlockEntity::new, Set.of(PHBlockRegistry.DRUM)));
    public static final BlockEntityType<WaywardLanternBlockEntity> WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("wayward_lantern"),
            new BlockEntityType<>(WaywardLanternBlockEntity::new, Set.of(PHBlockRegistry.WAYWARD_LANTERN)));

    /** 空实现：仅用于触发本类静态初始化（注册发生在这时）。 */
    public static void init() {
    }
}

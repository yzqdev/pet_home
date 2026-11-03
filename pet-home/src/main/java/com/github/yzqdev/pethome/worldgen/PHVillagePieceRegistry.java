package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;


/**
 * 结构池元素类型注册：向原版 {@code STRUCTURE_POOL_ELEMENT} 注册表登记
 * {@code pet_home:petshop} 类型（宠物商店结构池元素的编解码入口），
 * {@code init()} 仅为触发类加载的空实现。
 */
public class PHVillagePieceRegistry {

    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP = Registry.register(
            BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
            Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petshop"),
            () -> PetshopStructurePoolElement.CODEC);

    public static void init() {
    }
}

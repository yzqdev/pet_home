package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

/**
 * 结构池元素类型注册：把 {@code pet_home:petshop}（{@code PetshopStructurePoolElement} 的 codec 工厂） 注册进原版 {@code
 * STRUCTURE_POOL_ELEMENT} 注册表，使宠物商店元素能被结构池 JSON/NBT 反序列化。 {@link #init} 为空实现，仅由 {@code
 * PetHomeMod.onInitialize} 调用以触发静态初始化。
 */
public class PHVillagePieceRegistry {

  public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP =
      Registry.register(
          BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petshop"),
          () -> PetshopStructurePoolElement.CODEC);

  public static void init() {}
}

package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * 村庄宠物商店注入：把 {@link PetshopStructurePoolElement}（5 个群系皮肤）按 {@code petstoreVillageWeight} 权重塞进原版 5
 * 种村庄的 {@code houses} 结构池。直接改写已加载 {@code StructureTemplatePool} 的 {@code templates/rawTemplates}
 * 字段（无反射，字段为 public 可写），注入发生在 {@code SERVER_STARTING}（见 PetHomeMod），对之后生成的所有村庄生效，已生成区块不受影响。
 */
public class VillageHouseManager {
  public static final List<ResourceLocation> VILLAGE_REPLACEMENT_POOLS =
      List.of(
          ResourceLocation.parse("minecraft:village/plains/houses"),
          ResourceLocation.parse("minecraft:village/desert/houses"),
          ResourceLocation.parse("minecraft:village/savanna/houses"),
          ResourceLocation.parse("minecraft:village/snowy/houses"),
          ResourceLocation.parse("minecraft:village/taiga/houses"));

  /** 往结构池追加元素 weight 份（同时维护 templates 与 rawTemplates 两份列表）；weight ≤ 0 或池为空时静默跳过，同一实例不会重复添加。 */
  public static StructureTemplatePool addToPool(
      StructureTemplatePool pool, StructurePoolElement element, int weight) {
    if (weight > 0 && pool != null) {
      ObjectArrayList<StructurePoolElement> templates = new ObjectArrayList<>(pool.templates);
      if (!templates.contains(element)) {
        for (int i = 0; i < weight; ++i) {
          templates.add(element);
        }

        List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList<>(pool.rawTemplates);
        rawTemplates.add(new Pair<>(element, weight));
        pool.templates = templates;
        pool.rawTemplates = rawTemplates;
        PetHomeMod.LOGGER.info("Added petshop to village structure pool {}", pool);
      }
    }

    return pool;
  }

  /**
   * 逐群系创建商店元素并注入对应村庄 {@code houses} 结构池（minecraft:village/各群系/houses）；由 {@code SERVER_STARTING}
   * 回调调用。单机 JVM 内可多次进入（退出世界重进），因此本方法必须幂等：元素列表每次现场构建（不落入静态集合）， {@link #addToPool} 以元素实例判重兜底。
   */
  public static void addAllHouses(RegistryAccess registryAccess) {
    int weight = PetHomeConfig.petstoreVillageWeight;
    for (String biome : List.of("plains", "desert", "savanna", "snowy", "taiga")) {
      StructurePoolElement petshop =
          new PetshopStructurePoolElement(
              ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, biome + "_petshop"),
              StructurePoolElement.EMPTY);
      ResourceLocation poolId = ResourceLocation.parse("minecraft:village/" + biome + "/houses");
      try {
        StructureTemplatePool pool =
            registryAccess
                .registryOrThrow(Registries.TEMPLATE_POOL)
                .getOptional(poolId)
                .orElse(null);
        if (pool != null) {
          addToPool(pool, petshop, weight);
        }
      } catch (Exception e) {
        PetHomeMod.LOGGER.error("Could not add petshop to village pool {}", poolId, e);
      }
    }
  }
}

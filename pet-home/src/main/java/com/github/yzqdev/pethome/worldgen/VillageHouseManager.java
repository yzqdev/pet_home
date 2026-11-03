package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 把宠物店元素注入五个生物群系村庄的 houses 池。{@link #addAllHouses} 由 SERVER_STARTING
 * （{@code ServerEvent.serverStart}）触发，单机 JVM 内可多次进入（退出世界重进），
 * 因此本方法必须幂等：元素列表每次现场构建（不落入静态集合），
 * {@link #addToPool} 以元素实例判重兜底。
 */
public class VillageHouseManager {
    public static final List<Identifier> VILLAGE_REPLACEMENT_POOLS = List.of(Identifier.parse("minecraft:village/plains/houses"), Identifier.parse("minecraft:village/desert/houses"), Identifier.parse("minecraft:village/savanna/houses"), Identifier.parse("minecraft:village/snowy/houses"), Identifier.parse("minecraft:village/taiga/houses"));

    private static final Holder<StructureProcessorList> EMPTY_PROCESSORS = Holder.direct(new StructureProcessorList(List.of()));



    /**
     * 反射向结构池追加元素：{@code weight} 份进 {@code templates}（运行时抽取表）、
     * 一条 {@code (element, weight)} 进 {@code rawTemplates}（序列化源）；已包含则跳过。
     */
    @SuppressWarnings("unchecked")
    public static StructureTemplatePool addToPool(StructureTemplatePool pool, StructurePoolElement element, int weight) {
        if (weight <= 0 || pool == null) {
            return pool;
        }
        try {
            Field templatesField = StructureTemplatePool.class.getDeclaredField("templates");
            Field rawTemplatesField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            templatesField.setAccessible(true);
            rawTemplatesField.setAccessible(true);

            ObjectArrayList<StructurePoolElement> templates =
                    new ObjectArrayList<>((List<StructurePoolElement>) templatesField.get(pool));
            if (templates.contains(element)) {
                return pool;
            }
            for (int i = 0; i < weight; ++i) {
                templates.add(element);
            }

            List<Pair<StructurePoolElement, Integer>> rawTemplates =
                    new ArrayList<>((List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool));
            rawTemplates.add(new Pair<>(element, weight));

            templatesField.set(pool, templates);
            rawTemplatesField.set(pool, rawTemplates);
            PetHomeMod.LOGGER.info("Added petshop to village structure pool {}", pool);
        } catch (ReflectiveOperationException e) {
            PetHomeMod.LOGGER.error("Failed to inject pet shop into village structure pool", e);
        }

        return pool;
    }

    /** 服务器启动期调用：为每个群系构造对应宠物商店元素并注入 {@code minecraft:village/<biome>/houses} 池（权重 0 时等效禁用）。 */
    public static void addAllHouses(RegistryAccess registryAccess) {
        int weight = PetHomeConfig.petstoreVillageWeight;
        for (String biome : List.of("plains", "desert", "savanna", "snowy", "taiga")) {
            StructurePoolElement petshop = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, biome + "_petshop"), EMPTY_PROCESSORS);
            Identifier poolId = Identifier.parse("minecraft:village/" + biome + "/houses");
            try {
                StructureTemplatePool pool = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL).getValue(poolId);
                if (pool != null) {
                    addToPool(pool, petshop, weight);
                }
            } catch (Exception e) {
                PetHomeMod.LOGGER.error("Could not add petshop to village pool {}", poolId, e);
            }
        }
    }
}

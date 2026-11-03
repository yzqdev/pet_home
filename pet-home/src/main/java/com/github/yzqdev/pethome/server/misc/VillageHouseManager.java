package com.github.yzqdev.pethome.server.misc;


import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 村庄结构池注入器：把注册的 {@code (结构池, 修改器)} 对在服务器启动时套用到五个生物群系村庄的
 * {@code village/<群系>/houses} 结构池。原版 {@code StructureTemplatePool} 的 templates / rawTemplates
 * 是私有的，经 {@code mixin/StructureTemplatePoolAccessor} 访问器写入。
 */
public class VillageHouseManager {
    public static final List<ResourceLocation> VILLAGE_REPLACEMENT_POOLS = List.of(
              new ResourceLocation("minecraft:village/plains/houses"),
            new ResourceLocation("minecraft:village/desert/houses"),
            new ResourceLocation("minecraft:village/savanna/houses"),
            new ResourceLocation("minecraft:village/snowy/houses"),
            new ResourceLocation("minecraft:village/taiga/houses"));
    private static final List<Pair<ResourceLocation, Consumer<StructureTemplatePool>>> REGISTRY = new ArrayList<>();

    public static void register(ResourceLocation pool, Consumer<StructureTemplatePool> addToPool) {
        REGISTRY.add(new Pair<>(pool, addToPool));
        PetHomeMod.LOGGER.debug("registered addition to pool: " + pool);
    }

    /** 向结构池按 weight 追加元素（幂等：已存在不重复加；weight<=0 忽略）。 */
    public static StructureTemplatePool addToPool(StructureTemplatePool pool, StructurePoolElement element, int weight) {
        if (weight > 0) {
            if (pool != null) {
                ObjectArrayList<StructurePoolElement> templates = new ObjectArrayList<>(((com.github.yzqdev.pethome.mixin.StructureTemplatePoolAccessor) (Object) pool).ph_getTemplates());
                if (!templates.contains(element)) {
                    for (int i = 0; i < weight; i++) {
                        templates.add(element);
                    }
                    List<Pair<StructurePoolElement, Integer>> rawTemplates = new ArrayList(((com.github.yzqdev.pethome.mixin.StructureTemplatePoolAccessor) (Object) pool).ph_getRawTemplates());
                    rawTemplates.add(new Pair<>(element, weight));
                    ((com.github.yzqdev.pethome.mixin.StructureTemplatePoolAccessor) (Object) pool).ph_setTemplates(templates);
                    ((com.github.yzqdev.pethome.mixin.StructureTemplatePoolAccessor) (Object) pool).ph_setRawTemplates(rawTemplates);
                    PetHomeMod.LOGGER.info("Added to village structure pool");
                }
            }
        }
        return pool;
    }

    /** 应用全部已注册的注入（由 {@code ServerEvent.serverStart} 调用；池缺失 / 异常仅记日志不致命）。 */
    public static void addAllHouses(RegistryAccess registryAccess) {
        try {
            for (ResourceLocation villagePool : VILLAGE_REPLACEMENT_POOLS) {
                StructureTemplatePool pool = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL).getOptional(villagePool).orElse(null);
                if (pool != null) {
                    for (Pair<ResourceLocation, Consumer<StructureTemplatePool>> pair : REGISTRY) {
                        if (villagePool.equals(pair.getFirst())) {
                            pair.getSecond().accept(pool);
                        }
                    }
                }
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.error("Could not add village houses!");
            e.printStackTrace();
        }
    }
}
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VillageHouseManager {
    public static final List<Identifier> VILLAGE_REPLACEMENT_POOLS = List.of(Identifier.parse("minecraft:village/plains/houses"), Identifier.parse("minecraft:village/desert/houses"), Identifier.parse("minecraft:village/savanna/houses"), Identifier.parse("minecraft:village/snowy/houses"), Identifier.parse("minecraft:village/taiga/houses"));
    private static final List<Pair<Identifier, Consumer<StructureTemplatePool>>> REGISTRY = new ArrayList<>();

    private static final Holder<StructureProcessorList> EMPTY_PROCESSORS = Holder.direct(new StructureProcessorList(List.of()));


    public static StructureTemplatePool addToPool(StructureTemplatePool pool, StructurePoolElement element, int weight) {
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
                PetHomeMod.LOGGER.info("Added to village structure pool");
            }
        }

        return pool;
    }

    public static void addAllHouses(RegistryAccess registryAccess) {
        int weight = PetHomeConfig.petstoreVillageWeight;
        StructurePoolElement plains = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "plains_petshop"), EMPTY_PROCESSORS);
        REGISTRY.add(new Pair<>(Identifier.parse("minecraft:village/plains/houses"), (pool) -> VillageHouseManager.addToPool(pool, plains, weight)));


        StructurePoolElement desert = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "desert_petshop"), EMPTY_PROCESSORS);
        REGISTRY.add(new Pair<>(Identifier.parse("minecraft:village/desert/houses"), (pool) -> VillageHouseManager.addToPool(pool, desert, weight)));
        StructurePoolElement savanna = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "savanna_petshop"), EMPTY_PROCESSORS);
        REGISTRY.add(new Pair<>(Identifier.parse("minecraft:village/savanna/houses"), (pool) -> VillageHouseManager.addToPool(pool, savanna, weight)));
        StructurePoolElement snowy = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "snowy_petshop"), EMPTY_PROCESSORS);
        REGISTRY.add(new Pair<>(Identifier.parse("minecraft:village/snowy/houses"), (pool) -> VillageHouseManager.addToPool(pool, snowy, weight)));
        StructurePoolElement taiga = new PetshopStructurePoolElement(Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "taiga_petshop"), EMPTY_PROCESSORS);


        REGISTRY.add(new Pair<>(Identifier.parse("minecraft:village/taiga/houses"), (pool) -> VillageHouseManager.addToPool(pool, taiga, weight)));
        try {
            for (Identifier villagePool : VILLAGE_REPLACEMENT_POOLS) {
                StructureTemplatePool pool = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL).getValue(villagePool);
                if (pool != null) {
                    for (Pair<Identifier, Consumer<StructureTemplatePool>> pair : REGISTRY) {
                        if (villagePool.equals(pair.getFirst())) {
                            pair.getSecond().accept(pool);

                        }
                    }
                }
            }
        } catch (Exception e) {
            PetHomeMod.LOGGER.error("Could not add village houses!");
            PetHomeMod.LOGGER.error(e.toString());
        }

    }
}

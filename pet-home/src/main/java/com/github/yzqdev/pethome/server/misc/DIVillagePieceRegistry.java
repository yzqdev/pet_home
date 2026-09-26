package com.github.yzqdev.pethome.server.misc;



import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

public class DIVillagePieceRegistry {


    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP = Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, new ResourceLocation(PetHomeMod.MODID, "petshop"), () -> PetshopStructurePoolElement.CODEC);

    public static void registerHouses() {
        int weight = 17;
        StructurePoolElement plains = new PetshopStructurePoolElement(new ResourceLocation(PetHomeMod.MODID, "plains_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/plains/houses"), (pool) -> VillageHouseManager.addToPool(pool, plains, weight));
        StructurePoolElement desert = new PetshopStructurePoolElement(new ResourceLocation(PetHomeMod.MODID, "desert_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/desert/houses"), (pool) -> VillageHouseManager.addToPool(pool, desert, weight));
        StructurePoolElement savanna = new PetshopStructurePoolElement(new ResourceLocation(PetHomeMod.MODID, "savanna_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/savanna/houses"), (pool) -> VillageHouseManager.addToPool(pool, savanna, weight));
        StructurePoolElement snowy = new PetshopStructurePoolElement(new ResourceLocation(PetHomeMod.MODID, "snowy_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/snowy/houses"), (pool) -> VillageHouseManager.addToPool(pool, snowy, weight));
        StructurePoolElement taiga = new PetshopStructurePoolElement( new ResourceLocation(PetHomeMod.MODID, "taiga_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(new ResourceLocation("minecraft:village/taiga/houses"), (pool) -> VillageHouseManager.addToPool(pool, taiga, weight));
    }


    public static void init() {
    }
}

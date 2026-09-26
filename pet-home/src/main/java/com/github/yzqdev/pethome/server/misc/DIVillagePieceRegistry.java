package com.github.yzqdev.pethome.server.misc;



import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DIVillagePieceRegistry {

    public static final DeferredRegister<StructurePoolElementType<?>> DEF_REG = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, PetHomeMod.MODID);

    public static final RegistryObject<StructurePoolElementType<PetshopStructurePoolElement>> PETSHOP = DEF_REG.register("petshop", () -> () -> PetshopStructurePoolElement.CODEC);

    public static void registerHouses() {
        // 权重在 lambda 执行时（服务器启动注入阶段）才从配置读取，配置值改动后重启生效；0 表示完全不注入
        StructurePoolElement plains = new PetshopStructurePoolElement(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "plains_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(ResourceLocation.parse("minecraft:village/plains/houses"), (pool) -> VillageHouseManager.addToPool(pool, plains, PetHomeMod.CONFIG.petstoreVillageWeight.get()));
        StructurePoolElement desert = new PetshopStructurePoolElement(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "desert_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(ResourceLocation.parse("minecraft:village/desert/houses"), (pool) -> VillageHouseManager.addToPool(pool, desert, PetHomeMod.CONFIG.petstoreVillageWeight.get()));
        StructurePoolElement savanna = new PetshopStructurePoolElement(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "savanna_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(ResourceLocation.parse("minecraft:village/savanna/houses"), (pool) -> VillageHouseManager.addToPool(pool, savanna, PetHomeMod.CONFIG.petstoreVillageWeight.get()));
        StructurePoolElement snowy = new PetshopStructurePoolElement(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "snowy_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(ResourceLocation.parse("minecraft:village/snowy/houses"), (pool) -> VillageHouseManager.addToPool(pool, snowy, PetHomeMod.CONFIG.petstoreVillageWeight.get()));
        StructurePoolElement taiga = new PetshopStructurePoolElement( ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "taiga_petshop"), StructurePoolElement.EMPTY);
        VillageHouseManager.register(ResourceLocation.parse("minecraft:village/taiga/houses"), (pool) -> VillageHouseManager.addToPool(pool, taiga, PetHomeMod.CONFIG.petstoreVillageWeight.get()));
    }

}

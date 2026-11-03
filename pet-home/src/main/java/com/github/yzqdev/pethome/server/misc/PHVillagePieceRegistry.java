package com.github.yzqdev.pethome.server.misc;



import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

/**
 * 结构池元素类型注册：{@code petshop} 类型注册到原版 {@code STRUCTURE_POOL_ELEMENT}；
 * {@link #registerHouses()} 把五个生物群系版本的宠物商店元素（各自指向手写结构 NBT，
 * 权重硬编码 17）登记到 {@link VillageHouseManager}。
 */
public class PHVillagePieceRegistry {


    public static final StructurePoolElementType<PetshopStructurePoolElement> PETSHOP = Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, new ResourceLocation(PetHomeMod.MODID, "petshop"), () -> PetshopStructurePoolElement.CODEC);

    /** 注册五生物群系（plains/desert/savanna/snowy/taiga）宠物商店到村庄 houses 结构池；由 {@code ServerEvent.serverStart} 调用。 */
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
        // 主线程预热宠物店刷怪列表：结构生成发生在 worldgen worker 线程，惰性初始化在多线程下不安全
        PetshopStructurePoolElement.preloadMobLists();
    }


    public static void init() {
    }
}

package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeMod;
// 迁移说明：原来引用的 datagen/loot/LootTableGen 仍是 NeoForge 实现（暂时排除编译），
// 这里直接内联同一张战利品表的 ResourceKey，避免 worldgen 依赖数据生成代码。
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseCoralPlantTypeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * 宠物商店结构池元素（注册为 {@code petshop} 类型）：生成时处理结构模板里的数据标记方块——
 * {@code petshop_water}（鱼缸：水/海草/珊瑚 + {@code petstore_fishtank} 标签动物）、
 * {@code petshop_cage_0..3}（笼子：按 {@code petstore_cage_*} 标签随机刷动物）、
 * {@code petshop_chest}（挂 {@code pet_home:chests/petshop_chest} 战利品表）。
 * 动物名单按标签懒初始化一次（静态缓存）。
 */
public class PetshopStructurePoolElement extends LegacySinglePoolElement {

    public static final Identifier FISHTANK_MOBS = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_fishtank");
    public static final Identifier CAGE_0_MOBS = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_0");
    public static final Identifier CAGE_1_MOBS = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_1");
    public static final Identifier CAGE_2_MOBS = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_2");
    public static final Identifier CAGE_3_MOBS = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_3");
    private static boolean initializedMobLists = false;
    private static EntityType[] fishtankMobs = null;
    private static EntityType[] cage0Mobs = null;
    private static EntityType[] cage1Mobs = null;
    private static EntityType[] cage2Mobs = null;
    private static EntityType[] cage3Mobs = null;

    /**
     * 在服务器启动（主线程）预热刷怪列表：handleDataMarker 运行在世界生成 worker 线程，
     * 惰性初始化在多线程下无同步不安全；此方法保证进入生成阶段前缓存已就绪，
     * handleDataMarker 内的判空只是兜底。
     */
    public static void preloadMobLists() {
        if (!initializedMobLists) {
            synchronized (PetshopStructurePoolElement.class) {
                if (!initializedMobLists) {
                    fishtankMobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_FISHTANK).toArray(EntityType[]::new);
                    cage0Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_0).toArray(EntityType[]::new);
                    cage1Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_1).toArray(EntityType[]::new);
                    cage2Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_2).toArray(EntityType[]::new);
                    cage3Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_3).toArray(EntityType[]::new);
                    initializedMobLists = true;
                }
            }
        }
    }

    public static final MapCodec<PetshopStructurePoolElement> CODEC = RecordCodecBuilder.mapCodec((petshopStructurePoolElementInstance) -> {
        return petshopStructurePoolElementInstance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec()).apply(petshopStructurePoolElementInstance, PetshopStructurePoolElement::new);
    });

    protected PetshopStructurePoolElement(Either<Identifier, StructureTemplate> either, Holder<StructureProcessorList> structureProcessorListHolder, StructureTemplatePool.Projection projection, Optional<LiquidSettings> liquidSettings) {
        super(either, structureProcessorListHolder, projection, liquidSettings);
    }

    public PetshopStructurePoolElement(Identifier resourceLocation, Holder<StructureProcessorList> processors) {
        super(Either.left(resourceLocation), processors, StructureTemplatePool.Projection.RIGID, Optional.empty());
    }

    /** 结构生成期逐标记方块回调：按 metadata 分派鱼缸/笼子/箱子；同时把标记方块本身置掉（水/珊瑚/空气）。 */
    @Override
    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos pos, Rotation rotation, RandomSource random, BoundingBox box) {
        String contents = structureBlockInfo.nbt().getStringOr("metadata", "");

        preloadMobLists();
        switch (contents) {
            case "petshop_water" -> {
                BlockState state = Blocks.WATER.defaultBlockState();
                float f = random.nextFloat();
                if (f < 0.5F) {
                    state = Blocks.SEAGRASS.defaultBlockState();
                } else if (f < 0.75F) {
                    Block coralBlock = switch (random.nextInt(5)) {
                        case 1 -> Blocks.TUBE_CORAL;
                        case 2 -> Blocks.BRAIN_CORAL;
                        case 3 -> Blocks.BUBBLE_CORAL;
                        case 4 -> Blocks.FIRE_CORAL;
                        default -> Blocks.HORN_CORAL;
                    };
                    state = coralBlock.defaultBlockState().setValue(BaseCoralPlantTypeBlock.WATERLOGGED, true);
                }
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 2, random, fishtankMobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), state, 2);
            }
            case "petshop_chest" -> {
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);

                RandomizableContainer.setBlockEntityLootTable(levelAccessor, random, structureBlockInfo.pos().below(),
                        net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                                net.minecraft.resources.Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "chests/petshop_chest")));
            }
            case "petshop_cage_0" -> {
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1 + random.nextInt(2), random, cage0Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 4);
            }
            case "petshop_cage_1" -> {
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 2 + random.nextInt(2), random, cage1Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
            }
            case "petshop_cage_2" -> {
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1 + random.nextInt(2), random, cage2Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
            }
            case "petshop_cage_3" -> {
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1, random, cage3Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
            }
            default -> throw new IllegalStateException("Unexpected value: " + contents);
        }
    }

    private static List<EntityType<?>> getAllMatchingEntities(TagKey<EntityType<?>> tag) {
        return BuiltInRegistries.ENTITY_TYPE.stream().filter((type -> type.builtInRegistryHolder().is(tag))).toList();
    }

    /** 在标记位置刷 {@code count} 只随机动物（结构方块占位校验 + 仅服务端），Mob 设置持久化并 finalizeSpawn。 */
    public void spawnAnimalsAt(LevelAccessor accessor, BlockPos at, int count, RandomSource random, EntityType... types) {
        if (types.length > 0 && count > 0 && accessor.getBlockState(at).getBlock() == Blocks.STRUCTURE_BLOCK && accessor instanceof ServerLevelAccessor serverLevel) {
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(types.length);
                Entity entity = types[index].create(serverLevel.getLevel(), EntitySpawnReason.STRUCTURE);
                entity.setPos(Vec3.atBottomCenterOf(at));
                entity.setYRot(random.nextInt(360) - 180);
                entity.setXRot(random.nextInt(360) - 180);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.STRUCTURE, null);
                }
                serverLevel.addFreshEntityWithPassengers(entity);
            }
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean should) {

        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(boundingBox);
        structureplacesettings.setRotation(rotation);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setLiquidSettings(this.overrideLiquidSettings.orElse(liquidSettings));
        structureplacesettings.setIgnoreEntities(false);
        structureplacesettings.setFinalizeEntities(true);
        if (!should) {
            structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        this.processors.value().list().forEach(structureplacesettings::addProcessor);
        this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
        return structureplacesettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return PHVillagePieceRegistry.PETSHOP;
    }

    @Override
    public String toString() {
        return "PetShop[" + this.template + "]";
    }
}

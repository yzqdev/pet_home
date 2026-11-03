package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * 宠物商店结构池元素（{@code LegacySinglePoolElement} 扩展）：结构内以「结构方块 + metadata」
 * 作数据标记，生成时按 {@link #handleDataMarker} 把标记替换为鱼缸水体 / 带战利品的商店箱子 /
 * 按实体标签（{@code petstore_fishtank}、{@code petstore_cage_0..3}）随机刷出的待售动物。
 */
public class PetshopStructurePoolElement extends LegacySinglePoolElement {

    public static final ResourceLocation CHEST = new ResourceLocation(PetHomeMod.MODID, "chests/petshop_chest");
    public static final ResourceLocation FISHTANK_MOBS = new ResourceLocation(PetHomeMod.MODID, "petstore_fishtank");
    public static final ResourceLocation CAGE_0_MOBS = new ResourceLocation(PetHomeMod.MODID, "petstore_cage_0");
    public static final ResourceLocation CAGE_1_MOBS = new ResourceLocation(PetHomeMod.MODID, "petstore_cage_1");
    public static final ResourceLocation CAGE_2_MOBS = new ResourceLocation(PetHomeMod.MODID, "petstore_cage_2");
    public static final ResourceLocation CAGE_3_MOBS = new ResourceLocation(PetHomeMod.MODID, "petstore_cage_3");
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
                    fishtankMobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_FISHTANK).toArray(new EntityType[0]);
                    cage0Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_0).toArray(new EntityType[0]);
                    cage1Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_1).toArray(new EntityType[0]);
                    cage2Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_2).toArray(new EntityType[0]);
                    cage3Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_3).toArray(new EntityType[0]);
                    initializedMobLists = true;
                }
            }
        }
    }

    public static final Codec<PetshopStructurePoolElement> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(instance, PetshopStructurePoolElement::new);
    });

    protected PetshopStructurePoolElement(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> processors, StructureTemplatePool.Projection projection) {
        super(either, processors, projection);
    }

    public PetshopStructurePoolElement(ResourceLocation resourceLocation, Holder<StructureProcessorList> processors) {
        super(Either.left(resourceLocation), processors, StructureTemplatePool.Projection.RIGID);
    }

    /** 数据标记处理：petshop_water=鱼缸（水体 + 水生动物）、petshop_chest=商店箱子、petshop_cage_0..3=四类笼子刷动物；标签实体列表首次使用时收集一次。 */
    @Override
    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos pos, Rotation rotation, RandomSource random, BoundingBox box) {
        String contents = structureBlockInfo.nbt().getString("metadata");
        preloadMobLists();
        switch (contents) {
            case "petshop_water":
                BlockState state = Blocks.WATER.defaultBlockState();
                float f = random.nextFloat();
                if (f < 0.5F) {
                    state = Blocks.SEAGRASS.defaultBlockState();
                } else if (f < 0.75F) {
                    Block coralBlock;
                    switch (random.nextInt(5)) {
                        case 1:
                            coralBlock = Blocks.TUBE_CORAL;
                            break;
                        case 2:
                            coralBlock = Blocks.BRAIN_CORAL;
                            break;
                        case 3:
                            coralBlock = Blocks.BUBBLE_CORAL;
                            break;
                        case 4:
                            coralBlock = Blocks.FIRE_CORAL;
                            break;
                        default:
                            coralBlock = Blocks.HORN_CORAL;
                            break;
                    }
                    state = coralBlock.defaultBlockState().setValue(BaseCoralPlantTypeBlock.WATERLOGGED, true);
                }
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 2,  random, fishtankMobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), state, 2);
                break;
            case "petshop_chest":
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
                RandomizableContainerBlockEntity.setLootTable(levelAccessor, random, structureBlockInfo.pos().below(), CHEST);
                break;
            case "petshop_cage_0"://wolf, rabbit or cat
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1 + random.nextInt(2), random, cage0Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 4);
                break;
            case "petshop_cage_1"://desert terrarium
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 2 + random.nextInt(2), random, cage1Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
                break;
            case "petshop_cage_2"://ice terrarium
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1 + random.nextInt(2), random, cage2Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
                break;
            case "petshop_cage_3"://parrot
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 1, random, cage3Mobs);
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);
                break;
        }
    }

    private static List<EntityType<?>> getAllMatchingEntities(TagKey<EntityType<?>> tag) {
       return BuiltInRegistries.ENTITY_TYPE.stream().collect(java.util.stream.Collectors.toList()).stream().filter((type -> type.is(tag))).toList();
    }

    /** 在结构方块位置随机刷 count 只动物（仅服务端、仅当目标位置仍是结构方块），Mob 附上 persistenceRequired 并走 STRUCTURE finalizeSpawn。 */
    public void spawnAnimalsAt(LevelAccessor accessor, BlockPos at, int count, RandomSource random, EntityType... types) {
        if (types.length > 0 && count > 0 && accessor.getBlockState(at).getBlock() == Blocks.STRUCTURE_BLOCK && accessor instanceof ServerLevelAccessor serverLevel) {
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(types.length);
                Entity entity = types[index].create(serverLevel.getLevel());
                entity.setPos(Vec3.atBottomCenterOf(at));
                entity.setYRot(random.nextInt(360) - 180);
                entity.setXRot(random.nextInt(360) - 180);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                }
                serverLevel.addFreshEntityWithPassengers(entity);
            }
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean keepJigsaws) {
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(boundingBox);
        structureplacesettings.setRotation(rotation);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setIgnoreEntities(false);
        structureplacesettings.setFinalizeEntities(true);
        if (!keepJigsaws) {
            structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        this.processors.value().list().forEach(structureplacesettings::addProcessor);
        this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
        return structureplacesettings;
    }

    public StructurePoolElementType<?> getType() {
        return PHVillagePieceRegistry.PETSHOP;
    }

    public String toString() {
        return "PetShop[" + this.template + "]";
    }
}

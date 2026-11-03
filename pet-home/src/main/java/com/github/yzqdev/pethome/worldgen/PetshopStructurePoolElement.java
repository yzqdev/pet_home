package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.datagen.loot.LootTableGen;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetshopStructurePoolElement extends LegacySinglePoolElement {

    /** 五组宠物店刷怪的不可变快照，经单一 volatile 引用发布（record 的 final 字段保证安全发布）。 */
    private record MobLists(EntityType<?>[] fishtank, EntityType<?>[] cage0, EntityType<?>[] cage1, EntityType<?>[] cage2, EntityType<?>[] cage3) {}

    private static volatile MobLists mobLists;

    /**
     * 在服务器启动（主线程）预热刷怪列表：handleDataMarker 运行在世界生成 worker 线程，
     * 此方法保证进入生成阶段前缓存已就绪；volatile 引用 + 不可变 record 使双检锁安全，
     * handleDataMarker 内的判空只是兜底。
     */
    public static void preloadMobLists() {
        if (mobLists == null) {
            synchronized (PetshopStructurePoolElement.class) {
                if (mobLists == null) {
                    mobLists = new MobLists(
                            entitiesIn(PHTagRegistry.PETSTORE_FISHTANK),
                            entitiesIn(PHTagRegistry.PETSTORE_CAGE_0),
                            entitiesIn(PHTagRegistry.PETSTORE_CAGE_1),
                            entitiesIn(PHTagRegistry.PETSTORE_CAGE_2),
                            entitiesIn(PHTagRegistry.PETSTORE_CAGE_3));
                }
            }
        }
    }

    public static final MapCodec<PetshopStructurePoolElement> CODEC = RecordCodecBuilder.mapCodec((petshopStructurePoolElementInstance) -> {
        return petshopStructurePoolElementInstance.group(templateCodec(), processorsCodec(), projectionCodec(), overrideLiquidSettingsCodec()).apply(petshopStructurePoolElementInstance, PetshopStructurePoolElement::new);
    });

    protected PetshopStructurePoolElement(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> structureProcessorListHolder, StructureTemplatePool.Projection projection, Optional<LiquidSettings> liquidSettings) {
        super(either, structureProcessorListHolder, projection, liquidSettings);
    }

    public PetshopStructurePoolElement(ResourceLocation resourceLocation, Holder<StructureProcessorList> processors) {
        super(Either.left(resourceLocation), processors, StructureTemplatePool.Projection.RIGID, Optional.empty());
    }

    @Override
    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos pos, Rotation rotation, RandomSource random, BoundingBox box) {
        String contents = structureBlockInfo.nbt().getString("metadata");

        preloadMobLists();
        MobLists lists = mobLists;
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
                spawnAnimalsAt(levelAccessor, structureBlockInfo.pos(), 2, random, lists.fishtank());
                levelAccessor.setBlock(structureBlockInfo.pos(), state, 2);
            }
            case "petshop_chest" -> {
                levelAccessor.setBlock(structureBlockInfo.pos(), Blocks.AIR.defaultBlockState(), 2);

                RandomizableContainer.setBlockEntityLootTable(levelAccessor, random, structureBlockInfo.pos().below(), LootTableGen.PET_LOOT_TABLE);
            }
            case "petshop_cage_0" -> {
                spawnCage(levelAccessor, structureBlockInfo, 1 + random.nextInt(2), random, lists.cage0());
            }
            case "petshop_cage_1" -> {
                spawnCage(levelAccessor, structureBlockInfo, 2 + random.nextInt(2), random, lists.cage1());
            }
            case "petshop_cage_2" -> {
                spawnCage(levelAccessor, structureBlockInfo, 1 + random.nextInt(2), random, lists.cage2());
            }
            case "petshop_cage_3" -> {
                spawnCage(levelAccessor, structureBlockInfo, 1, random, lists.cage3());
            }
            default -> throw new IllegalStateException("Unexpected value: " + contents);
        }
    }

    /** 笼子标记块通用处理：随机刷 {@code count} 只 {@code types} 里的动物，随后把标记块置空气。 */
    private static void spawnCage(LevelAccessor level, StructureTemplate.StructureBlockInfo info, int count, RandomSource random, EntityType<?>[] types) {
        spawnAnimalsAt(level, info.pos(), count, random, types);
        level.setBlock(info.pos(), Blocks.AIR.defaultBlockState(), 2);
    }

    private static EntityType<?>[] entitiesIn(TagKey<EntityType<?>> tag) {
        List<EntityType<?>> types = new ArrayList<>();
        for (Holder<EntityType<?>> holder : BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(tag)) {
            types.add(holder.value());
        }
        return types.toArray(EntityType<?>[]::new);
    }

    /** 在标记块位置随机刷 {@code count} 只 {@code types} 里的动物（Mob 设持久化 + finalizeSpawn），仅服务端且标记块仍为 structure_block 时执行；标签含不可生成实体时跳过该只（{@code create} 可返回 null）。 */
    private static void spawnAnimalsAt(LevelAccessor accessor, BlockPos at, int count, RandomSource random, EntityType<?>[] types) {
        if (types.length > 0 && count > 0 && accessor.getBlockState(at).getBlock() == Blocks.STRUCTURE_BLOCK && accessor instanceof ServerLevelAccessor serverLevel) {
            for (int i = 0; i < count; i++) {
                EntityType<?> type = types[random.nextInt(types.length)];
                Entity entity = type.create(serverLevel.getLevel());
                if (entity == null) {
                    continue;
                }
                entity.setPos(Vec3.atBottomCenterOf(at));
                entity.setYRot(random.nextFloat() * 360.0F);
                entity.setXRot(0.0F);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, null);
                }
                serverLevel.addFreshEntityWithPassengers(entity);
            }
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings liquidSettings, boolean keepJigsaws) {

        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(boundingBox);
        structureplacesettings.setRotation(rotation);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setLiquidSettings(this.overrideLiquidSettings.orElse(liquidSettings));
        structureplacesettings.setIgnoreEntities(false);
        structureplacesettings.setFinalizeEntities(true);
        if (!keepJigsaws) {
            structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        this.processors.value().list().forEach(structureplacesettings::addProcessor);
        this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
        return structureplacesettings;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return PHVillagePieceRegistry.PETSHOP.get();
    }

    @Override
    public String toString() {
        return "PetShop[" + this.template + "]";
    }
}

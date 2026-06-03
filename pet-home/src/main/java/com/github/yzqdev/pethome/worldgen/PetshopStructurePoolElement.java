package com.github.yzqdev.pethome.worldgen;

import com.github.yzqdev.pethome.PetHomeMod;
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

import java.util.List;
import java.util.Optional;

public class PetshopStructurePoolElement extends LegacySinglePoolElement {

    public static final ResourceLocation FISHTANK_MOBS = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_fishtank");
    public static final ResourceLocation CAGE_0_MOBS = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_0");
    public static final ResourceLocation CAGE_1_MOBS = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_1");
    public static final ResourceLocation CAGE_2_MOBS = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_2");
    public static final ResourceLocation CAGE_3_MOBS = ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "petstore_cage_3");
    private static boolean initializedMobLists = false;
    private static EntityType[] fishtankMobs = null;
    private static EntityType[] cage0Mobs = null;
    private static EntityType[] cage1Mobs = null;
    private static EntityType[] cage2Mobs = null;
    private static EntityType[] cage3Mobs = null;

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

        if (!initializedMobLists) {
            fishtankMobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_FISHTANK).toArray(new EntityType[0]);
            cage0Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_0).toArray(EntityType[]::new);
            cage1Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_1).toArray(EntityType[]::new);
            cage2Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_2).toArray(EntityType[]::new);
            cage3Mobs = getAllMatchingEntities(PHTagRegistry.PETSTORE_CAGE_3).toArray(EntityType[]::new);
            initializedMobLists = true;
        }
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

                RandomizableContainer.setBlockEntityLootTable(levelAccessor, random, structureBlockInfo.pos().below(), LootTableGen.PET_LOOT_TABLE);
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

    private List<EntityType<?>> getAllMatchingEntities(TagKey<EntityType<?>> tag) {
        return BuiltInRegistries.ENTITY_TYPE.stream().filter((type -> type.is(tag))).toList();
    }

    public void spawnAnimalsAt(LevelAccessor accessor, BlockPos at, int count, RandomSource random, EntityType... types) {
        if (types.length > 0 && count > 0 && accessor.getBlockState(at).getBlock() == Blocks.STRUCTURE_BLOCK && accessor instanceof ServerLevelAccessor serverLevel) {
            for (int i = 0; i < count; i++) {
                int index = types.length == 1 ? 0 : random.nextInt(types.length - 1);
                Entity entity = types[index].create(serverLevel.getLevel());
                entity.setPos(Vec3.atBottomCenterOf(at));
                entity.setYRot(random.nextInt(360) - 180);
                entity.setXRot(random.nextInt(360) - 180);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, null);
                }
                serverLevel.addFreshEntityWithPassengers(entity);
            }
        }
    }

    @Override
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, LiquidSettings p_352069_, boolean should) {

        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
        structureplacesettings.setBoundingBox(boundingBox);
        structureplacesettings.setRotation(rotation);
        structureplacesettings.setKnownShape(true);
        structureplacesettings.setLiquidSettings(p_352069_);
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
        return PHVillagePieceRegistry.PETSHOP.get();
    }

    @Override
    public String toString() {
        return "PetShop[" + this.template + "]";
    }
}

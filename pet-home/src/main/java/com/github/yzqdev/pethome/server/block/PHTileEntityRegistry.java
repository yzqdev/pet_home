package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** 方块实体类型注册：Fabric 原生 {@code Registry.register}，每个类型绑定其有效方块集合。 */
public class PHTileEntityRegistry {

    /** 宠物床方块实体（覆盖全部 16 色）：按主人分桶保存复活请求队列。 */
    public static final BlockEntityType<PetBedBlockEntity> PET_BED = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "pet_bed"),
            build(BlockEntityType.Builder.of(PetBedBlockEntity::new,
                    PHBlockRegistry.WHITE_PET_BED, PHBlockRegistry.ORANGE_PET_BED, PHBlockRegistry.MAGENTA_PET_BED, PHBlockRegistry.LIGHT_BLUE_PET_BED, PHBlockRegistry.YELLOW_PET_BED, PHBlockRegistry.LIME_PET_BED, PHBlockRegistry.PINK_PET_BED, PHBlockRegistry.GRAY_PET_BED, PHBlockRegistry.LIGHT_GRAY_PET_BED, PHBlockRegistry.CYAN_PET_BED, PHBlockRegistry.PURPLE_PET_BED, PHBlockRegistry.BLUE_PET_BED, PHBlockRegistry.BROWN_PET_BED, PHBlockRegistry.GREEN_PET_BED, PHBlockRegistry.RED_PET_BED, PHBlockRegistry.BLACK_PET_BED
            )));
    /** 指令鼓方块实体：保存当前指令档位与红石触发状态。 */
    public static final BlockEntityType<DrumBlockEntity> DRUM = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "drum"),
            build(BlockEntityType.Builder.of(DrumBlockEntity::new,
                    PHBlockRegistry.DRUM
            )));

    /** 往生灯笼方块实体：保存被收容宠物的数据与召回请求。 */
    public static final BlockEntityType<WaywardLanternBlockEntity> WAYWARD_LANTERN = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(PetHomeMod.MODID, "wayward_lantern"),
            build(BlockEntityType.Builder.of(WaywardLanternBlockEntity::new,
                    PHBlockRegistry.WAYWARD_LANTERN
            )));

    /** 统一收口 {@code BlockEntityType.Builder#build}（datafixer 传 null）。 */
    public static BlockEntityType build(BlockEntityType.Builder builder) {
        return builder.build(null);
    }

    public static void init() {
    }
}

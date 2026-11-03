package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 方块状态与方块模型 datagen：指令鼓 command 属性三变体（游走/停留/跟随，POWERED 仅红石
 * 逻辑无独立模型）；16 色宠物床色座模型（继承静态母模 pet_bed，只覆写 bed 槽贴图）+ FACING
 * 四向旋转；往生灯笼单变体（12 元素模型静态手写）。宠物床/灯笼的 BlockItem 物品模型由
 * blockstates 的 models/block 文件经原版 auto-fallback 承接。
 */
public class PHBlockStateProvider extends BlockStateProvider {

    public PHBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PetHomeMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var wander = models().cubeBottomTop("drum_wander", modLoc("block/drum_side"), modLoc("block/drum_bottom"), modLoc("block/drum_wander"));
        var stay = models().cubeBottomTop("drum_stay", modLoc("block/drum_side"), modLoc("block/drum_bottom"), modLoc("block/drum_stay"));
        var follow = models().cubeBottomTop("drum_follow", modLoc("block/drum_side"), modLoc("block/drum_bottom"), modLoc("block/drum_follow"));

        getVariantBuilder(PHBlockRegistry.DRUM.get())
                .partialState().with(DrumBlock.COMMAND, 0).modelForState().modelFile(wander).addModel()
                .partialState().with(DrumBlock.COMMAND, 1).modelForState().modelFile(stay).addModel()
                .partialState().with(DrumBlock.COMMAND, 2).modelForState().modelFile(follow).addModel();

        // 宠物床：色座模型（母模 pet_bed 静态手写，textures.bed 覆写）+ FACING 四向旋转
        java.util.Map<String, Block> petBeds = new java.util.HashMap<>();
        petBeds.put("white", PHBlockRegistry.WHITE_PET_BED.get());
        petBeds.put("orange", PHBlockRegistry.ORANGE_PET_BED.get());
        petBeds.put("magenta", PHBlockRegistry.MAGENTA_PET_BED.get());
        petBeds.put("light_blue", PHBlockRegistry.LIGHT_BLUE_PET_BED.get());
        petBeds.put("yellow", PHBlockRegistry.YELLOW_PET_BED.get());
        petBeds.put("lime", PHBlockRegistry.LIME_PET_BED.get());
        petBeds.put("pink", PHBlockRegistry.PINK_PET_BED.get());
        petBeds.put("gray", PHBlockRegistry.GRAY_PET_BED.get());
        petBeds.put("light_gray", PHBlockRegistry.LIGHT_GRAY_PET_BED.get());
        petBeds.put("cyan", PHBlockRegistry.CYAN_PET_BED.get());
        petBeds.put("purple", PHBlockRegistry.PURPLE_PET_BED.get());
        petBeds.put("blue", PHBlockRegistry.BLUE_PET_BED.get());
        petBeds.put("brown", PHBlockRegistry.BROWN_PET_BED.get());
        petBeds.put("green", PHBlockRegistry.GREEN_PET_BED.get());
        petBeds.put("red", PHBlockRegistry.RED_PET_BED.get());
        petBeds.put("black", PHBlockRegistry.BLACK_PET_BED.get());
        var facing = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
        for (var entry : petBeds.entrySet()) {
            String name = "pet_bed_" + entry.getKey();
            Block bed = entry.getValue();
            var seat = models().withExistingParent(name, modLoc("block/pet_bed"))
                    .texture("bed", modLoc("block/pet_bed_seat_" + entry.getKey()));
            getVariantBuilder(bed)
                    .partialState().with(facing, net.minecraft.core.Direction.NORTH)
                    .modelForState().modelFile(seat).addModel()
                    .partialState().with(facing, net.minecraft.core.Direction.EAST)
                    .modelForState().modelFile(seat).rotationY(90).addModel()
                    .partialState().with(facing, net.minecraft.core.Direction.SOUTH)
                    .modelForState().modelFile(seat).rotationY(180).addModel()
                    .partialState().with(facing, net.minecraft.core.Direction.WEST)
                    .modelForState().modelFile(seat).rotationY(270).addModel();
        }

        // 往生灯笼：单变体（模型为静态手写 12 元素）
        simpleBlock(PHBlockRegistry.WAYWARD_LANTERN.get(),
                new net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile(modLoc("block/wayward_lantern")));
    }
}

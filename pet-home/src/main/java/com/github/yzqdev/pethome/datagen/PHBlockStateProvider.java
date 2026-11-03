package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.DyeColors;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PetBedBlock;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 方块状态与方块模型 datagen：指令鼓 command 属性三变体（游走/停留/跟随，POWERED 仅红石
 * 逻辑无独立模型）；16 色宠物床色座模型（继承静态母模 pet_bed，只覆写 bed 槽贴图）+ FACING
 * 四向旋转；往生灯笼单变体（12 元素模型静态手写）。宠物床/灯笼的 BlockItem 物品模型定义由
 * NeoForge ItemModelProvider 体系外的原版 auto-fallback 覆盖（模型存在即可），本 provider
 * 只补 models/item 下的引用文件。
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
        for (var color : DyeColors.COLORS.keySet()) {
            String name = "pet_bed_" + color.name().toLowerCase();
            Block bed = PHBlockRegistry.PET_BED_BLOCKS.get(color).get();
            var seat = models().withExistingParent(name, modLoc("block/pet_bed"))
                    .texture("bed", modLoc("block/pet_bed_seat_" + color.name().toLowerCase()));
            getVariantBuilder(bed)
                    .partialState().with(PetBedBlock.FACING, net.minecraft.core.Direction.NORTH)
                    .modelForState().modelFile(seat).addModel()
                    .partialState().with(PetBedBlock.FACING, net.minecraft.core.Direction.EAST)
                    .modelForState().modelFile(seat).rotationY(90).addModel()
                    .partialState().with(PetBedBlock.FACING, net.minecraft.core.Direction.SOUTH)
                    .modelForState().modelFile(seat).rotationY(180).addModel()
                    .partialState().with(PetBedBlock.FACING, net.minecraft.core.Direction.WEST)
                    .modelForState().modelFile(seat).rotationY(270).addModel();
        }

        // 往生灯笼：单变体（模型为静态手写 12 元素）
        simpleBlock(PHBlockRegistry.WAYWARD_LANTERN.get(),
                new net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile(modLoc("block/wayward_lantern")));
    }
}

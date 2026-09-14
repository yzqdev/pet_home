package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.block.DrumBlock;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * 指挥鼓的 blockstate 与方块模型：command 属性三变体（游走/停留/跟随）。
 * POWERED 仅用于红石触发逻辑，无独立模型。
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
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
    }
}

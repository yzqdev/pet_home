package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.NbtKeys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * 指挥鼓方块实体：只负责持久化放置者 UUID（DrumBlock 右键时记录，issueCommand 广播三态指令时用于确定"鼓的主人"）。
 */
public class DrumBlockEntity extends BlockEntity {

    private UUID placerUUID;

    public DrumBlockEntity(BlockPos pos, BlockState state) {
        super(PHTileEntityRegistry.DRUM.get(), pos, state);
    }

    public UUID getPlacerUUID() {
        return placerUUID;
    }

    public void setPlacerUUID(UUID placerUUID) {
        this.placerUUID = placerUUID;
    }


    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.placerUUID = input.read(NbtKeys.PLACER_UUID, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.placerUUID != null) {
            output.store(NbtKeys.PLACER_UUID, UUIDUtil.CODEC, placerUUID);
        }
    }
}

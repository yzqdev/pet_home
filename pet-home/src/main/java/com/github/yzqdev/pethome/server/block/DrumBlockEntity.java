package com.github.yzqdev.pethome.server.block;

import com.github.yzqdev.pethome.server.NbtKeys;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 指挥鼓方块实体：只持久化放置者 UUID（{@code NbtKeys.PLACER_UUID}），供红石触发路径确定指令归属。 */
public class DrumBlockEntity extends BlockEntity {

  private UUID placerUUID;

  public DrumBlockEntity(BlockPos pos, BlockState state) {
    super(PHTileEntityRegistry.DRUM, pos, state);
  }

  public UUID getPlacerUUID() {
    return placerUUID;
  }

  public void setPlacerUUID(UUID placerUUID) {
    this.placerUUID = placerUUID;
  }

  @Override
  protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.loadAdditional(compound, registries);
    if (compound.hasUUID(NbtKeys.PLACER_UUID)) {
      this.placerUUID = compound.getUUID(NbtKeys.PLACER_UUID);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
    super.saveAdditional(compound, registries);
    if (this.placerUUID != null) {
      compound.putUUID(NbtKeys.PLACER_UUID, placerUUID);
    }
  }
}

package com.github.yzqdev.pethome.server.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * 宠物床（{@code pet_home:pet_bed_*}，16 色）的 BlockItem：记录所属 {@link DyeColor}， tooltip
 * 说明床的用途（宠物死亡后次日按床位置复活）。
 */
public class PetbedItem extends BlockItem {
  /** 该物品对应的染色（决定方块状态与物品 id 后缀）。 */
  private final DyeColor color;

  /** 对应方块引用（覆写 {@link #getBlock()} 固定返回，避免父类按注册名解析）。 */
  private final Block block;

  public PetbedItem(Block block, Properties props, DyeColor color) {
    super(block, props);
    this.block = block;
    this.color = color;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  public void onDestroyed(ItemEntity itemEntity) {}

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable("tooltips.pet_home.substitute_pet_bed.desc")
            .withStyle(ChatFormatting.GREEN));
  }
}

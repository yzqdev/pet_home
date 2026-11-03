package com.github.yzqdev.pethome.server.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 项圈标签（{@code pet_home:collar_tag}）：承载宠物附魔的物品——附魔的真正载体是宠物，此物品相当于可搬运 附魔的“项圈”，转移给宠物后由 {@code
 * TameableUtils} 读写其附魔集合。
 */
public class CollarTagItem extends Item {

  public CollarTagItem() {
    super(new Properties());
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return true;
  }

  @Override
  public int getEnchantmentValue() {
    return 1;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable("tooltips.pet_home.substitute_collar.desc")
            .withStyle(ChatFormatting.GREEN));
  }
}

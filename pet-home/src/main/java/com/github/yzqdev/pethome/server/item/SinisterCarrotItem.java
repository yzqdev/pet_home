package com.github.yzqdev.pethome.server.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/** 诡异萝卜：可食用食物（营养 1、食用必中 100 tick 凋零），用于驯服/喂食特定宠物的材料。 */
public class SinisterCarrotItem extends Item {

  public SinisterCarrotItem() {
    super(
        new Properties()
            .rarity(Rarity.UNCOMMON)
            .food(
                (new FoodProperties.Builder())
                    .nutrition(1)
                    .saturationModifier(0.3f)
                    .effect(new MobEffectInstance(MobEffects.WITHER, 100), 1.0F)
                    .build()));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable("tooltips.pet_home.substitute_sinister_carrot.desc")
            .withStyle(ChatFormatting.GREEN));
  }
}

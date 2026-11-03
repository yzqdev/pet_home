package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.misc.PetCompassTeleport;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * 哨子（whistle）：右键吹响，召回玩家的全部宠物（罗盘档案里的存活宠物，含未加载强载召回与宝盒召回， 复用 {@link PetCompassTeleport} 的单只召回路径）。使用后
 * 100 tick 冷却防连按刷屏。
 */
public class WhistleItem extends Item {

  /** 使用冷却（tick） */
  private static final int COOLDOWN_TICKS = 100;

  public WhistleItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      PetCompassTeleport.recallAllPets(serverPlayer);
      level.playSound(
          null,
          player.getX(),
          player.getY(),
          player.getZ(),
          SoundEvents.NOTE_BLOCK_FLUTE.value(),
          SoundSource.PLAYERS,
          0.8F,
          1.6F);
      player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
      player.swing(hand);
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable(LangDefinition.TOOLTIP_WHISTLE_DESC)
            .withStyle(ChatFormatting.GREEN));
  }
}

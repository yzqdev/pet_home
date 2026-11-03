package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * 羽毛竿：复用钓鱼钩机制（{@code FeatherEntity} 借用 {@code FishingHook} 行为）的宠物引导工具， 耐久
 * 160。右键抛出羽毛实体；羽毛在飞时再次右键按钓竿规则收线并扣耐久。
 */
public class FeatherOnAStickItem extends Item {

  public FeatherOnAStickItem() {
    super(new Item.Properties().durability(160));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable("tooltips.pet_home.substitute_feather.desc")
            .withStyle(ChatFormatting.GREEN));
  }

  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    if (player.fishing != null && player.fishing.isAlive()) {
      if (!level.isClientSide()) {
        int i = player.fishing.retrieve(itemstack);
        itemstack.hurtAndBreak(i, player, EquipmentSlot.MAINHAND);
      }
      level.playSound(
          null,
          player.getX(),
          player.getY(),
          player.getZ(),
          SoundEvents.FISHING_BOBBER_RETRIEVE,
          SoundSource.NEUTRAL,
          1.0F,
          0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
      player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
    } else {
      level.playSound(
          null,
          player.getX(),
          player.getY(),
          player.getZ(),
          SoundEvents.FISHING_BOBBER_THROW,
          SoundSource.NEUTRAL,
          0.5F,
          0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
      FeatherEntity featherEntity = new FeatherEntity(player, level);
      featherEntity.setOwner(player);
      if (!level.isClientSide()) {
        level.addFreshEntity(featherEntity);
      }
      player.awardStat(Stats.ITEM_USED.get(this));
      player.gameEvent(GameEvent.ITEM_INTERACT_START);
    }

    return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
  }
}

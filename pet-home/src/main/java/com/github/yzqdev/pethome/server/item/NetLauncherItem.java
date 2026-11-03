package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.NetEntity;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 捕网发射器：蓄力把捕网当抛射物打出去的远程工具。两种模式（存于物品数据组件 {@code RELEASE_MODE}）：
 *
 * <ul>
 *   <li>捕捉模式：发射空网（现场生成 {@code pet_home:net} 实体），命中生物即装网；
 *   <li>释放模式：消耗背包里已装生物的满网，把里面的生物连同网一起发射出去。
 * </ul>
 *
 * 潜行右键切换模式（物品名后缀显示当前模式），长按右键蓄力、松开发射。
 */
public class NetLauncherItem extends Item {

  public NetLauncherItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      List<Component> tooltipComponents,
      TooltipFlag tooltipFlag) {
    tooltipComponents.add(
        Component.translatable(LangDefinition.net_launcher_tip).withStyle(ChatFormatting.GREEN));
    tooltipComponents.add(
        Component.translatable(LangDefinition.net_launcher_default_only_tamable)
            .withStyle(ChatFormatting.GRAY));
  }

  /**
   * 蓄力结束（松开右键）：蓄力不足 0.1 直接无效；服务端按模式生成 {@code NetEntity} 并按玩家视角发射， 发射器扣 1
   * 点耐久；释放模式还要求背包有满网（没有则提示），成功后满网数量 -1。
   */
  @Override
  public void releaseUsing(
      ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
    if (entityLiving instanceof Player player) {

      int i = this.getUseDuration(stack, player) - timeLeft;
      if (i < 0) {
        return;
      }

      float f = getNetVelocity(i);

      if (f >= 0.1) {
        // 获取包含实体的球

        if (!worldIn.isClientSide()) {

          if (isCaptureMode(stack)) {
            ItemStack netStack = new ItemStack(PHItemRegistry.NET_ITEM);

            if (netStack.getItem() instanceof NetItem netItem) {
              NetEntity netEntity = netItem.createNet(worldIn, player, netStack);
              netEntity.shootFromRotation(
                  player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 0);

              worldIn.addFreshEntity(netEntity);
              stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
          } else {
            ItemStack netWithEntityStack = this.findNet(player);
            if (netWithEntityStack.isEmpty()) {
              player.sendSystemMessage(Component.translatable(LangDefinition.no_net_entity_text));
              return;
            }

            if (netWithEntityStack.getItem() instanceof NetItem netItem) {
              NetEntity netEntity = netItem.createNet(worldIn, player, netWithEntityStack);
              netEntity.shootFromRotation(
                  player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 0);

              worldIn.addFreshEntity(netEntity);
              stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

              netWithEntityStack.shrink(1);
            }
          }
        }

        worldIn.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.ARROW_SHOOT,
            SoundSource.PLAYERS,
            1.0F,
            1.0F);
      }
    }
  }

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return true;
  }

  @Override
  public @NotNull ItemStack getDefaultInstance() {
    var stack = new ItemStack(this);
    stack.set(PHDataComponents.RELEASE_MODE, false);
    return stack;
  }

  /** 按蓄力 tick 数计算初速：{@code (x²+2x)/3}（x=秒），封顶 1.5（与弓同族的抛物线手感）。 */
  public static float getNetVelocity(int charge) {
    float f = (float) charge / 20;
    f = (f * f + f * 2) / 3;
    f = Math.min(f, 1.5f);
    return f;
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 72000;
  }

  /** 右键：潜行=切换捕捉/释放模式并提示；否则进入蓄力使用状态。 */
  @Override
  @Nonnull
  public InteractionResultHolder<ItemStack> use(
      Level worldIn, Player player, @Nonnull InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (player.isCrouching()) {

      boolean capture = isCaptureMode(stack);
      stack.set(PHDataComponents.RELEASE_MODE, capture);
      player.displayClientMessage(capture ? RELEASE : CAPTURE, true);
      return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
    if (isCaptureMode(stack)) {
      player.startUsingItem(hand);
      return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    } else {

      player.startUsingItem(hand);
      return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
  }

  public static final Component CAPTURE =
      Component.translatable(LangDefinition.capturing_text).withStyle(ChatFormatting.GREEN);
  public static final Component RELEASE =
      Component.translatable(LangDefinition.release_text).withStyle(ChatFormatting.RED);

  @Override
  @Nonnull
  public Component getName(@Nonnull ItemStack stack) {
    MutableComponent base = (MutableComponent) super.getName(stack);
    return base.append(" (").append(isCaptureMode(stack) ? CAPTURE : RELEASE).append(")");
  }

  // helpers

  /** 该物品是否处于捕捉模式：数据组件缺失或为 false 时均视为捕捉（默认模式）。 */
  public static boolean isCaptureMode(ItemStack stack) {

    return (!stack.has(PHDataComponents.RELEASE_MODE))
        || Boolean.FALSE.equals(stack.get(PHDataComponents.RELEASE_MODE));
  }

  /** 优先级查找满网：副手 → 主手 → 背包逐槽；找不到返回 {@link ItemStack#EMPTY}。 */
  protected ItemStack findNet(Player player) {
    ItemStack stack = player.getMainHandItem();

    if (isFilledNet(player.getItemInHand(InteractionHand.OFF_HAND))) {
      return player.getItemInHand(InteractionHand.OFF_HAND);
    } else if (isFilledNet(player.getItemInHand(InteractionHand.MAIN_HAND))) {
      return player.getItemInHand(InteractionHand.MAIN_HAND);
    } else {
      for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
        ItemStack itemstack = player.getInventory().getItem(i);

        if (isFilledNet(itemstack)) {
          return itemstack;
        }
      }
    }
    return ItemStack.EMPTY;
  }

  /** 空网判定：是捕网且里面没有装生物。 */
  public static boolean isEmptyNet(ItemStack stack) {
    return stack.getItem() instanceof NetItem && !NetItem.containsEntity(stack);
  }

  /** 满网判定：是捕网且里面装着生物。 */
  public static boolean isFilledNet(ItemStack stack) {
    return stack.getItem() instanceof NetItem && NetItem.containsEntity(stack);
  }
}

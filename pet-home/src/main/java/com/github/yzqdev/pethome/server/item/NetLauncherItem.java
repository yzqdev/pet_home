package com.github.yzqdev.pethome.server.item;

import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.NetEntity;
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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 捕网发射器：蓄力发射 NetEntity 投射物。潜行右键在抓捕模式（发射空网抓生物）
 * 与释放模式（从背包找有内容的网发射放出实体）间切换，模式存
 * {@link PHDataComponents#RELEASE_MODE} 组件并反映在物品名后缀。
 */
public class NetLauncherItem extends Item {

    public NetLauncherItem(Properties properties) {
        super(properties);

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(LangDefinition.net_launcher_tip).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable(LangDefinition.net_launcher_default_only_tamable).withStyle(ChatFormatting.GRAY));

    }

    /** 松开右键：蓄力达到阈值才发射（抓捕模式发空网 / 释放模式消耗背包内一个有内容的网），耐久-1 */
    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {


            int i = this.getUseDuration(stack, player) - timeLeft;
            if (i < 0) {
                return;
            }


            float f = getNetVelocity(i);

            if (f >= 0.1) {
//获取包含实体的球

                if (!worldIn.isClientSide()) {

                    if (isCaptureMode(stack)) {
                        ItemStack netStack = new ItemStack(PHItemRegistry.NET_ITEM.get());

                        if (netStack.getItem() instanceof NetItem netItem) {
                            NetEntity netEntity = netItem.createNet(worldIn, player, netStack);
                            netEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 0);

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
                            netEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 0);

                            worldIn.addFreshEntity(netEntity);
                            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

                            netWithEntityStack.shrink(1);


                        }
                    }

                }

                worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);


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

    /**
     * Gets the velocity of the net entity from the launcher's charge
     */
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


    /** 蓄力 → 初速（弓弦公式，上限 1.5）；副手切换模式；两种模式都 startUsingItem 蓄力 */
    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, @Nonnull InteractionHand hand) {
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

    public static final Component CAPTURE = Component.translatable(LangDefinition.capturing_text).withStyle(ChatFormatting.GREEN);
    public static final Component RELEASE = Component.translatable(LangDefinition.release_text).withStyle(ChatFormatting.RED);

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        MutableComponent base = (MutableComponent) super.getName(stack);
        return base.append(" (").append(isCaptureMode(stack) ? CAPTURE : RELEASE).append(")");
    }

    /** 是否抓捕模式：RELEASE_MODE 组件缺省或 false 均视为抓捕（默认行为） */
    public static boolean isCaptureMode(ItemStack stack) {

        return (!stack.has(PHDataComponents.RELEASE_MODE)) || Boolean.FALSE.equals(stack.get(PHDataComponents.RELEASE_MODE));
    }

    /** 释放模式找第一个有内容的网：先副手→主手→背包；找不到返回 EMPTY（发射前提示） */
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


    public static boolean isEmptyNet(ItemStack stack) {
        return stack.getItem() instanceof NetItem && !NetItem.containsEntity(stack);
    }

    public static boolean isFilledNet(ItemStack stack) {
        return stack.getItem() instanceof NetItem && NetItem.containsEntity(stack);
    }

}

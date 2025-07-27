package com.github.yzqdev.pet_home.server.item;

import com.github.yzqdev.pet_home.datagen.LangDefinition;
import com.github.yzqdev.pet_home.server.PHDataComponents;
import com.github.yzqdev.pet_home.server.entity.NetEntity;
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

public class NetLauncherItem extends Item {

    public NetLauncherItem(Properties properties) {
        super(properties);

    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(LangDefinition.ConstantMsg.net_launcher_tip).withStyle(ChatFormatting.GREEN));
        tooltipComponents.add(Component.translatable(LangDefinition.ConstantMsg.net_launcher_default_only_tamable).withStyle(ChatFormatting.GRAY));

    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {


            int i = this.getUseDuration(stack, player) - timeLeft;
            if (i < 0) return;


            float f = getNetVelocity(i);

            if (f >= 0.1) {
//获取包含实体的球

                if (!worldIn.isClientSide) {

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
                            player.sendSystemMessage(Component.translatable(LangDefinition.ConstantMsg.no_net_entity_text));
                            return;
                        }

                        if (netWithEntityStack.getItem() instanceof NetItem netItem) {
                            NetEntity netEntity = netItem.createNet(worldIn, player, netWithEntityStack);
//
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


    /**
     * Called when the equipped item is right clicked.
     */
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

    public static final Component CAPTURE = Component.translatable(LangDefinition.ConstantMsg.capturing_text).withStyle(ChatFormatting.GREEN);
    public static final Component RELEASE = Component.translatable(LangDefinition.ConstantMsg.release_text).withStyle(ChatFormatting.RED);

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        MutableComponent base = (MutableComponent) super.getName(stack);
        return base.append(" (").append(isCaptureMode(stack) ? CAPTURE : RELEASE).append(")");
    }

    //helpers
    public static boolean isCaptureMode(ItemStack stack) {

        return (!stack.has(PHDataComponents.RELEASE_MODE))||  Boolean.FALSE.equals(stack.get(PHDataComponents.RELEASE_MODE));
    }

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

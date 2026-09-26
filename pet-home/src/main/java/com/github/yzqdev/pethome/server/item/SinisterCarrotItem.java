package com.github.yzqdev.pethome.server.item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SinisterCarrotItem extends Item {

    public SinisterCarrotItem() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.3F).effect(()->new MobEffectInstance(MobEffects.WITHER, 100), 1.0F).build()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_sinister_carrot.desc").withStyle(ChatFormatting.GREEN));
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // 兔子/僵尸马的转换统一在 EntityInteractHandler（PlayerInteractEvent）里做。
        // 不要在这里重复实现：事件与物品两条路径各转一次会生成两个目标（僵尸马还会双倍消耗胡萝卜）。
        return InteractionResult.PASS;
    }
}

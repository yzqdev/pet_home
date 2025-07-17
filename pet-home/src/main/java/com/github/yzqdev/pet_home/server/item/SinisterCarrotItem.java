package com.github.yzqdev.pet_home.server.item;


import com.github.yzqdev.pet_home.util.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.EventHooks;

import java.util.List;

public class SinisterCarrotItem extends Item {

    public SinisterCarrotItem() {
        super(new Properties().rarity(Rarity.UNCOMMON).food((new FoodProperties.Builder()).nutrition(1).saturationModifier(0.3f).effect(new MobEffectInstance(MobEffects.WITHER, 100), 1.0F).build()));
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_sinister_carrot.desc").withStyle(ChatFormatting.GREEN));

    }
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(entity.getType() == EntityType.RABBIT && TameableUtils.isTamed(entity) && TameableUtils.isPetOf(player, entity) && EventHooks.canLivingConvert(entity, EntityType.RABBIT, (timer) -> {})){
            if(entity instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL){
                player.swing(hand);
                rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
                rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
                rabbit.setVariant(Rabbit.Variant.EVIL);
                if(!player.isCreative()){
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
        }
        if(entity.getType() == EntityType.ZOMBIE_HORSE && EventHooks.canLivingConvert(entity, EntityType.SKELETON_HORSE, (timer) -> {})){
            player.swing(hand);
            ZombieHorse horse = (ZombieHorse)entity;
            horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
            horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
            CompoundTag horseExtras = new CompoundTag();
            horse.addAdditionalSaveData(horseExtras);
            for(int i = 0; i < 6 + horse.getRandom().nextInt(5); i++){
                horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
            }
            SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level());
            if(horse.isLeashed()){
                skeleton.setLeashedTo(horse.getLeashHolder(), true);
            }
            skeleton.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            skeleton.setNoAi(horse.isNoAi());
            skeleton.setBaby(horse.isBaby());
            if (horse.hasCustomName()) {
                skeleton.setCustomName(horse.getCustomName());
                skeleton.setCustomNameVisible(horse.isCustomNameVisible());
            }
            skeleton.readAdditionalSaveData(horseExtras);
            skeleton.setPersistenceRequired();
            EventHooks.onLivingConvert(horse, skeleton);
            player.level().addFreshEntity(skeleton);
            horse.discard();
            if(!player.isCreative()){
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}

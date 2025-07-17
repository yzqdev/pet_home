package com.github.yzqdev.pet_home.server.item;

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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.EventHooks;

import java.util.List;

public class RottenAppleItem extends Item {

    public RottenAppleItem() {
        super(new Properties().food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.3f).effect(()->new MobEffectInstance(MobEffects.POISON, 100, 1), 1.0F).build()));
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltips.pet_home.substitute_rotten_apple.desc").withStyle(ChatFormatting.GREEN));

    }
@Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if(entity.getType() == EntityType.HORSE && EventHooks.canLivingConvert(entity, EntityType.ZOMBIE_HORSE, (timer) -> {})){
            player.swing(hand);
            Horse horse = (Horse)entity;
            horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
            horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
            CompoundTag horseExtras = new CompoundTag();
            if(!horse.getBodyArmorItem().isEmpty()){
                horse.spawnAtLocation(horse.getBodyArmorItem().copy());
                horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
            horse.addAdditionalSaveData(horseExtras);
            for(int i = 0; i < 6 + horse.getRandom().nextInt(5); i++){
                horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
            }
            ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level());
            if(horse.isLeashed()){
                zombie.setLeashedTo(horse.getLeashHolder(), true);
            }
            zombie.moveTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), horse.getXRot());
            zombie.setNoAi(horse.isNoAi());
            zombie.setBaby(horse.isBaby());
            if (horse.hasCustomName()) {
                zombie.setCustomName(horse.getCustomName());
                zombie.setCustomNameVisible(horse.isCustomNameVisible());
            }
            zombie.readAdditionalSaveData(horseExtras);
            zombie.setPersistenceRequired();
            EventHooks.onLivingConvert(horse, zombie);
            player.level().addFreshEntity(zombie);
            horse.discard();
            if(!player.isCreative()){
                stack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

}

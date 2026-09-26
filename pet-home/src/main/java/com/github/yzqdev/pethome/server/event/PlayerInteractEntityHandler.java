package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.ModifedToBeTameable;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Map;


public class PlayerInteractEntityHandler {

    public static InteractionResult onInteractWithEntity(Player player, InteractionHand hand, Entity target) {
        Level level = player.level();
        ItemStack itemstack = player.getItemInHand(hand);

        // 1. 抓捕逻辑 (Net Item)
        InteractionResult result = handleNetCapture(player, hand, level, target, itemstack);
        if (result != InteractionResult.PASS) {
            return result;
        }

        // 2. 宠物相关逻辑 (仅限 LivingEntity 且属于该玩家)
        if (target instanceof LivingEntity living && TameableUtils.isPetOf(player, target)) {
            // 贪婪附魔喂食
            result = handleGluttonousFeeding(player, living, itemstack);
            if (result != InteractionResult.PASS) return result;

            // 项圈逻辑
            result = handleCollarTag(player, level, living, itemstack);
            if (result != InteractionResult.PASS) return result;

            // 契约逻辑 (解除拥有关系)
            result = handleDeedOfOwnership(player, hand, target, itemstack);
            if (result != InteractionResult.PASS) return result;
        }


        if (target instanceof Rabbit rabbit && PetHomeConfig.tameableRabbit) {
            if (handleRabbitHayBlock(player, rabbit, itemstack)) {
                return InteractionResult.SUCCESS;
            }
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(player, rabbit)) {
                ((IComandableMob) rabbit).playerSetCommand(player, rabbit);
            }
        }


        if (target instanceof LivingEntity living) {
            handleEntityConversions(player, hand, living, itemstack);
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理使用捕网抓捕实体的逻辑
     */
    private static InteractionResult handleNetCapture(Player player, InteractionHand hand, Level level, Entity target, ItemStack itemstack) {
        if (!level.isClientSide() && itemstack.is(PHItemRegistry.NET_ITEM)) {
            if (!target.isAlive() || NetItem.containsEntity(itemstack)) {
                return InteractionResult.SUCCESS;
            }
            var netItem = (NetItem) itemstack.getItem();
            if (netItem.getType() == Type.EMPTY) {
                if (!NetItem.canCatchMob(target)) {
                    return InteractionResult.SUCCESS;
                }
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM);
                CompoundTag nbt = NetItem.getNBTfromEntity(target);
                ItemStack newerStack = newStack.split(1);
                newerStack.set(PHDataComponents.ENTITY_HOLDER, nbt);

                player.swing(hand);
                player.setItemInHand(hand, newStack);
                if (!player.addItem(newerStack)) {
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
                    player.level().addFreshEntity(itemEntity);
                }
                target.discard();
                player.getCooldowns().addCooldown(itemstack.getItem(), 5);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理“贪婪”附魔的自动喂食愈合逻辑
     */
    private static InteractionResult handleGluttonousFeeding(Player player, LivingEntity living, ItemStack itemstack) {
        if (TameableUtils.hasEnchant(living, ModEnchantments.GLUTTONOUS)) {
            var foodProperty = itemstack.get(net.minecraft.core.component.DataComponents.FOOD);
            if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
                living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
                living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理项圈标签的应用与替换逻辑
     */
    private static InteractionResult handleCollarTag(Player player, Level level, LivingEntity living, ItemStack itemstack) {

        if (!PetHomeConfig.collarTag) {
            return InteractionResult.PASS;
        }
        if (itemstack.is(PHItemRegistry.COLLAR_TAG)) {
            if (!level.isClientSide && living.isAlive()) {
                var itemEnchantments = itemstack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);

                if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                    living.setCustomName(itemstack.getHoverName());
                }
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }

                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG);
                    if (entityEnchantments != null) {
                        var reg = living.level().registryAccess().registry(Registries.ENCHANTMENT);
                        reg.ifPresent(r -> {
                            for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                                var oneEnchant = r.get(entry.getKey());
                                if (oneEnchant != null) {
                                    collarFrom.enchant(r.wrapAsHolder(oneEnchant), entry.getValue());
                                }
                            }
                        });
                    }
                    living.spawnAtLocation(collarFrom);
                }

                ServerEvent.blockCollarTick(living);
                living.playSound(PHSoundRegistry.COLLAR_TAG, 1, 1);
                TameableUtils.clearEnchants(living);
                if (!itemEnchantments.isEmpty()) {
                    TameableUtils.addEnchant(living, itemEnchantments);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 处理所有权契约（解除驯服）逻辑
     */
    private static InteractionResult handleDeedOfOwnership(Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (TameableUtils.isTamed(target) && itemstack.is(PHItemRegistry.DEED_OF_OWNERSHIP)) {
            TamableAnimal tamableAnimal = (TamableAnimal) target;
            TameableUtils.clearEnchants(tamableAnimal);
            TameableUtils.removePetBedPos(tamableAnimal);
            tamableAnimal.setTame(false, false);
            tamableAnimal.setOwnerUUID(null);
            tamableAnimal.setOrderedToSit(false);
            tamableAnimal.setInSittingPose(false);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void handleEntityConversions(Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
        // 马 -> 僵尸马 (烂苹果)
        if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE)) {
            convertHorseToZombie(player, hand, (Horse) living, itemstack);
        }
        // 兔子 -> 邪恶兔子 (阴森胡萝卜)
        else if (living.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living)) {
                convertRabbitToEvil(player, hand, (Rabbit) living, itemstack);
            }
        }
        // 僵尸马 -> 骷髅马 (阴森胡萝卜)
        else if (living.getType() == EntityType.ZOMBIE_HORSE && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            convertZombieToSkeletonHorse(player, hand, (ZombieHorse) living, itemstack);
        }
    }

    private static void convertHorseToZombie(Player player, InteractionHand hand, Horse horse, ItemStack itemstack) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

        CompoundTag horseExtras = new CompoundTag();
        if (!horse.getBodyArmorItem().isEmpty()) {
            horse.spawnAtLocation(horse.getBodyArmorItem().copy());
            horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        horse.addAdditionalSaveData(horseExtras);
        spawnSneezeParticles(horse);

        ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level());

        if (zombie != null) {
            transferBasicData(horse, zombie, horseExtras);
            player.level().addFreshEntity(zombie);
            horse.discard();
            consumeItem(player, itemstack);
        }
    }

    private static void convertRabbitToEvil(Player player, InteractionHand hand, Rabbit rabbit, ItemStack itemstack) {
        if (rabbit.getVariant() != Rabbit.Variant.EVIL) {
            player.swing(hand);
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
            rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
            rabbit.setVariant(Rabbit.Variant.EVIL);
            consumeItem(player, itemstack);
        }
    }

    private static void convertZombieToSkeletonHorse(Player player, InteractionHand hand, ZombieHorse horse, ItemStack itemstack) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

        CompoundTag horseExtras = new CompoundTag();
        horse.addAdditionalSaveData(horseExtras);
        spawnSneezeParticles(horse);

        SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level());
        if (skeleton != null) {
            transferBasicData(horse, skeleton, horseExtras);
            player.level().addFreshEntity(skeleton);
            horse.discard();
            consumeItem(player, itemstack);
        }
    }

    // 辅助方法：处理数据继承
    private static void transferBasicData(Mob oldMob, Mob newMob, CompoundTag extras) {
        if (oldMob.isLeashed()) {
            newMob.setLeashedTo(oldMob.getLeashHolder(), true);
        }
        newMob.moveTo(oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getYRot(), oldMob.getXRot());
        newMob.setNoAi(oldMob.isNoAi());
        if (oldMob instanceof AgeableMob ageable && newMob instanceof AgeableMob newAgeable) {
            newAgeable.setBaby(ageable.isBaby());
        }
        if (oldMob.hasCustomName()) {
            newMob.setCustomName(oldMob.getCustomName());
            newMob.setCustomNameVisible(oldMob.isCustomNameVisible());
        }
        newMob.readAdditionalSaveData(extras);
        newMob.setPersistenceRequired();
    }

    // 辅助方法：生成粒子
    private static void spawnSneezeParticles(Entity entity) {
        for (int i = 0; i < 6 + entity.level().getRandom().nextInt(5); i++) {
            entity.level().addParticle(ParticleTypes.SNEEZE, entity.getRandomX(1.0F), entity.getRandomY(), entity.getRandomZ(1.0F), 0F, 0F, 0F);
        }
    }

    // 辅助方法：消耗物品
    private static void consumeItem(Player player, ItemStack stack) {
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }


    private static boolean handleRabbitHayBlock(Player player, Rabbit rabbit, ItemStack stack) {
        if (stack.getItem() == Items.HAY_BLOCK) {
            if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                rabbit.heal(3);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return true;
            }
            if (!TameableUtils.isTamed(rabbit) && !rabbit.level().isClientSide()) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                rabbit.playSound(SoundEvents.FOX_EAT);
                if (rabbit.getRandom().nextBoolean()) {
                    for (int i = 0; i < 3; ++i) {
                        double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                        ((ServerLevel) rabbit.level()).sendParticles(ParticleTypes.HEART, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                    }
                    ((ModifedToBeTameable) rabbit).setTame(true);
                    ((ModifedToBeTameable) rabbit).setTameOwnerUUID(player.getUUID());
                    ((IComandableMob) rabbit).setCommand(1);
                } else {
                    for (int i = 0; i < 3; ++i) {
                        double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
                        double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
                        ((ServerLevel) rabbit.level()).sendParticles(ParticleTypes.SMOKE, rabbit.getRandomX(1.0D), rabbit.getRandomY() + 0.5D, rabbit.getRandomZ(1.0D), 3, d0, d1, d2, 0.02F);
                    }
                }
                return true;
            }
        }
        return false;
    }
}

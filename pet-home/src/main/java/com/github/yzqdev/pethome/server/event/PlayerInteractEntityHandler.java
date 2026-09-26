package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.server.NbtKeys;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.enchantment.DIEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.ModifedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.item.DeedOfOwnershipItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.DIParticleRegistry;
import com.github.yzqdev.pethome.server.misc.DISoundRegistry;
import com.github.yzqdev.pethome.server.misc.DITagRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;
import java.util.UUID;


public class PlayerInteractEntityHandler {

    public InteractionResult onInteractWithEntity(Player player, InteractionHand hand, Entity entity, ItemStack stack) {
        if (entity instanceof LivingEntity livingEntity) {
            boolean horseToZombie = entity.getType() == EntityType.HORSE && stack.is(PHItemRegistry.ROTTEN_APPLE);
            boolean rabbitToEvil = entity.getType() == EntityType.RABBIT && entity instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL
                    && stack.is(PHItemRegistry.SINISTER_CARROT) && TameableUtils.isTamed(entity) && TameableUtils.isPetOf(player, entity);
            boolean zombieToSkeleton = entity.getType() == EntityType.ZOMBIE_HORSE && stack.is(PHItemRegistry.SINISTER_CARROT);
            if (horseToZombie || rabbitToEvil || zombieToSkeleton) {
                if (livingEntity.level().isClientSide()) {
                    // 客户端只做挥手反馈；返回 CONSUME 避免客户端预测继续走原版交互
                    player.swing(hand);
                    return InteractionResult.CONSUME;
                }
                if (horseToZombie) {
                    convertHorse(player, hand, stack, (Horse) entity);
                } else if (rabbitToEvil) {
                    convertRabbitToEvil(player, hand, stack, entity);
                } else {
                    convertZombieHorse(player, hand, stack, (ZombieHorse) entity);
                }
                // 返回 SUCCESS 中止后续原版交互，避免 Item#interactLivingEntity 里那份重复转换逻辑再跑一次（“生成两个”的根因）
                return InteractionResult.SUCCESS;
            }
        }
        if (TameableUtils.isTamed(entity)) {
            if (stack.is(PHItemRegistry.DEED_OF_OWNERSHIP)) {
                InteractionResult result = handleDeedOfOwnership(player, stack, entity);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        if (TameableUtils.couldBeTamed(entity) && TameableUtils.isZombiePet((LivingEntity) entity)) {
            return InteractionResult.SUCCESS;
        }
        if (entity instanceof LivingEntity living && TameableUtils.isTamed(entity) && TameableUtils.hasEnchant(living, DIEnchantmentRegistry.GLUTTONOUS)) {
            InteractionResult result = tryFeedGluttonous(player, living, stack);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (entity instanceof Rabbit rabbit && PetHomeMod.CONFIG.tameableRabbit.get()) {
            InteractionResult result = handleRabbitHayBlock(player, rabbit, stack);
            if (result != InteractionResult.PASS) {
                return result;
            }
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(player, rabbit)) {
                ((IComandableMob) rabbit).playerSetCommand(player, rabbit);
            }
        }
        if (entity instanceof LivingEntity living && TameableUtils.isPetOf(player, entity) && !living.getType().is(DITagRegistry.REFUSES_COLLAR_TAGS)) {
            InteractionResult result = applyCollarTag(player, living, stack);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    private void convertHorse(Player player, InteractionHand hand, ItemStack stack, Horse horse) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
        CompoundTag horseExtras = new CompoundTag();
        if (!horse.getArmor().isEmpty()) {
            horse.spawnAtLocation(horse.getArmor().copy());
            horse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        horse.addAdditionalSaveData(horseExtras);
        for (int i = 0; i < 6 + horse.getRandom().nextInt(5); i++) {
            horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
        }
        ZombieHorse zombie = EntityType.ZOMBIE_HORSE.create(horse.level());
        if (horse.isLeashed()) {
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
        player.level().addFreshEntity(zombie);
        horse.discard();
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    private void convertRabbitToEvil(Player player, InteractionHand hand, ItemStack stack, Entity entity) {
        if (entity instanceof Rabbit rabbit && rabbit.getVariant() != Rabbit.Variant.EVIL) {
            player.swing(hand);
            rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
            rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
            rabbit.setVariant(Rabbit.Variant.EVIL);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }
    }

    private void convertZombieHorse(Player player, InteractionHand hand, ItemStack stack, ZombieHorse horse) {
        player.swing(hand);
        horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
        horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());
        CompoundTag horseExtras = new CompoundTag();
        horse.addAdditionalSaveData(horseExtras);
        for (int i = 0; i < 6 + horse.getRandom().nextInt(5); i++) {
            horse.level().addParticle(ParticleTypes.SNEEZE, horse.getRandomX(1.0F), horse.getRandomY(), horse.getRandomZ(1.0F), 0F, 0F, 0F);
        }
        SkeletonHorse skeleton = EntityType.SKELETON_HORSE.create(horse.level());
        if (horse.isLeashed()) {
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
        player.level().addFreshEntity(skeleton);
        horse.discard();
        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    /**
     * @return 非 PASS 表示已处理
     */
    private InteractionResult handleDeedOfOwnership(Player player, ItemStack stack, Entity entity) {
        CompoundTag tag = stack.getTag();
        boolean unbound = !DeedOfOwnershipItem.isBound(stack);
        Entity currentOwner = TameableUtils.getOwnerOf(entity);
        if (TameableUtils.isTamed(entity) && currentOwner != null && currentOwner.equals(player) && unbound) {
            CompoundTag newTag = new CompoundTag();
            newTag.putBoolean(NbtKeys.HAS_BOUND_ENTITY, true);
            newTag.putUUID(NbtKeys.BOUND_ENTITY, entity.getUUID());
            newTag.putString(NbtKeys.BOUND_ENTITY_NAME, entity.getName().getString());
            stack.setTag(newTag);
            return InteractionResult.SUCCESS;
        }
        if (TameableUtils.isTamed(entity) && tag != null && tag.getBoolean(NbtKeys.HAS_BOUND_ENTITY) && tag.hasUUID(NbtKeys.BOUND_ENTITY)) {
            UUID fromItem = tag.getUUID(NbtKeys.BOUND_ENTITY);
            if (entity.getUUID().equals(fromItem)) {
                player.getCooldowns().addCooldown(stack.getItem(), 5);
                TameableUtils.setOwnerUUIDOf(entity, player.getUUID());
                player.displayClientMessage(Component.translatable("message.pet_home.set_owner", player.getName(), entity.getName()), true);
                if (currentOwner instanceof Player && !currentOwner.equals(player)) {
                    ((Player) currentOwner).displayClientMessage(Component.translatable("message.pet_home.set_owner", player.getName(), entity.getName()), true);
                }
                stack.setTag(new CompoundTag());
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult tryFeedGluttonous(Player player, LivingEntity living, ItemStack stack) {
        if (stack.getItem().isEdible() && living.getHealth() < living.getMaxHealth() && stack.getItem().getFoodProperties() != null) {
            living.heal((float) Math.floor(stack.getItem().getFoodProperties().getNutrition() * 1.5F));
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * @return 非 PASS 表示已处理
     */
    private InteractionResult handleRabbitHayBlock(Player player, Rabbit rabbit, ItemStack stack) {
        if (stack.getItem() == Items.HAY_BLOCK) {
            if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                rabbit.heal(3);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
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
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult applyCollarTag(Player player, LivingEntity living, ItemStack stack) {
        if (stack.is(PHItemRegistry.COLLAR_TAG) && PetHomeMod.CONFIG.collarTag.get()) {
            if (!player.level().isClientSide() && living.isAlive()) {
                Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.deserializeEnchantments(stack.getEnchantmentTags());
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);
                if (stack.hasCustomHoverName() && living.hasCustomName() && stack.getHoverName().equals(living.getCustomName())) {
                    boolean hasSameEnchants = itemEnchantments.isEmpty();
                    if (entityEnchantments != null) {
                        hasSameEnchants = true;
                        for (Map.Entry<Enchantment, Integer> itemEntry : itemEnchantments.entrySet()) {
                            ResourceLocation name = BuiltInRegistries.ENCHANTMENT.getKey(itemEntry.getKey());
                            if (entityEnchantments.get(name) == null || !entityEnchantments.get(name).equals(itemEntry.getValue())) {
                                hasSameEnchants = false;
                            }
                        }
                    }
                    if (hasSameEnchants) {
                        return InteractionResult.FAIL;
                    }
                }
                if (stack.hasCustomHoverName()) {
                    living.setCustomName(stack.getHoverName());
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                ServerEvent.blockCollarTick(living);
                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG);
                    if (entityEnchantments != null) {
                        collarFrom.getOrCreateTag();
                        if (!collarFrom.getTag().contains(NbtKeys.ENCHANTMENTS, 9)) {
                            collarFrom.getTag().put(NbtKeys.ENCHANTMENTS, new ListTag());
                        }

                        ListTag listtag = collarFrom.getTag().getList(NbtKeys.ENCHANTMENTS, 10);
                        for (Map.Entry<ResourceLocation, Integer> entry : entityEnchantments.entrySet()) {
                            listtag.add(EnchantmentHelper.storeEnchantment(entry.getKey(), entry.getValue()));
                        }
                    } else {
                        collarFrom.setTag(null);
                    }
                    living.spawnAtLocation(collarFrom);
                }
                living.playSound(DISoundRegistry.COLLAR_TAG, 1, 1);
                if (itemEnchantments.isEmpty()) {
                    TameableUtils.clearEnchants(living);
                } else {
                    ListTag listTag = new ListTag();
                    for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()) {
                        TameableUtils.addEnchant(living, new EnchantmentInstance(entry.getKey(), entry.getValue()), listTag);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

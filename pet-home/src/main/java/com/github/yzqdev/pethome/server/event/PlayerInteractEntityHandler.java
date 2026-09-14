package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.datagen.ModEnchantments;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import com.github.yzqdev.pethome.server.entity.ModifedToBeTameable;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

@EventBusSubscriber(modid = PetHomeMod.MODID)
public class PlayerInteractEntityHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        Level level = event.getLevel();
        Entity target = event.getTarget();
        ItemStack itemstack = event.getItemStack();

        // 1. 抓捕逻辑 (Net Item)
        if (handleNetCapture(event, player, hand, level, target, itemstack)) {
            return;
        }

        // 2. 宠物相关逻辑 (仅限 LivingEntity 且属于该玩家)
        if (target instanceof LivingEntity living && TameableUtils.isPetOf(player, target)) {
            // 贪婪附魔喂食
            if (handleGluttonousFeeding(event, living, itemstack)) return;

            // 项圈逻辑
            if (handleCollarTag(event, living, itemstack)) return;

            // 契约逻辑 (解除拥有关系)
            if (handleDeedOfOwnership(event, player, hand, target, itemstack)) return;
        }

        // 兔子驯服/指令 (自 1.20 移植)——必须在 isPetOf 门之外：
        // 干草块驯服针对野兔（没有主人，isPetOf 恒 false），嵌进门内会导致驯服永远无法触发
        if (target instanceof Rabbit rabbit && PetHomeConfig.tameableRabbit) {
            if (handleRabbitHayBlock(event, player, rabbit, itemstack)) return;
            if (TameableUtils.isTamed(rabbit) && TameableUtils.isPetOf(player, rabbit)) {
                ((IComandableMob) rabbit).playerSetCommand(player, rabbit);
            }
        }

        // 3. 实体转换逻辑 (马变僵尸、兔子变邪恶、僵尸马变骷髅马)
        if (target instanceof LivingEntity living) {
            handleEntityConversions(event, player, hand, living, itemstack);
        }
    }

    /**
     * 处理使用捕网抓捕实体的逻辑
     */
    private static boolean handleNetCapture(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Level level, Entity target, ItemStack itemstack) {
        if (!level.isClientSide() && itemstack.is(PHItemRegistry.NET_ITEM)) {
            if (!target.isAlive() || NetItem.containsEntity(itemstack)) {
                return true;
            }
            var netItem = (NetItem) itemstack.getItem();
            if (netItem.getType() == Type.EMPTY) {
                if (!NetItem.canCatchMob(target)) {
                    event.setCanceled(true);
                    return true;
                }
                ItemStack newStack = new ItemStack(PHItemRegistry.NET_HAS_ITEM.get());
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
                return true;
            }
        }
        return false;
    }

    /**
     * 处理“贪婪”附魔的自动喂食愈合逻辑
     */
    private static boolean handleGluttonousFeeding(PlayerInteractEvent.EntityInteract event, LivingEntity living, ItemStack itemstack) {
        if (TameableUtils.hasEnchant(living, ModEnchantments.GLUTTONOUS)) {
            var foodProperty = itemstack.getItem().getFoodProperties(itemstack, living);
            if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
                living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }
                living.playSound(living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT, 1F, living.getVoicePitch());
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }

    /**
     * 处理项圈标签的应用与替换逻辑
     */
    private static boolean handleCollarTag(PlayerInteractEvent.EntityInteract event, LivingEntity living, ItemStack itemstack) {
        if (itemstack.is(PHItemRegistry.COLLAR_TAG.get())) {
            if (!event.getEntity().level().isClientSide && living.isAlive()) {
                var itemEnchantments = itemstack.getTagEnchantments();
                Map<ResourceLocation, Integer> entityEnchantments = TameableUtils.getEnchants(living);

                if (itemstack.has(DataComponents.CUSTOM_NAME)) {
                    living.setCustomName(itemstack.getHoverName());
                }
                if (!event.getEntity().isCreative()) {
                    itemstack.shrink(1);
                }

                if (TameableUtils.hasCollar(living)) {
                    ItemStack collarFrom = new ItemStack(PHItemRegistry.COLLAR_TAG.get());
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
                living.playSound(PHSoundRegistry.COLLAR_TAG.get(), 1, 1);
                TameableUtils.clearEnchants(living);
                if (!itemEnchantments.isEmpty()) {
                    TameableUtils.addEnchant(living, itemEnchantments);
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return true;
        }
        return false;
    }

    /**
     * 处理所有权契约（解除驯服）逻辑
     */
    private static boolean handleDeedOfOwnership(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
        if (TameableUtils.isTamed(target) && itemstack.is(PHItemRegistry.DEED_OF_OWNERSHIP.get())) {
            TamableAnimal tamableAnimal = (TamableAnimal) target;
            TameableUtils.clearEnchants(tamableAnimal);
            TameableUtils.removePetBedPos(tamableAnimal);
            tamableAnimal.setTame(false, false);
            tamableAnimal.setOwnerUUID(null);
            tamableAnimal.setOrderedToSit(false);
            tamableAnimal.setInSittingPose(false);
            player.swing(hand);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    /**
     * 汇总处理实体的转换（进化/变异）逻辑
     */
    /**
     * 汇总处理实体的转换（进化/变异）逻辑。
     * 转换只在这里做：转换完成后必须取消事件，否则原版 interactOn 会继续走
     * Item.interactLivingEntity（历史双马根因——物品类里曾有一份重复转换逻辑，已删除）。
     * 客户端只做挥手反馈并取消事件，避免客户端预测开始进食。
     */
    private static void handleEntityConversions(PlayerInteractEvent.EntityInteract event, Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
        boolean clientSide = living.level().isClientSide();
        // 马 -> 僵尸马 (烂苹果)
        if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE)) {
            if (EventHooks.canLivingConvert(living, EntityType.ZOMBIE_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertHorseToZombie(player, hand, (Horse) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 兔子 -> 邪恶兔子 (阴森胡萝卜)
        else if (living.getType() == EntityType.RABBIT && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living) &&
                    EventHooks.canLivingConvert(living, EntityType.RABBIT, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertRabbitToEvil(player, hand, (Rabbit) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
        }
        // 僵尸马 -> 骷髅马 (阴森胡萝卜)
        else if (living.getType() == EntityType.ZOMBIE_HORSE && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
            if (EventHooks.canLivingConvert(living, EntityType.SKELETON_HORSE, (timer) -> {})) {
                if (clientSide) {
                    player.swing(hand);
                } else {
                    convertZombieToSkeletonHorse(player, hand, (ZombieHorse) living, itemstack);
                }
                event.setCanceled(true);
                event.setCancellationResult(clientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS);
            }
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
            EventHooks.onLivingConvert(horse, zombie);
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
            EventHooks.onLivingConvert(horse, skeleton);
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
    /**
     * 兔子驯服/治疗：干草块驯服兔子（自 1.20 移植）
     */
    private static boolean handleRabbitHayBlock(PlayerInteractEvent.EntityInteract event, Player player, Rabbit rabbit, ItemStack stack) {
        if (stack.getItem() == Items.HAY_BLOCK) {
            if (TameableUtils.isTamed(rabbit) && rabbit.getHealth() < rabbit.getMaxHealth()) {
                rabbit.heal(3);
                if (!event.getEntity().isCreative()) {
                    stack.shrink(1);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
            if (!TameableUtils.isTamed(rabbit) && !rabbit.level().isClientSide()) {
                if (!event.getEntity().isCreative()) {
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
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return true;
            }
        }
        return false;
    }
}

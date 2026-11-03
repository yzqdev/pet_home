package com.github.yzqdev.pethome.server.event;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.item.NetItem;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.item.Type;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import java.util.Map;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * 实体交互总入口（Fabric {@code UseEntityCallback}，双端都会进入；耗时/副作用逻辑都在 {@code !level.isClientSide()}
 * 分支内）。判定顺序：捕网抓捕 →（自家宠物）贪婪喂食 / 项圈标签 / 所有权契约 → 兔子干草块驯服或指令切换 → 实体转换（烂苹果/阴森胡萝卜）。
 */
public class PlayerInteractEntityHandler {

  /** UseEntityCallback 的处理方法，按类注释的顺序短路分发；未命中时返回 {@code PASS} 交给原版交互。 */
  public static InteractionResult onInteractWithEntity(
      Player player, InteractionHand hand, Entity target) {
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
   * 空捕网右键生物：过 {@code NetItem.canCatchMob} 黑名单/可驯服校验后，把实体完整 NBT 存进 {@code
   * PHDataComponents.ENTITY_HOLDER} 的新捕网（{@code net_has_item}），目标 discard， 新捕网交回物品栏（放不下则掉落），5 tick
   * 使用冷却。仅服务端。
   */
  private static InteractionResult handleNetCapture(
      Player player, InteractionHand hand, Level level, Entity target, ItemStack itemstack) {
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
          ItemEntity itemEntity =
              new ItemEntity(
                  player.level(), player.getX(), player.getY(), player.getZ(), newerStack);
          player.level().addFreshEntity(itemEntity);
        }
        target.discard();
        player.getCooldowns().addCooldown(itemstack.getItem(), 5);
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }

  /** 贪婪（GLUTTONOUS）附魔：手持食物右键自家宠物，按 {@code 营养值×1.5} 回血并消耗食物（创造模式不消耗）。 */
  private static InteractionResult handleGluttonousFeeding(
      Player player, LivingEntity living, ItemStack itemstack) {
    if (TameableUtils.hasEnchant(living, PHEnchantments.GLUTTONOUS)) {
      var foodProperty = itemstack.get(net.minecraft.core.component.DataComponents.FOOD);
      if (foodProperty != null && living.getHealth() < living.getMaxHealth()) {
        living.heal((float) Math.floor(foodProperty.nutrition() * 1.5F));
        if (!player.isCreative()) {
          itemstack.shrink(1);
        }
        living.playSound(
            living.getRandom().nextBoolean() ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT,
            1F,
            living.getVoicePitch());
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }

  /**
   * 项圈标签应用/替换（配置 {@code collar_tags} 开启时）：同步宠物名；旧项圈（已有附魔）的宠物 会把原附魔转写回一个掉落的项圈标签物品；随后抑制实体 tick（{@code
   * blockCollarTick}）、 清空并写入新附魔。仅服务端执行写逻辑。
   */
  private static InteractionResult handleCollarTag(
      Player player, Level level, LivingEntity living, ItemStack itemstack) {

    if (!PetHomeConfig.collarTag) {
      return InteractionResult.PASS;
    }
    if (itemstack.is(PHItemRegistry.COLLAR_TAG)) {
      if (!level.isClientSide && living.isAlive()) {
        var itemEnchantments =
            itemstack.get(net.minecraft.core.component.DataComponents.ENCHANTMENTS);
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
            reg.ifPresent(
                r -> {
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
   * 所有权契约：清空附魔、解除宠物床绑定并解除驯服（{@code setTame(false)+setOwnerUUID(null)}）。仅对原版 {@code TamableAnimal}
   * 生效（Mixin 扩展物种不适用）。
   */
  private static InteractionResult handleDeedOfOwnership(
      Player player, InteractionHand hand, Entity target, ItemStack itemstack) {
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

  /** 转换物品分发：烂苹果→马变僵尸马；阴森胡萝卜→自家兔子变邪恶兔、僵尸马变骷髅马。转换逻辑只在此处实现（物品类是 tooltip 壳）。 */
  private static void handleEntityConversions(
      Player player, InteractionHand hand, LivingEntity living, ItemStack itemstack) {
    // 马 -> 僵尸马 (烂苹果)
    if (living.getType() == EntityType.HORSE && itemstack.is(PHItemRegistry.ROTTEN_APPLE)) {
      convertHorse(player, hand, (Horse) living, itemstack, EntityType.ZOMBIE_HORSE);
    }
    // 兔子 -> 邪恶兔子 (阴森胡萝卜)
    else if (living.getType() == EntityType.RABBIT
        && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
      if (TameableUtils.isTamed(living) && TameableUtils.isPetOf(player, living)) {
        convertRabbitToEvil(player, hand, (Rabbit) living, itemstack);
      }
    }
    // 僵尸马 -> 骷髅马 (阴森胡萝卜)
    else if (living.getType() == EntityType.ZOMBIE_HORSE
        && itemstack.is(PHItemRegistry.SINISTER_CARROT)) {
      convertHorse(player, hand, (ZombieHorse) living, itemstack, EntityType.SKELETON_HORSE);
    }
  }

  /**
   * 马类转换共用流程（烂苹果马→僵尸马 / 阴森胡萝卜僵尸马→骷髅马）：掉落马铠（仅普通马有）、 复制 NBT（含名字/AI/幼年/缰绳）重建目标实体，广播转换事件后 discard
   * 原马，非创造扣物品。
   */
  private static void convertHorse(
      Player player,
      InteractionHand hand,
      AbstractHorse horse,
      ItemStack itemstack,
      EntityType<? extends AbstractHorse> resultType) {
    player.swing(hand);
    horse.playSound(SoundEvents.HORSE_DEATH, 0.8F, horse.getVoicePitch());
    horse.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, horse.getVoicePitch());

    CompoundTag horseExtras = new CompoundTag();
    if (horse instanceof Horse normalHorse && !normalHorse.getBodyArmorItem().isEmpty()) {
      normalHorse.spawnAtLocation(normalHorse.getBodyArmorItem().copy());
      normalHorse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    }
    horse.addAdditionalSaveData(horseExtras);
    spawnSneezeParticles(horse);

    AbstractHorse result = resultType.create(horse.level());

    if (result != null) {
      transferBasicData(horse, result, horseExtras);
      // 对应 NeoForge 侧 EventHooks.onLivingConvert：让其它模组感知马类转换
      ServerLivingEntityEvents.MOB_CONVERSION.invoker().onConversion(horse, result, false);
      player.level().addFreshEntity(result);
      horse.discard();
      consumeItem(player, itemstack);
    }
  }

  /** 阴森胡萝卜把自家兔子直接改为邪恶变种（{@code Rabbit.Variant.EVIL}，不重建实体）。 */
  private static void convertRabbitToEvil(
      Player player, InteractionHand hand, Rabbit rabbit, ItemStack itemstack) {
    if (rabbit.getVariant() != Rabbit.Variant.EVIL) {
      player.swing(hand);
      rabbit.playSound(SoundEvents.RABBIT_ATTACK, 0.8F, rabbit.getVoicePitch());
      rabbit.playSound(SoundEvents.ZOMBIE_INFECT, 0.8F, rabbit.getVoicePitch());
      rabbit.setVariant(Rabbit.Variant.EVIL);
      consumeItem(player, itemstack);
    }
  }

  // 辅助方法：处理数据继承
  /**
   * 转换时继承旧实体数据：牵引绳、位置朝向、NoAI、幼年态、自定义名外， 还把旧实体<b>附加存档 NBT（不含 UUID，来自
   * addAdditionalSaveData）</b>readAdditionalSaveData 进新实体并设持久化。
   */
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
      entity
          .level()
          .addParticle(
              ParticleTypes.SNEEZE,
              entity.getRandomX(1.0F),
              entity.getRandomY(),
              entity.getRandomZ(1.0F),
              0F,
              0F,
              0F);
    }
  }

  // 辅助方法：消耗物品
  private static void consumeItem(Player player, ItemStack stack) {
    if (!player.isCreative()) {
      stack.shrink(1);
    }
  }

  /**
   * 干草块驯服兔子（配置 {@code tameable_rabbit} 开启时）：已驯服受伤的兔子回 3 点血； 未驯服的兔子 50% 概率驯服（设主人、指令=停留）并冒爱心，失败冒烟。返回
   * {@code true} 表示已消费干草块。
   */
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
            ((ServerLevel) rabbit.level())
                .sendParticles(
                    ParticleTypes.HEART,
                    rabbit.getRandomX(1.0D),
                    rabbit.getRandomY() + 0.5D,
                    rabbit.getRandomZ(1.0D),
                    3,
                    d0,
                    d1,
                    d2,
                    0.02F);
          }
          ((ModifiedToBeTameable) rabbit).setTame(true);
          ((ModifiedToBeTameable) rabbit).setTameOwnerUUID(player.getUUID());
          ((IComandableMob) rabbit).setCommand(1);
        } else {
          for (int i = 0; i < 3; ++i) {
            double d0 = rabbit.getRandom().nextGaussian() * 0.02D;
            double d1 = rabbit.getRandom().nextGaussian() * 0.02D;
            double d2 = rabbit.getRandom().nextGaussian() * 0.02D;
            ((ServerLevel) rabbit.level())
                .sendParticles(
                    ParticleTypes.SMOKE,
                    rabbit.getRandomX(1.0D),
                    rabbit.getRandomY() + 0.5D,
                    rabbit.getRandomZ(1.0D),
                    3,
                    d0,
                    d1,
                    d2,
                    0.02F);
          }
        }
        return true;
      }
    }
    return false;
  }
}

package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

/**
 * 友军伤害保护判定链（配置 [friendly_fire] 的全部消费点）。 由 {@code EntityHurtHandler.handleFriendlyFire}
 * 在服务端伤害事件里调用；潜行攻击可整体绕过保护。
 */
public class FriendlyFireCommon {

  public static final TagKey<Item> BYPASS_PET =
      TagKey.create(
          Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_pet"));
  public static final TagKey<Item> BYPASS_ALL =
      TagKey.create(
          Registries.ITEM,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_all_protection"));
  public static final TagKey<EntityType<?>> GENERAL_PROTECTION =
      TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("general_protection"));
  public static final TagKey<EntityType<?>> PLAYER_PROTECTION =
      TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("player_protection"));
  public static final TagKey<EntityType<?>> BYPASSED_PROTECTION =
      TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("bypassed_entity_types"));

  public static void init() {

    PHConstants.LOG.debug("Protect children = {}", PetHomeConfig.protectChildren);
    PHConstants.LOG.debug("Protect pets from owner = {}", PetHomeConfig.protectPetsFromOwner);
    PHConstants.LOG.debug("Protect pets from pets = {}", PetHomeConfig.protectPetsFromPets);
    PHConstants.LOG.debug("Reflect damage = {}", PetHomeConfig.reflectDamage);
  }

  /** 保护判定入口：命中保护时向攻击者（玩家）弹出 actionbar 提示；返回 {@code true} 表示应取消这次伤害。 */
  public static boolean preventAttack(Entity target, DamageSource source, float amount) {

    final Entity attacker = source.getEntity();
    final boolean preventDamage = source != null && isProtected(target, attacker, amount);

    if (preventDamage
        && attacker instanceof ServerPlayer player
        && PetHomeConfig.displayHitWarning) {

      player.displayClientMessage(
          Component.literal("")
              .withStyle(ChatFormatting.AQUA)
              .append(
                  Component.translatable("notif.friendlyfire.protected", target.getName())
                      .withStyle(ChatFormatting.WHITE)),
          true);
    }

    return preventDamage;
  }

  /**
   * 完整保护判定链（依次短路）： {@code no_protection_entity} 白名单 → 空目标/空攻击者/潜行绕过 → {@code can_hurt_all} 物品白名单 →
   * {@code other_should_protect_entity} 强保护实体 → 玩家攻击 {@code player_cant_hurt_entity} 实体 →
   * 主人打宠物（reflectDamage 时把伤害反弹给攻击者自己）→ 同主人宠物互伤 → 双向结盟队伍保护 （respectTeamRules 时尊重队伍友伤开关）→
   * 玩家攻击幼年非敌对生物。
   */
  public static boolean isProtected(Entity victim, Entity attacker, float amount) {

    if (PetHomeConfig.noProtectionEntity != null
        && PetHomeConfig.noProtectionEntity.contains(victim.getType())) {

      return false;
    }

    // Null targets or sources can not be protected. Sneaking will bypass this mod
    // entirely.
    if (victim == null || attacker == null || attacker.isCrouching()) {

      return false;
    }

    // The item used by the attacker.
    final ItemStack heldItem =
        attacker instanceof LivingEntity attackerLiving
            ? attackerLiving.getMainHandItem()
            : ItemStack.EMPTY;

    // Items in the bypass all tag will always cause damage.
    if (PetHomeConfig.canHurtAllItem != null
        && PetHomeConfig.canHurtAllItem.contains(heldItem.getItem())) {

      return false;
    }

    // Mobs with general protection tag are almost always protected.
    if (PetHomeConfig.otherShouldProtectionEntity != null
        && PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType())) {

      return true;
    }

    // Mobs with player protection are protected from players.
    if (attacker instanceof Player player
        && PetHomeConfig.playerCantHurtEntity != null
        && PetHomeConfig.playerCantHurtEntity.contains(victim.getType())) {

      return true;
    }

    // Gets the pet owner ID, will be null if not a pet mob.
    final UUID ownerId = getOwner(victim);

    if (ownerId != null
        && PetHomeConfig.canHurtPetItem != null
        && !PetHomeConfig.canHurtPetItem.contains(heldItem.getItem())) {

      // Protects owners from hurting their pets.
      if (PetHomeConfig.protectPetsFromOwner && ownerId.equals(attacker.getUUID())) {

        // Reflection causes players to hurt themselves instead.
        if (PetHomeConfig.reflectDamage) {

          attacker.hurt(attacker.level().damageSources().generic(), amount);
        }

        return true;
      }

      // Protect pets from pets with the same owner.
      else if (PetHomeConfig.protectPetsFromPets && ownerId.equals(getOwner(attacker))) {

        return true;
      }
    }

    if (PetHomeConfig.protectTeamMembers && isOnProtectedTeam(attacker, victim)) {

      return true;
    }

    // Check if child mobs can be killed.
    if (PetHomeConfig.protectChildren
        && attacker instanceof Player
        && !(victim instanceof Enemy)
        && victim instanceof AgeableMob agable
        && agable.isBaby()
        && !attacker.isCrouching()) {

      return true;
    }

    return false;
  }

  /**
   * 双方队伍是否互相结盟（视为主方队伍）；宠物无队伍时经主人（在线实体或离线档案缓存）解析有效队伍。类型按 Team 接口判定，兼容第三方模组自定义队伍（如 Goety 仆从的
   * DummyTeam），不假设原版 PlayerTeam。
   */
  public static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

    final Team attackerTeam = getEffectiveTeam(attacker);
    final Team victimTeam = getEffectiveTeam(victim);
    return attackerTeam != null
        && victimTeam != null
        && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam))
        && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
  }

  /** 实体的"有效队伍"：自身无队伍时按归属者解析（在线取主人队伍；离线则查玩家档案缓存， 仅服务端可用），供队伍保护判定覆盖"宠物没有自己的队伍"的场景。 */
  @Nullable
  public static Team getEffectiveTeam(Entity entity) {

    final Team directTeam = entity.getTeam();

    if (directTeam == null
        && entity instanceof OwnableEntity ownable
        && ownable.getOwnerUUID() != null) {

      if (ownable.getOwner() != null) {

        return ownable.getOwner().getTeam();
      }

      if (entity.level() instanceof ServerLevel server) {

        final GameProfile fetchResult =
            server.getServer().getProfileCache().get(ownable.getOwnerUUID()).orElse(null);

        if (fetchResult != null) {

          return entity.level().getScoreboard().getPlayersTeam(fetchResult.getName());
        }
      }
    }

    return directTeam;
  }

  /** 取实体主人 UUID，兼容 {@code OwnableEntity} 与 {@code AbstractHorse}（马类不实现该接口）两条分支。 */
  @Nullable
  public static UUID getOwner(Entity entity) {

    if (entity instanceof OwnableEntity ownable) {

      return ownable.getOwnerUUID();
    }

    // Thanks Mojang
    if (entity instanceof AbstractHorse horse) {

      return horse.getOwnerUUID();
    }

    return null;
  }
}

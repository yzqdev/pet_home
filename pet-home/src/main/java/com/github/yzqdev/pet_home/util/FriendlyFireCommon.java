package com.github.yzqdev.pet_home.util;

import com.github.yzqdev.pet_home.PetHomeConfig;
import com.github.yzqdev.pet_home.PetHomeMod;
import com.mojang.authlib.GameProfile;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;

import javax.annotation.Nullable;
import java.util.UUID;

public class FriendlyFireCommon {

   public  static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM,  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_pet"));
   public  static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_all_protection"));
   public  static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "general_protection"));
   public  static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "player_protection"));
   public  static final TagKey<EntityType<?>> BYPASSED_PROTECTION = TagKey.create(Registries.ENTITY_TYPE,GlobalUtil.res( "bypassed_entity_types"));



    public static void init() {

        Constants.LOG.debug("Protect children = {}",PetHomeConfig.protectChildren);
        Constants.LOG.debug("Protect pets from owner = {}", PetHomeConfig.protectPetsFromOwner);
        Constants.LOG.debug("Protect pets from pets = {}",PetHomeConfig.protectPetsFromPets);
        Constants.LOG.debug("Reflect damage = {}",PetHomeConfig.reflectDamage);
    }

    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player &&PetHomeConfig.displayHitWarning) {

            player.displayClientMessage(Component.literal( "").withStyle(ChatFormatting.AQUA).append( Component.translatable("notif.friendlyfire.protected", target.getName()).withStyle(ChatFormatting.WHITE)), true);
        }

        return preventDamage;
    }

   public  static boolean isProtected(Entity victim, Entity attacker, float amount) {

        if (PetHomeConfig.noProtectionEntity.contains(victim.getType()) ) {

            return false;
        }

        // Null targets or sources can not be protected. Sneaking will bypass this mod
        // entirely.
        if (victim == null || attacker == null || attacker.isCrouching()) {

            return false;
        }

        // The item used by the attacker.
        final ItemStack heldItem = attacker instanceof LivingEntity attackerLiving ? attackerLiving.getMainHandItem() : ItemStack.EMPTY;

        // Items in the bypass all tag will always cause damage.
        if (PetHomeConfig.canHurtAllItem.contains(heldItem.getItem()) ) {

            return false;
        }

        // Mobs with general protection tag are almost always protected.
        if (PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType()) ) {

            return true;
        }

        // Mobs with player protection are protected from players.
        if (attacker instanceof Player player && PetHomeConfig.playerCantHurtEntity.contains(victim.getType()) ) {

            return true;
        }

        // Gets the pet owner ID, will be null if not a pet mob.
        final UUID ownerId = getOwner(victim);

        if (ownerId != null && !PetHomeConfig.canHurtPetItem.contains(heldItem.getItem() )) {

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
        if (PetHomeConfig.protectChildren && attacker instanceof Player && !(victim instanceof Enemy) && victim instanceof AgeableMob agable && agable.isBaby() && !attacker.isCrouching()) {

            return true;
        }

        return false;
    }

   public  static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

        final PlayerTeam attackerTeam = getEffectiveTeam(attacker);
        final PlayerTeam victimTeam = getEffectiveTeam(victim);
        return attackerTeam != null && victimTeam != null && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam)) && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
    }

    @Nullable
   public  static PlayerTeam getEffectiveTeam(Entity entity) {

        final PlayerTeam directTeam = entity.getTeam();

        if (directTeam == null && entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {

            if (ownable.getOwner() != null) {

                return ownable.getOwner().getTeam();
            }

            if (entity.level() instanceof ServerLevel server) {

                final GameProfile fetchResult = server.getServer().getProfileCache().get(ownable.getOwnerUUID()).orElse(null);

                if (fetchResult != null) {

                    return entity.level().getScoreboard().getPlayersTeam(fetchResult.getName());
                }
            }
        }

        return directTeam;
    }

    @Nullable
   public  static UUID getOwner(Entity entity) {

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
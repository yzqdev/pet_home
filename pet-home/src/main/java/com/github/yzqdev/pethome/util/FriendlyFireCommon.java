package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.authlib.GameProfile;
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

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 友军伤害保护判定链（由 {@code EntityHurtHandler.onLivingAttack} 调用）：
 * 实体白名单 → 物品白名单 → 主人保护（可反弹伤害）→ 同主人宠物互护 → 队伍保护 → 幼崽保护；
 * 潜行攻击整体绕过。配置值读 {@link PetHomeConfig} 的静态缓存。
 */
public class FriendlyFireCommon {

   public  static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM,  new ResourceLocation(PetHomeMod.MODID, "bypass_pet"));
   public  static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, new ResourceLocation(PetHomeMod.MODID, "bypass_all_protection"));
   public  static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "general_protection"));
   public  static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "player_protection"));
   public  static final TagKey<EntityType<?>> BYPASSED_PROTECTION = TagKey.create(Registries.ENTITY_TYPE,GlobalUtil.res( "bypassed_entity_types"));



    public static void init() {

       PHConstants.LOG.debug("Protect children = {}",PetHomeConfig.protectChildren);
        PHConstants.LOG.debug("Protect pets from owner = {}", PetHomeConfig.protectPetsFromOwner);
        PHConstants.LOG.debug("Protect pets from pets = {}",PetHomeConfig.protectPetsFromPets);
        PHConstants.LOG.debug("Reflect damage = {}",PetHomeConfig.reflectDamage);
    }

    /** 判定命中是否应被取消；取消且攻击者是服务端玩家时按配置弹行动栏提示。 */
    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player &&PetHomeConfig.displayHitWarning) {

            player.displayClientMessage(Component.literal( "").withStyle(ChatFormatting.AQUA).append( Component.translatable("notif.friendlyfire.protected", target.getName()).withStyle(ChatFormatting.WHITE)), true);
        }

        return preventDamage;
    }

    /** 保护判定主体（顺序见类注释；{@code reflectDamage} 开启时主人打宠物会改为对自己结算等量伤害）。 */
   public  static boolean isProtected(Entity victim, Entity attacker, float amount) {

        if (PetHomeConfig.noProtectionEntity!=null&&PetHomeConfig.noProtectionEntity.contains(victim.getType()) ) {

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
        if (PetHomeConfig.canHurtAllItem!=null&&PetHomeConfig.canHurtAllItem.contains(heldItem.getItem()) ) {

            return false;
        }

        // Mobs with general protection tag are almost always protected.
        if (PetHomeConfig.otherShouldProtectionEntity!=null&&PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType()) ) {

            return true;
        }

        // Mobs with player protection are protected from players.
        if (attacker instanceof Player player &&PetHomeConfig.playerCantHurtEntity!=null&& PetHomeConfig.playerCantHurtEntity.contains(victim.getType()) ) {

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

    /** 双方队伍互相结盟即视为保护；{@code respectTeamRules} 开启时尊重队伍自身「允许友伤」设置。类型按 Team 接口判定，兼容第三方模组自定义队伍（如 Goety 仆从的 DummyTeam），不假设原版 PlayerTeam。 */
   public  static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

        final Team attackerTeam = getEffectiveTeam(attacker);
        final Team victimTeam = getEffectiveTeam(victim);
        return attackerTeam != null && victimTeam != null && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam)) && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
    }

    /** 取实体生效队伍：自身无队伍时回退到主人的队伍（OwnableEntity 路径，含服务端按 UUID 反查 GameProfile）。 */
    @Nullable
   public  static Team getEffectiveTeam(Entity entity) {

        final Team directTeam = entity.getTeam();

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

    /** 取归属 UUID：OwnableEntity（含 ModifiedToBeTameable 的 Mixin 桥接）或马类（AbstractHorse 不实现该接口）。 */
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
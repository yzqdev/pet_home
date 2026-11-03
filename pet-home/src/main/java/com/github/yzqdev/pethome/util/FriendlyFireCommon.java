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

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @author yzqdev
 */
public class FriendlyFireCommon {

   /** 手持物品标签：攻击其他玩家宠物不触发宠物保护（general 保护仍生效）。 */
   public  static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM,  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_pet"));
   /** 手持物品标签：完全绕过本模组全部保护。 */
   public  static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_all_protection"));
   public  static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "general_protection"));
   public  static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res(  "player_protection"));
   public  static final TagKey<EntityType<?>> BYPASSED_PROTECTION = TagKey.create(Registries.ENTITY_TYPE,GlobalUtil.res( "bypassed_entity_types"));



    public static void init() {

       PHConstants.LOG.debug("Protect children = {}",PetHomeConfig.protectChildren);
        PHConstants.LOG.debug("Protect pets from owner = {}", PetHomeConfig.protectPetsFromOwner);
        PHConstants.LOG.debug("Protect pets from pets = {}",PetHomeConfig.protectPetsFromPets);
        PHConstants.LOG.debug("Reflect damage = {}",PetHomeConfig.reflectDamage);
    }

    /**
     * 伤害事件入口：判定该次伤害是否应被友好保护取消（内部走 {@link #isProtected} 判定链）。
     * 命中保护时向攻击方玩家（仅 ServerPlayer 且开启配置）在动作栏显示受保护提示。
     * 挂在 LivingHurtHandler 的高优先级伤害事件上。
     */
    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player &&PetHomeConfig.displayHitWarning) {

            player.displayClientMessage(Component.literal( "").withStyle(ChatFormatting.AQUA).append( Component.translatable("notif.friendlyfire.protected", target.getName()).withStyle(ChatFormatting.WHITE)), true);
        }

        return preventDamage;
    }

    /**
     * 友好保护判定链（按顺序短路）：豁免实体类型配置 → 空/潜行攻击者 → bypass_all 物品 →
     * general_protection 实体标签（无条件保护）→ player_protection 标签（仅对玩家）→
     * 主人不打自家宠物（reflectDamage 时反弹等额 generic 伤害）→ 同主宠物互不伤害 →
     * 队伍互相结盟保护 → 玩家打非敌对幼年生物。潜行攻击可整体绕过本模组保护。
     */
   public  static boolean isProtected(Entity victim, Entity attacker, float amount) {

        if (PetHomeConfig.noProtectionEntity!=null&&PetHomeConfig.noProtectionEntity.contains(victim.getType()) ) {

            return false;
        }

        // 目标或来源为 null 时无法判定保护；潜行攻击完全绕过本模组的保护规则。
        if (victim == null || attacker == null || attacker.isCrouching()) {

            return false;
        }

        // 攻击者主手物品。
        final ItemStack heldItem = attacker instanceof LivingEntity attackerLiving ? attackerLiving.getMainHandItem() : ItemStack.EMPTY;

        // 手持 bypass_all_protection 标签物品时始终可造成伤害。
        if (PetHomeConfig.canHurtAllItem!=null&&PetHomeConfig.canHurtAllItem.contains(heldItem.getItem()) ) {

            return false;
        }

        // 在 general_protection 标签中的生物几乎总是受保护。
        if (PetHomeConfig.otherShouldProtectionEntity!=null&&PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType()) ) {

            return true;
        }

        // 在 player_protection 标签中的生物不受玩家伤害。
        if (attacker instanceof Player player &&PetHomeConfig.playerCantHurtEntity!=null&& PetHomeConfig.playerCantHurtEntity.contains(victim.getType()) ) {

            return true;
        }

        // 获取宠物的主人 ID；非宠物生物返回 null。
        final UUID ownerId = getOwner(victim);

        if (ownerId != null && !PetHomeConfig.canHurtPetItem.contains(heldItem.getItem())) {

            // 保护主人不被自家宠物判定所伤（即主人无法伤害自家宠物）。
            if (PetHomeConfig.protectPetsFromOwner && ownerId.equals(attacker.getUUID())) {

                // 开启反弹时，让攻击者转而伤害自己。
                if (PetHomeConfig.reflectDamage) {

                    attacker.hurt(attacker.level().damageSources().generic(), amount);
                }

                return true;
            }

            // 保护同主人的宠物之间互相伤害。
            else if (PetHomeConfig.protectPetsFromPets && ownerId.equals(getOwner(attacker))) {

                return true;
            }
        }

        if (PetHomeConfig.protectTeamMembers && isOnProtectedTeam(attacker, victim)) {

            return true;
        }

        // 幼年动物保护：非敌对生物的幼体不可被玩家击杀。
        if (PetHomeConfig.protectChildren && attacker instanceof Player && !(victim instanceof Enemy) && victim instanceof AgeableMob agable && agable.isBaby() && !attacker.isCrouching()) {

            return true;
        }

        return false;
    }

    /** 双方队伍互相结盟即视为受保护；respectTeamRules 开启时再尊重受害者队伍的 friendlyFire 设置。类型按 Team 接口判定，兼容第三方模组自定义队伍（如 Goety 仆从的 DummyTeam），不假设原版 PlayerTeam。 */
   public  static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

        final Team attackerTeam = getEffectiveTeam(attacker);
        final Team victimTeam = getEffectiveTeam(victim);
        return attackerTeam != null && victimTeam != null && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam)) && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
    }

    /**
     * 解析实体的「有效队伍」：自身无队伍时向上继承——先看在线主人的队伍，
     * 主人离线则按 UUID 查 profile 缓存再取记分板队伍（仅服务端）；使宠物的保护随主人队伍生效。
     */
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

    /** 取实体主人 UUID；马类未实现 OwnableEntity，需单独走 AbstractHorse#getOwnerUUID。 */
    @Nullable
   public  static UUID getOwner(Entity entity) {

        if (entity instanceof OwnableEntity ownable) {

            return ownable.getOwnerUUID();
        }


        if (entity instanceof AbstractHorse horse) {

            return horse.getOwnerUUID();
        }

        return null;
    }
}
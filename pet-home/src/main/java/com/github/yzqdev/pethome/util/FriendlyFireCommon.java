package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 友好保护判定核心（EntityHurtHandler 伤害管线两处调用）：主人不打自家宠物、同主人宠物互不打、
 * 保护幼年动物、队伍规则；白/黑名单由配置列表 + {@code pet_home:*} 标签共同决定。
 */
public class FriendlyFireCommon {

    public static final TagKey<Item> BYPASS_PET = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_pet"));
    public static final TagKey<Item> BYPASS_ALL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "bypass_all_protection"));
    public static final TagKey<EntityType<?>> GENERAL_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("general_protection"));
    public static final TagKey<EntityType<?>> PLAYER_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("player_protection"));
    public static final TagKey<EntityType<?>> BYPASSED_PROTECTION = TagKey.create(Registries.ENTITY_TYPE, GlobalUtil.res("bypassed_entity_types"));


    public static void init() {

        PHConstants.LOG.debug("Protect children = {}", PetHomeConfig.protectChildren);
        PHConstants.LOG.debug("Protect pets from owner = {}", PetHomeConfig.protectPetsFromOwner);
        PHConstants.LOG.debug("Protect pets from pets = {}", PetHomeConfig.protectPetsFromPets);
        PHConstants.LOG.debug("Reflect damage = {}", PetHomeConfig.reflectDamage);
    }

    /** 保护判定入口：{@code isProtected} 为真时取消攻击；攻击者是服务端玩家且配置开启时发"目标受保护"提示。 */
    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player && PetHomeConfig.displayHitWarning) {

            player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.AQUA).append(Component.translatable("notif.friendlyfire.protected", target.getName()).withStyle(ChatFormatting.WHITE)), true);
        }

        return preventDamage;
    }

    /**
     * 保护判定链（按序短路）：{@code noProtectionEntity} 黑名单直通 → 无目标/无来源/潜行直通（潜行完全绕过本模组）→
     * {@code canHurtAllItem} 物品直通 → {@code otherShouldProtectionEntity}/{@code playerCantHurtEntity} 白名单直保 →
     * 主人伤自家宠物（reflectDamage 时反弹）→ 同主人宠物互伤 → 队伍互保 → 幼年动物保护。
     */
    public static boolean isProtected(Entity victim, Entity attacker, float amount) {

        if (PetHomeConfig.noProtectionEntity != null && PetHomeConfig.noProtectionEntity.contains(victim.getType())) {

            return false;
        }

        // 目标或来源为 null 时无法判定保护；潜行攻击完全绕过本模组的保护规则。
        if (victim == null || attacker == null || attacker.isCrouching()) {

            return false;
        }

        // 攻击者主手物品。
        final ItemStack heldItem = attacker instanceof LivingEntity attackerLiving ? attackerLiving.getMainHandItem() : ItemStack.EMPTY;

        // 手持 bypass_all_protection 标签物品时始终可造成伤害。
        if (PetHomeConfig.canHurtAllItem!=null&&PetHomeConfig.canHurtAllItem.contains(heldItem.getItem())) {

            return false;
        }

        // 在 general_protection 标签中的生物几乎总是受保护。
        if (PetHomeConfig.otherShouldProtectionEntity!=null&&PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType())) {

            return true;
        }

        // 在 player_protection 标签中的生物不受玩家伤害。
        if (attacker instanceof Player player && PetHomeConfig.playerCantHurtEntity != null && PetHomeConfig.playerCantHurtEntity.contains(victim.getType())) {

            return true;
        }

        // 获取宠物的主人 ID；非宠物生物返回 null。
        final UUID ownerId = getOwner(victim);

        if (ownerId != null &&PetHomeConfig.canHurtPetItem!=null&& !PetHomeConfig.canHurtPetItem.contains(heldItem.getItem())) {

            // 保护主人不被自家宠物判定所伤（即主人无法伤害自家宠物）。
            if (PetHomeConfig.protectPetsFromOwner && ownerId.equals(attacker.getUUID())) {

                // 开启反弹时，让攻击者转而伤害自己。
                if (PetHomeConfig.reflectDamage && attacker.level() instanceof ServerLevel serverLevel) {

                    attacker.hurtServer(serverLevel, serverLevel.damageSources().generic(), amount);
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

    /** 双方队伍结盟且（respectTeamRules 时）受害者队伍未开友伤 → 受保护；任一方无队伍则不保。类型按 Team 接口判定，兼容第三方模组自定义队伍（如 Goety 仆从的 DummyTeam），不假设原版 PlayerTeam。 */
    public static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

        final Team attackerTeam = getEffectiveTeam(attacker);
        final Team victimTeam = getEffectiveTeam(victim);
        return attackerTeam != null && victimTeam != null && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam)) && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
    }

    /** 实体"生效队伍"：自身无队伍时回退到主人（OwnableEntity）的队伍——主人在线取其 scoreboard 队伍。无队伍返回 {@code null}。 */
    @Nullable
    public static Team getEffectiveTeam(Entity entity) {

        final Team directTeam = entity.getTeam();

        // 26.1: OwnableEntity 不再有 getOwnerUUID，改用 getOwnerReference
        if (directTeam == null && entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {

            if (ownable.getOwner() != null) {

                return ownable.getOwner().getTeam();
            }

            if (entity.level() instanceof ServerLevel server) {

                ServerPlayer ownerPlayer = server.getServer().getPlayerList().getPlayer(ownable.getOwnerReference().getUUID());

                if (ownerPlayer != null) {

                    return entity.level().getScoreboard().getPlayersTeam(ownerPlayer.getScoreboardName());
                }
            }
        }

        return directTeam;
    }

    /** 归属 UUID（OwnableEntity/马类）；26.1 走 {@code getOwnerReference}（旧 getOwnerUUID 已移除）。无主返回 {@code null}。 */
    @Nullable
    public static UUID getOwner(Entity entity) {

        // 26.1: OwnableEntity/AbstractHorse 不再有 getOwnerUUID，改用 getOwnerReference
        if (entity instanceof OwnableEntity ownable) {

            return ownable.getOwnerReference() != null ? ownable.getOwnerReference().getUUID() : null;
        }

        // AbstractHorse 未实现 OwnableEntity，需单独取主人引用。
        if (entity instanceof AbstractHorse horse) {

            return horse.getOwnerReference() != null ? horse.getOwnerReference().getUUID() : null;
        }

        return null;
    }
}
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

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 友军伤害保护判定链（{@code EntityHurtHandler.handleFriendlyFire} 与横扫 Mixin 共用）。
 * 判定顺序见 {@link #isProtected}：实体白名单 → 潜行/物品白名单绕过 → 标签强制保护 →
 * 主人打宠物 / 同主人宠物互打（可反伤）→ 队伍保护 → 幼崽保护。
 * 26.1 取主人 UUID 走 {@code getOwnerReference()}（{@code OwnableEntity} 无 {@code getOwnerUUID}）。
 * 配置白名单字段在读取前都有判空（配置未加载时为 null）。
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

    /**
     * 受击入口：命中保护规则返回 true（调用方应取消伤害）；命中方为玩家且开启
     * {@code displayHitWarning} 时同步发一条保护提示。
     *
     * @return true = 本次攻击应被取消
     */
    public static boolean preventAttack(Entity target, DamageSource source, float amount) {

        final Entity attacker = source.getEntity();
        final boolean preventDamage = source != null && isProtected(target, attacker, amount);

        if (preventDamage && attacker instanceof ServerPlayer player && PetHomeConfig.displayHitWarning) {

            player.sendSystemMessage(Component.literal("").withStyle(ChatFormatting.AQUA).append(Component.translatable("notif.friendlyfire.protected", target.getName()).withStyle(ChatFormatting.WHITE)), true);
        }

        return preventDamage;
    }

    /**
     * 完整保护判定链（顺序即优先级）：
     * ① {@code noProtectionEntity} 白名单直接放行；② 空 victim/attacker 或潜行攻击者放行；
     * ③ {@code can_hurt_all} 物品白名单放行；④ {@code other_should_protect_entity} 强制保护；
     * ⑤ {@code player_cant_hurt_entity} 玩家不可伤；⑥ 主人打宠物（可配 {@code reflectDamage}
     * 让攻击者自伤）与同主人宠物互打；⑦ 双向结盟队伍（尊重队伍友伤规则）；⑧ 非敌对幼崽。
     */
    public static boolean isProtected(Entity victim, Entity attacker, float amount) {

        if (PetHomeConfig.noProtectionEntity != null && PetHomeConfig.noProtectionEntity.contains(victim.getType())) {

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
        if (PetHomeConfig.canHurtAllItem!=null&&PetHomeConfig.canHurtAllItem.contains(heldItem.getItem())) {

            return false;
        }

        // Mobs with general protection tag are almost always protected.
        if (PetHomeConfig.otherShouldProtectionEntity!=null&&PetHomeConfig.otherShouldProtectionEntity.contains(victim.getType())) {

            return true;
        }

        // Mobs with player protection are protected from players.
        if (attacker instanceof Player player && PetHomeConfig.playerCantHurtEntity != null && PetHomeConfig.playerCantHurtEntity.contains(victim.getType())) {

            return true;
        }

        // Gets the pet owner ID, will be null if not a pet mob.
        final UUID ownerId = getOwner(victim);

        if (ownerId != null &&PetHomeConfig.canHurtPetItem!=null&& !PetHomeConfig.canHurtPetItem.contains(heldItem.getItem())) {

            // Protects owners from hurting their pets.
            if (PetHomeConfig.protectPetsFromOwner && ownerId.equals(attacker.getUUID())) {

                // Reflection causes players to hurt themselves instead.
                if (PetHomeConfig.reflectDamage && attacker.level() instanceof ServerLevel serverLevel) {

                    attacker.hurtServer(serverLevel, serverLevel.damageSources().generic(), amount);
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

    /** 双方队伍互相结盟即视为保护关系；{@code respectTeamRules} 开启时尊重原版"允许友伤"的队伍设置。类型按 Team 接口判定，兼容第三方模组自定义队伍（如 Goety 仆从的 DummyTeam），不假设原版 PlayerTeam。 */
    public static boolean isOnProtectedTeam(Entity attacker, Entity victim) {

        final Team attackerTeam = getEffectiveTeam(attacker);
        final Team victimTeam = getEffectiveTeam(victim);
        return attackerTeam != null && victimTeam != null && (attackerTeam.isAlliedTo(victimTeam) && victimTeam.isAlliedTo(attackerTeam)) && (!PetHomeConfig.respectTeamRules || !victimTeam.isAllowFriendlyFire());
    }

    /** 有效队伍：优先实体自身队伍；无队伍的可驯服实体回退到主人（在线玩家优先）的队伍。 */
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

    /** 主人 UUID（{@code getOwnerReference}，26.1 已无 {@code getOwnerUUID}）；马类单独分支；无主返回 null。 */
    @Nullable
    public static UUID getOwner(Entity entity) {

        // 26.1: OwnableEntity/AbstractHorse 不再有 getOwnerUUID，改用 getOwnerReference
        if (entity instanceof OwnableEntity ownable) {

            return ownable.getOwnerReference() != null ? ownable.getOwnerReference().getUUID() : null;
        }

        // Thanks Mojang
        if (entity instanceof AbstractHorse horse) {

            return horse.getOwnerReference() != null ? horse.getOwnerReference().getUUID() : null;
        }

        return null;
    }
}
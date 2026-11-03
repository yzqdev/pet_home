package com.github.yzqdev.pethome.server.entity;



import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 归属与驯服判定：主人 UUID 读写、{@code isPetOf}/{@code isTamed} 族、物种开关表、项圈、
 * 安全血量与迷途灯笼的卸载召回判定（原 {@code TameableUtils} 一区）。
 */
final class PetOwnershipUtils {

    private PetOwnershipUtils() {}
    /**
     * 改造物种 → {@code PetHomeConfig.tameable*} 开关的运行时视图（{@link #isTamed} 用）：
     * {@code null} 值表示该物种不是改造驯服对象（无需开关门）。马走 {@code TamableAnimal} 分支 +
     * {@link com.github.yzqdev.pethome.mixin.AbstractHorseMixin} 自身覆写 {@code isTame}，不经此表。
     * 惰性初始化，配置刷新后由 {@link PetHomeMod.CONFIG} 加载回调重算。
     */
    private static final Map<EntityType<?>, Boolean> SPECIES_TAME_GATES = new HashMap<>();

    /**
     * 判定两个实体是否归属同一主人（双向检查：任意一方是另一方的宠物即视为同主）。
     * 兼容 {@link ModifiedToBeTameable}（本模组改造的可驯服生物）与原版 {@link TamableAnimal}；
     * target 本身是玩家（主人）时也返回 true。友好保护/宠物友军判定的高频入口。
     */
    static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return hasSameOwnerAsOneWay(tameable, target) || hasSameOwnerAsOneWay(target, tameable);
    }

    private static boolean hasSameOwnerAsOneWay(Entity tameable, Entity target) {
        if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
            if (target instanceof ModifiedToBeTameable axolotl && axolotl.getTameOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof TamableAnimal otherPet && otherPet.getOwner() != null) {
                if (tamed.getOwner().equals(otherPet.getOwner())) {
                    return true;
                }
            }
            return tamed.getOwner().equals(target);
        } else if (tameable instanceof ModifiedToBeTameable axolotl && axolotl.getTameOwner() != null) {
            if (tameable instanceof TamableAnimal tamed && tamed.getOwner() != null) {
                if (tamed.getOwner().equals(axolotl.getTameOwner())) {
                    return true;
                }
            }
            if (target instanceof ModifiedToBeTameable otherPet && otherPet.getTameOwner() != null) {
                if (axolotl.getTameOwner().equals(otherPet.getTameOwner())) {
                    return true;
                }
            }
            return axolotl.getTameOwner().equals(target);
        }
        return false;
    }

    /**
     * 实体随区块卸载时是否应记入迷途灯笼召回请求（由 ServerEvents#onEntityLeaveWorld 调用）。
     * 只认「跟随状态」：三态指令开启时 command==2（跟随）才召回；三态关闭时本模组宠物按「未停留=跟随」兜底；
     * 第三方宠物（如 Alex's Mobs，未实现 IComandableMob）读其 NBT 里 {@code *Command} 键（编号 1=跟随），
     * 都没有再按「原版可驯服动物未坐下=跟随」兜底。
     * 第三方扫描会序列化一遍实体附加数据，调用方注意别在热路径高频使用（本模组宠物走内存判定，无序列化）。
     */
    static boolean shouldUnloadToLantern(LivingEntity tameable) {
        if (tameable instanceof IComandableMob commandableMob) {
            if (PetHomeMod.CONFIG.trinaryCommandSystem.get()) {
                return commandableMob.getCommand() == 2;
            }
            // 未启用三态：原版语义下「未停留」即为跟随
            return !(tameable instanceof TamableAnimal animal) || !animal.isOrderedToSit();
        }
        int foreignCommand = readForeignCommand(tameable);
        if (foreignCommand != -1) {
            return foreignCommand == 1;
        }
        return tameable instanceof TamableAnimal animal && !animal.isOrderedToSit();
    }

    /**
     * 读第三方宠物 NBT 里的 {@code *Command} 键（兼容 Alex's Mobs 等：其实体未实现 IComandableMob，
     * 编号 1=跟随）。 必须跳过本模组自己的 {@code DICommand}：它按三态编号写（2 = 跟随），
     * 与第三方编号体系不通用——三态关闭时若把自家宠物的 DICommand 当第三方键读，跟随中的宠物会被误判不召回。
     * 经 {@link com.github.yzqdev.pethome.mixin.EntityInvoker} 序列化附加数据（该方法 protected，直接调用不可达）。
     */
    private static int readForeignCommand(LivingEntity tameable) {
        CompoundTag tag = new CompoundTag();
        ((com.github.yzqdev.pethome.mixin.EntityInvoker) tameable)
                .pet_home$invokeAddAdditionalSaveData(tag);
        int command = -1;
        for (String s : tag.getAllKeys()) {
            if (s.endsWith("Command") && !NbtKeys.DI_COMMAND.equals(s) && tag.contains(s, 99)) {
                command = tag.getInt(s);
            }
        }
        return command;
    }

    /**
     * 判定实体是否为该玩家的宠物： alliedTo（团队/友军）或单向同主（entity 的主人是该玩家）。
     * 交互处理、保护判定的高频入口。
     */
    static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
    }

    /** 重算物种开关表（配置加载后调用）。 */
    static void refreshSpeciesGates() {
        SPECIES_TAME_GATES.clear();
        SPECIES_TAME_GATES.put(EntityType.AXOLOTL, PetHomeMod.CONFIG.tameableAxolotl.get());
        SPECIES_TAME_GATES.put(EntityType.FOX, PetHomeMod.CONFIG.tameableFox.get());
        SPECIES_TAME_GATES.put(EntityType.RABBIT, PetHomeMod.CONFIG.tameableRabbit.get());
        SPECIES_TAME_GATES.put(EntityType.FROG, PetHomeMod.CONFIG.tameableFrog.get());
    }

    /**
     * 判定实体是否已被驯服。对美西螈/狐狸/兔子/青蛙这四类改造生物，除 isTame 外还要求
     * 对应的「可驯服」配置开关处于开启状态（防止数据残留的旧个体被当作宠物）；
     * 其余实体按 {@link ModifiedToBeTameable} 或 {@link TamableAnimal} 的 isTame 判定。
     */
    static boolean isTamed(Entity entity) {
        // 改造实体的 Mixin 接口在运行时可能未绑定，需先 instanceof 再判 isTame
        if (entity instanceof ModifiedToBeTameable modifedToBeTameable) {
            if (!modifedToBeTameable.isTame()) {
                return false;
            }
            Boolean gate = SPECIES_TAME_GATES.get(entity.getType());
            return gate == null || gate;
        }
        return entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame();
    }

    /** 实体是否属于「可被驯服」的类型（原版 {@link TamableAnimal} 或本模组 {@link ModifiedToBeTameable} 改造生物），不关心当前是否已被驯服。 */
    static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifiedToBeTameable || entity instanceof TamableAnimal;
    }

    /** 取实体主人（在线玩家），兼容 {@link ModifiedToBeTameable} 与 {@link TamableAnimal} 两套归属体系；无主人或主人离线时返回 null。 */
    static Entity getOwnerOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwner();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwner();
        }
        return null;
    }

    /** 取主人 UUID（不要求主人在线），两套归属体系兼容；实体无归属概念时返回 null。网络鉴权（校验包伪造）与档案系统都以此为据。 */
    static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwnerUUID();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwnerUUID();
        }
        return null;
    }

    /** 写主人 UUID。注意：实体若同时命中两类接口会连写两次（正常实体只命中其一）。 */
    static void setOwnerUUIDOf(Entity entity, UUID uuid) {
        if (entity instanceof ModifiedToBeTameable) {
            ((ModifiedToBeTameable) entity).setTameOwnerUUID(uuid);
        }
        if (entity instanceof TamableAnimal) {
            ((TamableAnimal) entity).setOwnerUUID(uuid);
        }
    }

    /** 标记/清除「已佩戴项圈」状态（citadel tag 布尔键，随 sync 同步客户端）。戴上/摘下项圈时由交互处理调用。 */
    static void setHasCollar(LivingEntity enchanted, boolean collar) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(PetDataKeys.COLLAR_TAG, collar);
        PetDataSync.sync(enchanted, tag);
    }

    /** 实体是否已佩戴本模组项圈（决定附魔是否生效、tooltip 是否显示附魔列表等）。 */
    static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(PetDataKeys.COLLAR_TAG) && tag.getBoolean(PetDataKeys.COLLAR_TAG);
    }

    /** health_boost 存档保护：实体卸载/离开世界时记录的「安全血量」（避免下限回退导致血量异常），重新 join 时恢复用。 */
    static double getSafePetHealth(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getDouble(PetDataKeys.SAFE_PET_HEALTH);
    }

    static void setSafePetHealth(LivingEntity enchanted, double health) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putDouble(PetDataKeys.SAFE_PET_HEALTH, health);
        PetDataSync.sync(enchanted, tag);
    }
}

package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 归属与驯服判定：主人 UUID 解析、{@code isPetOf}/{@code isTamed} 族、物种开关表、项圈、
 * 安全血量与迷途灯笼的卸载召回判定（原 {@code TameableUtils} 一区）。
 */
final class PetOwnershipUtils {

    private PetOwnershipUtils() {}
    /**
     * 改造物种 → {@code PetHomeConfig.tameable*} 开关的运行时视图（{@link #isTamed} 用）：
     * {@code null} 值表示该物种不是改造驯服对象（无需开关门）。马走 {@code TamableAnimal} 分支 +
     * {@link com.github.yzqdev.pethome.mixin.AbstractHorseMixin} 自身覆写 {@code isTame}，不经此表。
     * 惰性初始化，配置刷新后由 {@code PetHomeConfig.onLoad} 重算。
     */
    private static final Map<EntityType<?>, Boolean> SPECIES_ENCHANT_GATES = new HashMap<>();

    /**
     * 取实体的驯服主人 UUID，兼容两类归属载体：本模组改造实体（{@link ModifiedToBeTameable}）与原版 {@link TamableAnimal}。
     * 26.1：{@code TamableAnimal#getOwnerUUID} 已移除，改从 {@code getOwnerReference} 解析。
     *
     * @return 无主人或非可驯服实体时为 {@code null}
     */
    static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwnerUUID();
        }

        // 26.1: TamableAnimal 不再有 getOwnerUUID，改用 getOwnerReference
        if (entity instanceof TamableAnimal) {
            var ownerReference = ((TamableAnimal) entity).getOwnerReference();
            return ownerReference != null ? ownerReference.getUUID() : null;
        }
        return null;
    }

    /** 宠物是否戴有项圈（citadel tag {@code HasPetCollar} 键，由 setEnchantmentTag 在写入附魔时置位） */
    static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(PetDataKeys.COLLAR_TAG) && tag.getBooleanOr(PetDataKeys.COLLAR_TAG, false);
    }

    /**
     * 是否属于「走失、应当被迷途灯笼召回」的宠物——只认**跟随状态**：
     * <ul>
     *   <li>本模组三态宠物（{@link IComandableMob}）：三态开启时 command == 2（跟随）；关闭时按原版语义「未停留 = 跟随」兜底。</li>
     *   <li>第三方宠物（Alex's Mobs 等）：读它们自己的 {@code *Command} 键（其编号 1 = 跟随）。</li>
     *   <li>原版可驯服动物（无指令系统）：未停留 = 跟随。</li>
     * </ul>
     * 停留 / 游走的宠物是玩家有意留在此处，不召回；无跟随模式可言的改造宠物（马等）同样不召回。
     */
    static boolean shouldUnloadToLantern(LivingEntity tameable) {
        if (tameable instanceof IComandableMob commandableMob) {
            if (PetHomeConfig.trinaryCommandSystem) {
                // 三态指令编号：0 = 游走，1 = 停留，2 = 跟随
                return commandableMob.getCommand() == PetDataKeys.COMMAND_FOLLOW;
            }
            // 未启用三态：原版语义下「未停留」即为跟随
            return !(tameable instanceof TamableAnimal animal) || !animal.isOrderedToSit();
        }
        // 第三方宠物兼容（alexs mobs 等）：它们的 *Command 编号与本模组三态编号不同（1 = 跟随）
        int foreignCommand = readForeignCommand(tameable);
        if (foreignCommand != -1) {
            return foreignCommand == PetDataKeys.FOREIGN_COMMAND_FOLLOW;
        }
        return tameable instanceof TamableAnimal animal && !animal.isOrderedToSit();
    }

    /**
     * 读第三方宠物 NBT 里的 {@code *Command} 键。
     * 必须跳过本模组自己的 {@link NbtKeys#DI_COMMAND}：它按三态编号写（2 = 跟随），与第三方编号体系不通用。
     * 经 {@link com.github.yzqdev.pethome.mixin.EntityInvoker} 只序列化附加数据——
     * {@code saveWithoutId} 会连带打包 Brain/属性/药效等大块状态，本方法在实体卸载路径上调用，不值得。
     */
    private static int readForeignCommand(LivingEntity tameable) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, tameable.registryAccess());
        ((com.github.yzqdev.pethome.mixin.EntityInvoker) tameable).pet_home$invokeAddAdditionalSaveData(output);
        CompoundTag tag = output.buildResult();
        int command = -1;
        for (String s : tag.keySet()) {
            if (s.endsWith("Command") && !NbtKeys.DI_COMMAND.equals(s) && tag.get(s) instanceof NumericTag) {
                command = tag.getIntOr(s, 0);
            }
        }
        return command;
    }

    /**
     * 读取存档时记录的「安全血量」（含 health_boost 提升后的血上限）：实体重新加载进世界时
     * （ServerEvent.onEntityJoinWorldEvent）用它在属性修饰符尚未重挂前恢复血量，防止血上限回缩掉血。
     */
    static double getSafePetHealth(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getDoubleOr(PetDataKeys.SAFE_PET_HEALTH, 0.0);
    }

    /** 重算物种开关表（{@link PetHomeConfig} 配置加载后调用）。 */
    static void refreshSpeciesGates() {
        SPECIES_ENCHANT_GATES.clear();
        SPECIES_ENCHANT_GATES.put(EntityType.AXOLOTL, PetHomeConfig.tameableAxolotl);
        SPECIES_ENCHANT_GATES.put(EntityType.FOX, PetHomeConfig.tameableFox);
        SPECIES_ENCHANT_GATES.put(EntityType.RABBIT, PetHomeConfig.tameableRabbit);
        SPECIES_ENCHANT_GATES.put(EntityType.FROG, PetHomeConfig.tameableFrog);
    }

    /**
     * 实体卸载进世界数据前记录当前血量（ServerEvent.onEntityLeaveWorld，仅 health_boost 附魔触发），与 {@link #getSafePetHealth} 配对。
     */
    static void setSafePetHealth(LivingEntity enchanted, double health) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putDouble(PetDataKeys.SAFE_PET_HEALTH, health);
        PetDataSync.sync(enchanted, tag);
    }

    /**
     * 实体是否处于「已驯服」状态。本模组改造的四个物种（美西螈/狐狸/兔子/青蛙）额外受对应
     * {@code PetHomeConfig.tameable*} 开关约束：开关关闭时即使实例上有驯服数据也视为未驯服
     * （这些实体由 mixin 挂数据槽，运行时可能带脏状态）。其余实体走常规判定。
     */
    static boolean isTamed(Entity entity) {
        // 改造实体的 Mixin 接口在运行时可能未绑定，需先 instanceof 再判 isTame
        if (entity instanceof ModifiedToBeTameable modifiedToBeTameable) {
            if (!modifiedToBeTameable.isTame()) {
                return false;
            }
            Boolean gate = SPECIES_ENCHANT_GATES.get(entity.getType());
            return gate == null || gate;
        }
        return entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame();
    }

    /**
     * 玩家视角的「自家宠物」判定：{@code entity} 与玩家结盟，或 entity 的主人就是该玩家
     * （单向归属，见 {@link #hasSameOwnerAsOneWay}）。网络包鉴权、交互分发、友好保护等均以此为入口。
     */
    static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
    }

    /** 单向归属判定：tameable 的主人是 target（target 为另一只宠物时比对双方 owner），或 target 就是主人本人。 */
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

    /** 双向同主人判定（两方向任一成立即可），光环治疗、引力、敌对排除等团队逻辑用。 */
    static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return hasSameOwnerAsOneWay(tameable, target) || hasSameOwnerAsOneWay(target, tameable);
    }

    /** 实体是否属于「可被驯服改造/原版可驯服」的范畴（只看类型，不看是否已驯服），事件层用粗筛。 */
    static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifiedToBeTameable || entity instanceof TamableAnimal;
    }

    /** 取实体主人的实体实例（在线/已加载才非 null），两类归属载体兼容，与 {@link #getOwnerUUIDOf} 对应。 */
    static Entity getOwnerOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwner();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwner();
        }
        return null;
    }
}

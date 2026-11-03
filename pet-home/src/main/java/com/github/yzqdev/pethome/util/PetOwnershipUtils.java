package com.github.yzqdev.pethome.util;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.NbtKeys;
import net.minecraft.nbt.NumericTag;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;

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
     * 惰性初始化，配置刷新后由 {@code PetHomeConfig.refresh} 重算。
     */
    private static final Map<EntityType<?>, Boolean> SPECIES_TAME_GATES = new HashMap<>();

    /** 归属 UUID：兼容本 mod 改造动物（ModifiedToBeTameable）与原版 TamableAnimal；非驯服实体返回 {@code null} */
    static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwnerUUID();
        }

        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwnerUUID();
        }
        return null;
    }

    static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(PetDataKeys.COLLAR_TAG) && tag.getBoolean(PetDataKeys.COLLAR_TAG);
    }

    /**
     * 卸载时是否应记入迷途灯笼（召回等待）。只认「跟随状态」：
     * 三态指令开启时 command==2（跟随）才召回；关闭时读实体 NBT 里任意 {@code *Command} 键
     * （兼容 Alex's Mobs 等第三方宠物，其编号 1=跟随，跳过本模组的 DICommand），
     * 都没有则按原版 TamableAnimal「未坐下=跟随」兜底。
     * 由 ServerEvent.onEntityLeaveWorld 在实体随区块卸载时调用。
     */
    static boolean shouldUnloadToLantern(LivingEntity tameable) {
        if (tameable instanceof IComandableMob commandableMob) {
            if (PetHomeConfig.trinaryCommandSystem) {
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
     * 经 {@link com.github.yzqdev.pethome.mixin.EntityInvoker} 只序列化附加数据——
     * {@code saveWithoutId} 会连带打包 Brain/属性/药效等大块状态，本方法在实体卸载路径上调用，不值得。
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

    /** health_boost 宠物的「卸载前安全血量」：离开世界时记录、join 时用它抬回当前血量（防血上限回落扣血） */
    static double getSafePetHealth(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getDouble(PetDataKeys.SAFE_PET_HEALTH);
    }

    static void setSafePetHealth(LivingEntity enchanted, double health) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putDouble(PetDataKeys.SAFE_PET_HEALTH, health);
        PetDataSync.sync(enchanted, tag);
    }

    /** 重算物种开关表（{@link PetHomeConfig} 配置加载后调用）。 */
    static void refreshSpeciesGates() {
        SPECIES_TAME_GATES.clear();
        SPECIES_TAME_GATES.put(EntityType.AXOLOTL, PetHomeConfig.tameableAxolotl);
        SPECIES_TAME_GATES.put(EntityType.FOX, PetHomeConfig.tameableFox);
        SPECIES_TAME_GATES.put(EntityType.RABBIT, PetHomeConfig.tameableRabbit);
        SPECIES_TAME_GATES.put(EntityType.FROG, PetHomeConfig.tameableFrog);
    }

    /**
     * 驯服判定。美西螈/狐狸/兔子/青蛙是本 mod 经 mixin 改造的可驯服动物，
     * 需 {@code ModifiedToBeTameable.isTame()} 且对应配置开关（tameable_axolotl 等）同时开启；
     * 其余实体按 ModifiedToBeTameable / TamableAnimal 的 isTame 判定。
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

    /**
     * player 是否是 entity 的主人（或友军）：team 友军判定，或单向 owner 链匹配。
     * 交互/保护/传送判定的高频入口，client/server 两端都可用。
     */
    static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
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

    static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return hasSameOwnerAsOneWay(tameable, target) || hasSameOwnerAsOneWay(target, tameable);
    }

    /** 理论上可驯服（无需已驯服）：本 mod 改造动物或原版 TamableAnimal */
    static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifiedToBeTameable || entity instanceof TamableAnimal;
    }

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

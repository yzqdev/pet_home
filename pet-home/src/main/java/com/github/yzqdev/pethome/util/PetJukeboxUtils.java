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
 * 跟随唱片机（disc_jockey）数据与魅力（charisma）交易折扣（原 {@code TameableUtils} 八区）。
 */
final class PetJukeboxUtils {

    private PetJukeboxUtils() {}
    /** charisma（魅力，自 1.20 移植）对主人的交易折扣加成：25 格内自家宠物的 10×等级之和，上限 50。 */
    static int getCharismaBonusForOwner(Player player) {
        Predicate<Entity> pet = (animal) -> PetOwnershipUtils.isTamed(animal) && PetOwnershipUtils.isPetOf(player, animal);
        List<LivingEntity> list =
                player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(25, 8, 25),
                        net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.and(pet));
        int charismas = 0;
        for (LivingEntity entity : list) {
            charismas += 10 * PetEnchantmentUtils.getEnchantLevel(entity, PHEnchantments.CHARISMA);
        }
        return Math.min(charismas, 50);
    }

    /** disc_jockey 唱片机跟随者的 UUID（未设置返回 null），citadel tag 存储。 */
    @Nullable
    static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.read(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC).orElse(null);
    }

    static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.store(NbtKeys.JUKEBOX_FOLLOWER_UUID, UUIDUtil.CODEC, id);
        PetDataSync.sync(enchanted, tag);
    }

    /** disc_jockey 唱片机当前播放的唱片 ItemStack（整体序列化到 citadel tag）；未设置返回 EMPTY。 */
    static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.read(NbtKeys.JUKEBOX_FOLLOWER_DISC, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    /**
     * 空 stack 表示「取下唱片」：直接删键（读端本来就按缺失返回 EMPTY）。 {@link ItemStack#CODEC} 拒绝空 stack
     * （{@code Item must not be minecraft:air}），若走 store 会在服务端交互线程抛 IllegalStateException 被吞，导致唱片取不下来。
     */
    static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (stack.isEmpty()) {
            tag.remove(NbtKeys.JUKEBOX_FOLLOWER_DISC);
        } else {
            tag.store(NbtKeys.JUKEBOX_FOLLOWER_DISC, ItemStack.CODEC, stack);
        }
        PetDataSync.sync(enchanted, tag);
    }
}

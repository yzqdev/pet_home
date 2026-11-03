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
 * 跟随唱片机（disc_jockey）数据与魅力（charisma）交易折扣（原 {@code TameableUtils} 八区）。
 */
final class PetJukeboxUtils {

    private PetJukeboxUtils() {}
    /** 唱片机跟随者的跟随目标 UUID（disc_jockey 召唤的宠物跟随唱片实体），citadel tag 存储；未设置返回 null。 */
    static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putUUID(PetDataKeys.JUKEBOX_FOLLOWER_UUID, id);
        PetDataSync.sync(enchanted, tag);
    }

    static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(PetDataKeys.JUKEBOX_FOLLOWER_UUID) ? tag.getUUID(PetDataKeys.JUKEBOX_FOLLOWER_UUID) : null;
    }

    /** 唱片机跟随者正在播放的唱片 ItemStack（完整序列化到 citadel tag）；未设置返回 {@link ItemStack#EMPTY}。 */
    static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.put(PetDataKeys.JUKEBOX_FOLLOWER_DISC, stack.save(new CompoundTag()));
        PetDataSync.sync(enchanted, tag);
    }

    static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(PetDataKeys.JUKEBOX_FOLLOWER_DISC) ? ItemStack.of(tag.getCompound(PetDataKeys.JUKEBOX_FOLLOWER_DISC)) : ItemStack.EMPTY;
    }

    /** charisma（魅力）附魔对主人的交易折扣加成：统计 25 格内自家宠物的 10*等级之和，上限 50（对应原版村民折扣公式）。 */
    static int getCharismaBonusForOwner(Player player) {
        Predicate<Entity> pet = (animal) -> PetOwnershipUtils.isTamed(animal) && PetOwnershipUtils.isPetOf(player, animal);
        List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25, 8, 25), EntitySelector.NO_SPECTATORS.and(pet));
        int charismas = 0;
        for (LivingEntity entity : list) {
            charismas += 10 * PetEnchantmentUtils.getEnchantLevel(entity, PHEnchantmentRegistry.CHARISMA);
        }
        return Math.min(charismas, 50);
    }
}

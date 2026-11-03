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
 * citadel tag 键、属性修饰符 UUID 与三态指令编号的<b>唯一归属</b>（package-private）。
 * 
 * <p>所有职责类共用同一份键定义，避免同一常量在多处各存副本导致改名时遗漏；
 * {@code TameableUtils} 的 public 常量保留指向此处同名字段的别名（字符串值不变）。
 */
final class PetDataKeys {

    private PetDataKeys() {}
    static final String ENCHANTMENT_TAG = "StoredPetEnchantments";

    static final String COLLAR_TAG = "HasPetCollar";

    static final String IMMUNITY_TIME_TAG = "PetImmunityTimer";

    // 以下两个计时器键供 LivingEntityMixin 存档桥接使用（运行时已迁移到独立 int 数据槽）
    static final String FROZEN_TIME_TAG = "PetFrozenTime";

    static final String ATTACK_TARGET_ENTITY = "PetAttackTarget";

    static final String SHADOW_PUNCH_TIMES = "PetShadowPunchTimes";

    static final String SHADOW_PUNCH_COOLDOWN = "PetShadowPunchCooldown";

    static final String PSYCHIC_WALL_COOLDOWN = "PetPsychicWallCooldown";

    static final String INTIMIDATION_COOLDOWN = "PetIntimidationCooldown";

    static final String SHADOW_PUNCH_STRIKING = "PetShadowPunchStriking";

    static final String JUKEBOX_FOLLOWER_UUID = "PetJukeboxFollowerUUID";

    static final String JUKEBOX_FOLLOWER_DISC = "PetJukeboxFollowerDisc";

    static final String BLAZING_PROTECTION_BARS = "PetBlazingProtectionBars";

    static final String BLAZING_PROTECTION_COOLDOWN = "PetBlazingProtectionCooldown";

    static final String HEALING_AURA_TIME_TAG = "PetHealingAuraTime";

    static final String HEALING_AURA_IMPULSE = "PetHealingAuraImpulse";

    static final String HAS_PET_BED = "HasPetBed";

    static final String PET_BED_X = "PetBedX";

    static final String PET_BED_Y = "PetBedY";

    static final String PET_BED_Z = "PetBedZ";

    static final String PET_BED_DIMENSION = "PetBedDimension";

    static final String FALL_DISTANCE_SYNC = "SyncedFallDistance";

    static final String ZOMBIE_PET = "ZombiePet";

    static final String SAFE_PET_HEALTH = "SafePetHealth";

    static final String COLLAR_SWAP_COOLDOWN = "CollarSwapCooldown";

    static final UUID HEALTH_BOOST_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B166EEEEE");

    static final UUID SPEED_BOOST_UUID = UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");

    static final UUID SPEED_BOOST_AQUATIC_LAND_UUID = UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31745");

    // TOUGH 附魔属性修改器（自 1.21 移植）
    static final UUID TOUGH_ARMOR_UUID = UUID.fromString("6f2a5c1d-93b7-4a2e-8d40-1c5f7a9b2e31");

    static final UUID TOUGH_KNOCKBACK_UUID = UUID.fromString("8c4d9e2f-1a6b-4f70-b3e5-9d2c8a7f4b62");
}

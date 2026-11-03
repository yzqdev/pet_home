package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.Identifier;

/**
 * 宠物数据的<b>键常量唯一归属</b>（package-private）：citadel tag 键、属性修饰符 Identifier、指令编号。
 *
 * <p>从 {@link TameableUtils} 拆出是为了让各职责类共享同一份键定义——原实现里这些键是 {@code TameableUtils} 的 private
 * 字段，拆分后若各自复制一份就会出现「同一个存档键两个来源」。 {@link TameableUtils#ENCHANTMENT_TAG} / {@link
 * TameableUtils#FROZEN_TIME_TAG} / {@link TameableUtils#HEALING_AURA_TIME} 三个 public
 * 常量在此处定义真值，门面只做别名转发（外部引用不变）。
 *
 * <p>键的字符串值即存档格式，改动等于换存档格式——禁止重命名值。
 */
final class PetDataKeys {

    private PetDataKeys() {
    }

    // ===== citadel tag 键（持久化，写入必须经 PetDataSync#sync） =====

    /** 附魔列表（{@code id} 字符串 + {@code lvl} short 的 ListTag）。 */
    static final String ENCHANTMENT_TAG = "StoredPetEnchantments";

    /** 是否戴项圈；由附魔写入路径（{@link PetEnchantmentUtils}）置位。 */
    static final String COLLAR_TAG = "HasPetCollar";

    /** 换项圈防抖（写入附魔时置 20 tick）。 */
    static final String COLLAR_SWAP_COOLDOWN = "CollarSwapCooldown";

    /** immunity_frame 剩余无敌 tick。 */
    static final String IMMUNITY_TIME_TAG = "PetImmunityTimer";

    /** frost_fang 冻结 tick（旧值键；现行存储为独立附件 {@link PHAttachments#FROZEN_TIME}）。 */
    static final String FROZEN_TIME_TAG = "PetFrozenTime";

    /** 暗影手/引力共用目标槽（实体 id，-1 表示无）。 */
    static final String ATTACK_TARGET_ENTITY = "PetAttackTarget";

    /** 每只暗影手的出拳进度（0..10，数组按附魔等级）。 */
    static final String SHADOW_PUNCH_TIMES = "PetShadowPunchTimes";

    /** 暗影手出拳间隔倒计时。 */
    static final String SHADOW_PUNCH_COOLDOWN = "PetShadowPunchCooldown";

    /** 各只暗影手是否处于出拳中（0=收回，1=打出）。 */
    static final String SHADOW_PUNCH_STRIKING = "PetShadowPunchStriking";

    /** 念力墙冷却倒计时。 */
    static final String PSYCHIC_WALL_COOLDOWN = "PetPsychicWallCooldown";

    /** intimidation（威吓）剩余威吓 tick。 */
    static final String INTIMIDATION_COOLDOWN = "PetIntimidationCooldown";

    /** 烈焰保护剩余护盾格数（0..2×等级）。 */
    static final String BLAZING_PROTECTION_BARS = "PetBlazingProtectionBars";

    /** 烈焰保护下一格护盾的充能倒计时。 */
    static final String BLAZING_PROTECTION_COOLDOWN = "PetBlazingProtectionCooldown";

    /** 治愈光环相位（旧值键；现行存储为独立附件 {@link PHAttachments#HEALING_AURA_TIME}）。 */
    static final String HEALING_AURA_TIME = "PetHealingAuraTime";

    /** 治愈光环受击冲能标记。 */
    static final String HEALING_AURA_IMPULSE = "PetHealingAuraImpulse";

    /** 音波轰击下次可施放时间戳（level game time）。 */
    static final String SONIC_BOOM_TIME = "PetSonicBoomTime";

    /** 是否绑定宠物床。 */
    static final String HAS_PET_BED = "HasPetBed";

    static final String PET_BED_X = "PetBedX";
    static final String PET_BED_Y = "PetBedY";
    static final String PET_BED_Z = "PetBedZ";

    /** 宠物床所在维度字符串。 */
    static final String PET_BED_DIMENSION = "PetBedDimension";

    /** void_cloud 缓降期间同步给客户端的下落距离。 */
    static final String FALL_DISTANCE_SYNC = "SyncedFallDistance";

    /** 存档时记录的「安全血量」（含 health_boost 提升后的血上限）。 */
    static final String SAFE_PET_HEALTH = "SafePetHealth";

    // ===== 属性修饰符 Identifier（数值/叠加方式与移除逻辑见 PetEnchantmentUtils） =====

    static final Identifier HEALTH_BOOST_UUID = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "health_boost");// UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B166EEEEE");
    static final Identifier SPEED_BOOST_UUID = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "speed_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");
    static final Identifier ARMOR_BOOST_UUID = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "armor_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");
    static final Identifier RESISTANCE_BOOST_UUID = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "resistance_boost");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");

    static final Identifier SPEED_BOOST_AQUATIC_LAND_UUID = Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "speed_boost_aqua");// UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31745");

    // ===== 指令编号 =====

    /** 本模组三态指令编号：0 = 游走，1 = 停留，2 = 跟随 */
    static final int COMMAND_FOLLOW = 2;

    /** 第三方宠物（Alex's Mobs 等）的 Command 编号：1 = 跟随 */
    static final int FOREIGN_COMMAND_FOLLOW = 1;

    /** 未设置/非宠物时的指令哨兵值（{@code readForeignCommand} 用）。 */
    static final int NO_FOREIGN_COMMAND = -1;
}

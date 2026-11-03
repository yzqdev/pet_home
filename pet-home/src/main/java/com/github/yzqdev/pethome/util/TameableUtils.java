package com.github.yzqdev.pethome.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * 全模组核心工具类（<b>门面</b>）：驯服判定与归属、附魔读写、宠物计时器、宠物床坐标、行为辅助（音波/嗅矿/威吓等）。
 * 几乎所有 server 代码与 mixin 依赖它。
 *
 * <p><b>实现已按职责拆分，本类只做委托</b>：
 *
 * <ul>
 *   <li>{@link PetOwnershipUtils}：归属、驯服状态、物种开关、项圈、安全血量、灯笼召回判定
 *   <li>{@link PetEnchantmentUtils}：附魔缓存读取、列表写入、属性修饰符同步
 *   <li>{@link PetStateUtils}：状态与计时器、宠物床坐标
 *   <li>{@link PetEntityQueryUtils}：附近实体查询与传送筛选
 *   <li>{@link PetEnchantmentBehaviorUtils}：附魔行为执行
 *   <li>{@link PetJukeboxUtils}：唱片机数据与魅力折扣
 *   <li>{@link PetDataKeys}：tag 键与属性修饰符常量；{@link PetDataSync}：citadel tag 写入唯一出口
 * </ul>
 *
 * <p>本类不承载游戏逻辑——所有 public 方法一行转发，签名/参数/返回类型/可见性/静态调用方式与拆分前一致，
 * 引用文件与 mixin 无需任何改动。Citadel tag 的写入仍然只经 {@link PetDataSync#sync}（包内可见，
 * 不对外暴露第二条写入路径）。
 *
 * <p>方法分区沿用原顺序：一、归属与驯服判定 → 二、灯笼卸载判定 → 三、宠物床坐标 → 四、附魔读写与属性 →
 * 五、计时器 → 六、行为辅助 → 七、传送与近距离查询 → 八、disc_jockey / charisma。
 */
public class TameableUtils {

    /** 附魔列表 tag 键（真值在 {@link PetDataKeys#ENCHANTMENT_TAG}，此处保留 public 别名供外部引用）。 */
    public static final String ENCHANTMENT_TAG = PetDataKeys.ENCHANTMENT_TAG;

    /** 冻结计时 tag 键（真值在 {@link PetDataKeys#FROZEN_TIME_TAG}；现行存储为独立附件 {@link PHAttachments#FROZEN_TIME}）。 */
    public static final String FROZEN_TIME_TAG = PetDataKeys.FROZEN_TIME_TAG;

    /** 治愈光环相位 tag 键（真值在 {@link PetDataKeys#HEALING_AURA_TIME}；现行存储为独立附件 {@link PHAttachments#HEALING_AURA_TIME}）。 */
    public static final String HEALING_AURA_TIME = PetDataKeys.HEALING_AURA_TIME;

    // ==================== 一、归属与驯服判定 → PetOwnershipUtils ====================

    /**
     * 取实体的驯服主人 UUID，兼容两类归属载体：本模组改造实体（
     * {@link com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable}）与原版 {@link net.minecraft.world.entity.TamableAnimal}。
     *
     * @return 无主人或非可驯服实体时为 {@code null}
     * @see PetOwnershipUtils#getOwnerUUIDOf
     */
    public static UUID getOwnerUUIDOf(Entity entity) {
        return PetOwnershipUtils.getOwnerUUIDOf(entity);
    }

    /** 宠物是否戴有项圈（citadel tag {@code HasPetCollar} 键，由附魔写入路径置位）。 */
    public static boolean hasCollar(LivingEntity enchanted) {
        return PetOwnershipUtils.hasCollar(enchanted);
    }

    /** 是否属于「走失、应当被迷途灯笼召回」的宠物——只认跟随状态，停留/游走不召回。 */
    public static boolean shouldUnloadToLantern(LivingEntity tameable) {
        return PetOwnershipUtils.shouldUnloadToLantern(tameable);
    }

    /** 存档时记录的「安全血量」（含 health_boost 提升后的血上限），实体重载时用于恢复血量。 */
    public static double getSafePetHealth(LivingEntity enchanted) {
        return PetOwnershipUtils.getSafePetHealth(enchanted);
    }

    /** 重算物种开关表（{@link com.github.yzqdev.pethome.PetHomeConfig} 配置加载后调用）。 */
    public static void refreshSpeciesGates() {
        PetOwnershipUtils.refreshSpeciesGates();
    }

    /** 实体卸载进世界数据前记录当前血量（仅 health_boost 附魔触发），与 {@link #getSafePetHealth} 配对。 */
    public static void setSafePetHealth(LivingEntity enchanted, double health) {
        PetOwnershipUtils.setSafePetHealth(enchanted, health);
    }

    /** 实体是否处于「已驯服」状态；改造物种额外受 {@code PetHomeConfig.tameable*} 开关约束。 */
    public static boolean isTamed(Entity entity) {
        return PetOwnershipUtils.isTamed(entity);
    }

    /** 玩家视角的「自家宠物」判定：结盟或主人即该玩家。网络包鉴权、交互分发、友好保护入口。 */
    public static boolean isPetOf(Player player, Entity entity) {
        return PetOwnershipUtils.isPetOf(player, entity);
    }

    /** 双向同主人判定（两方向任一成立即可），光环治疗、引力、敌对排除等团队逻辑用。 */
    public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return PetOwnershipUtils.hasSameOwnerAs(tameable, target);
    }

    /** 实体是否属于「可被驯服改造/原版可驯服」的范畴（只看类型，不看是否已驯服），事件层用粗筛。 */
    public static boolean couldBeTamed(Entity entity) {
        return PetOwnershipUtils.couldBeTamed(entity);
    }

    /** 取实体主人的实体实例（在线/已加载才非 null），与 {@link #getOwnerUUIDOf} 对应。 */
    public static Entity getOwnerOf(Entity entity) {
        return PetOwnershipUtils.getOwnerOf(entity);
    }

    // ==================== 三、宠物床坐标 → PetStateUtils ====================

    /** 宠物绑定的宠物床坐标（{@code HasPetBed=false} 或坐标缺失时返回 {@code null}）。 */
    @Nullable
    public static BlockPos getPetBedPos(LivingEntity enchanted) {
        return PetStateUtils.getPetBedPos(enchanted);
    }

    /** 绑定宠物床（站上宠物床时调用）；只写坐标，维度由 {@link #setPetBedDimension} 单独写入。 */
    public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        PetStateUtils.setPetBedPos(enchanted, petBed);
    }

    /** 解绑宠物床（床被拆/放生时调用）；只置 {@code HAS_PET_BED=false}。 */
    public static void removePetBedPos(LivingEntity enchanted) {
        PetStateUtils.removePetBedPos(enchanted);
    }

    /** 宠物床所在维度的字符串形式；未写入时按主世界处理。 */
    public static String getPetBedDimension(LivingEntity enchanted) {
        return PetStateUtils.getPetBedDimension(enchanted);
    }

    public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        PetStateUtils.setPetBedDimension(enchanted, dimension);
    }

    // ==================== 四、附魔读写与属性 → PetEnchantmentUtils ====================

    /** 清空宠物全部附魔（顺带把项圈置为已戴上并触发属性重算）。 */
    public static void clearEnchants(LivingEntity entity) {
        PetEnchantmentUtils.clearEnchants(entity);
    }

    /** {@link #getEnchantLevel} 的布尔形态。 */
    public static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return PetEnchantmentUtils.hasEnchant(entity, enchantment);
    }

    /** 查宠物单个附魔等级（mixin/渲染/tick 热路径，走 {@link PetEnchantCache} 引用比对缓存）。 */
    public static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return PetEnchantmentUtils.getEnchantLevel(entity, enchantment);
    }

    /** 组装宠物附魔列表的展示行（Jade tooltip 与宠物信息面板共用）。 */
    public static List<Component> getEnchantDescriptions(LivingEntity entity) {
        return PetEnchantmentUtils.getEnchantDescriptions(entity);
    }

    /** 取宠物全部附魔（{@code id → level}）；实体未挂 citadel 附件或无附魔键时返回 {@code null}（调用方需判空）。返回的 Map 不可修改。 */
    @Nullable
    public static Map<Identifier, Integer> getEnchants(LivingEntity entity) {
        return PetEnchantmentUtils.getEnchants(entity);
    }

    /** 构造附魔列表的单个条目 tag：{@code id} 存字符串、{@code lvl} 存 short（三侧同格式）。 */
    public static CompoundTag storeEnchantment(@Nullable Identifier pId, int pLevel) {
        return PetEnchantmentUtils.storeEnchantment(pId, pLevel);
    }

    /** 把 {@link ItemEnchantments}（附魔书内容）整表写入宠物附魔列表——覆盖旧列表而非追加。 */
    public static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
        PetEnchantmentUtils.addEnchant(entity, itemEnchantments);
    }

    // ==================== 五、计时器与状态槽 → PetStateUtils ====================

    /** 烈焰保护的剩余护盾格数（0..2×等级）。 */
    public static int getBlazingProtectionBars(LivingEntity enchanted) {
        return PetStateUtils.getBlazingProtectionBars(enchanted);
    }

    /** 烈焰保护下一格护盾的充能倒计时（tick）。 */
    public static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        return PetStateUtils.getBlazingProtectionCooldown(enchanted);
    }

    public static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setBlazingProtectionCooldown(enchanted, time);
    }

    public static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        PetStateUtils.setBlazingProtectionBars(enchanted, time);
    }

    /** 治愈光环相位计时器：&gt;0 施法中、0 待机、&lt;0 冷却（独立 int 附件）。 */
    public static int getHealingAuraTime(LivingEntity enchanted) {
        return PetStateUtils.getHealingAuraTime(enchanted);
    }

    /** 写治愈光环计时器；值不变时直接返回，避免每 tick 空广播。 */
    public static void setHealingAuraTime(LivingEntity enchanted, int time) {
        PetStateUtils.setHealingAuraTime(enchanted, time);
    }

    /** 音波轰击的下次可施放时间戳（level game time，非倒计时）。 */
    public static long getSonicboomAuraTime(LivingEntity enchanted) {
        return PetStateUtils.getSonicboomAuraTime(enchanted);
    }

    public static void setSonicboomAuraTime(LivingEntity enchanted, long time) {
        PetStateUtils.setSonicboomAuraTime(enchanted, time);
    }

    /** 治愈光环的标记。 */
    public static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        return PetStateUtils.getHealingAuraImpulse(enchanted);
    }

    public static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        PetStateUtils.setHealingAuraImpulse(enchanted, impulse);
    }

    /** 心理墙冷却倒计时（tick）。 */
    public static int getPsychicWallCooldown(LivingEntity enchanted) {
        return PetStateUtils.getPsychicWallCooldown(enchanted);
    }

    public static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setPsychicWallCooldown(enchanted, time);
    }

    /** 暗影手/引力共用目标槽：存目标实体 id（-1 无）。 */
    public static int getPetAttackTargetID(LivingEntity enchanted) {
        return PetStateUtils.getPetAttackTargetID(enchanted);
    }

    /** 把 id 解析为目标实体；id 无效或实体已卸载返回 {@code null}。 */
    @Nullable
    public static Entity getPetAttackTarget(LivingEntity enchanted) {
        return PetStateUtils.getPetAttackTarget(enchanted);
    }

    public static void setPetAttackTarget(LivingEntity enchanted, int id) {
        PetStateUtils.setPetAttackTarget(enchanted, id);
    }

    /** intimidation（恐吓）剩余恐吓 tick 数；&gt;0 期间只驱赶不重触发。 */
    public static int getIntimidationCooldown(LivingEntity enchanted) {
        return PetStateUtils.getIntimidationCooldown(enchanted);
    }

    public static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setIntimidationCooldown(enchanted, time);
    }

    /** immunity_frame 剩余无敌 tick；未戴该附魔恒 0。 */
    public static int getImmuneTime(LivingEntity enchanted) {
        return PetStateUtils.getImmuneTime(enchanted);
    }

    public static void setImmuneTime(LivingEntity enchanted, int time) {
        PetStateUtils.setImmuneTime(enchanted, time);
    }

    /** frost_fang 冻结剩余时间（独立 int 附件，渲染冰冻覆盖层用）。 */
    public static int getFrozenTime(LivingEntity enchanted) {
        return PetStateUtils.getFrozenTime(enchanted);
    }

    /** 写冻结计时（frost_fang 命中时置 60）；值相等直接返回，避免空广播。 */
    public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        PetStateUtils.setFrozenTimeTag(enchanted, time);
    }

    /** void_cloud 缓降期间同步给客户端的下落距离。 */
    public static float getFallDistance(LivingEntity enchanted) {
        return PetStateUtils.getFallDistance(enchanted);
    }

    public static void setFallDistance(LivingEntity enchanted, float dist) {
        PetStateUtils.setFallDistance(enchanted, dist);
    }

    /** 暗影手出拳间隔倒计时（tick）。 */
    public static int getShadowPunchCooldown(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchCooldown(enchanted);
    }

    public static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setShadowPunchCooldown(enchanted, time);
    }

    /** 每只暗影手的出拳进度（0..10，数组按附魔等级对应各只手）。 */
    public static int[] getShadowPunchTimes(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchTimes(enchanted);
    }

    public static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        PetStateUtils.setShadowPunchTimes(enchanted, times);
    }

    /** 各只暗影手是否处于出拳中（0=收回，1=打出）。 */
    public static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        PetStateUtils.setShadowPunchStriking(enchanted, times);
    }

    public static int[] getShadowPunchStriking(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchStriking(enchanted);
    }

    // ==================== 六、行为辅助（附魔效果执行） → PetEnchantmentBehaviorUtils ====================

    /** xp_transfer 附魔：吸附 3 格内经验球给主人（兼容保留，实际走玩家驱动版本）。 */
    public static void xpTransfer(LivingEntity living) {
        PetEnchantmentBehaviorUtils.xpTransfer(living);
    }

    /** rejuvenation 附魔：宠物受伤后用 3 格内经验球回血，溢出经验重建经验球退回。 */
    public static void absorbExpOrbs(LivingEntity living) {
        PetEnchantmentBehaviorUtils.absorbExpOrbs(living);
    }

    /** shepherd 附魔：每 8 tick 把 16 格内最近的动物清仇恨并导航走向宠物。 */
    public static void attractAnimals(LivingEntity attractor, int max) {
        PetEnchantmentBehaviorUtils.attractAnimals(attractor, max);
    }

    /** infamy_curse 附魔：每 400 tick 让 20 格内最近的怪物把宠物设为攻击目标。 */
    public static void aggroRandomMonsters(LivingEntity attractor) {
        PetEnchantmentBehaviorUtils.aggroRandomMonsters(attractor);
    }

    /** intimidation 附魔：按等级间隔（或受击时）威吓怪物令其逃离，持续 {@code 70×level} tick。 */
    public static void scareRandomMonsters(LivingEntity scary, int level) {
        PetEnchantmentBehaviorUtils.scareRandomMonsters(scary, level);
    }

    /** blight_curse 附魔：每 200 tick 毁掉周围花草/作物、把草方块退化成泥土/砂土。 */
    public static void destroyRandomPlants(LivingEntity living) {
        PetEnchantmentBehaviorUtils.destroyRandomPlants(living);
    }

    /** ore_scenting 附魔主循环：嗅探动画 + 扫描矿石生成高亮方块实体。 */
    public static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
        PetEnchantmentBehaviorUtils.detectRandomOres(attractor, interval, range, effectLength, maxOres);
    }

    /** sonic_boom 附魔的施放入口：5 格内敌对生物 &gt; 3 只时全体施放，否则只打当前目标。 */
    public static void performSonicBook(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        PetEnchantmentBehaviorUtils.performSonicBook(maid, monster, serverLevel);
    }

    /** 单体音波攻击：粒子串 + 音效 + 10 点 sonic_boom 伤害（按击退抗性衰减击退）。仅服务端调用。 */
    public static void sonicBoomAttack(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        PetEnchantmentBehaviorUtils.sonicBoomAttack(maid, monster, serverLevel);
    }

    /** insight 附魔：给 {@code level×15} 格范围内的敌对生物上 1 秒发光。 */
    public static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
        PetEnchantmentBehaviorUtils.applyGlowingEffect(livingEntity, enchantLevel);
    }

    // ==================== 七、传送与近距离查询 → PetEntityQueryUtils ====================

    /** tethered_teleport 附魔是否允许这只宠物跟着 owner 一起传送。 */
    public static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        return PetEntityQueryUtils.isValidTeleporter(owner, animal);
    }

    /** {@code range} 格内的非玩家活体（不含自身），吸血平摊/音波群攻等范围效果的基础查询。 */
    public static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        return PetEntityQueryUtils.getNearbyMobs(entity, range);
    }

    /** 光环可治疗的目标：4 格内同主人（双向）且未满血的活体，含主人本人。 */
    public static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        return PetEntityQueryUtils.getAuraHealables(pet);
    }

    /** 16 格内「有 healing_aura 且当前空闲」的同主人宠物（受击冲能用）。 */
    public static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        return PetEntityQueryUtils.getNearbyHealers(hurtOwner);
    }

    // ==================== 八、disc_jockey / charisma → PetJukeboxUtils ====================

    /** charisma 对主人的交易折扣加成：25 格内自家宠物的 10×等级之和，上限 50。 */
    public static int getCharismaBonusForOwner(Player player) {
        return PetJukeboxUtils.getCharismaBonusForOwner(player);
    }

    /** disc_jockey 唱片机跟随者的 UUID（未设置返回 null），citadel tag 存储。 */
    @Nullable
    public static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        return PetJukeboxUtils.getPetJukeboxUUID(enchanted);
    }

    public static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        PetJukeboxUtils.setPetJukeboxUUID(enchanted, id);
    }

    /** disc_jockey 唱片机当前播放的唱片 ItemStack；未设置返回 EMPTY。 */
    public static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        return PetJukeboxUtils.getPetJukeboxDisc(enchanted);
    }

    public static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        PetJukeboxUtils.setPetJukeboxDisc(enchanted, stack);
    }
}

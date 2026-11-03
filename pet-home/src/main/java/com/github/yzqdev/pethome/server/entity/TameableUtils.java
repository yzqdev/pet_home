package com.github.yzqdev.pethome.server.entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;


/**
 * 全模组核心工具类（<b>门面</b>）：驯服判定与归属、宠物附魔读写与属性修改、各类附魔计时器、
 * 宠物床坐标、定位/传送/索敌等行为辅助。几乎所有 server 代码与 mixin 依赖它。
 * 本侧（1.20）宠物数据在 {@code LivingEntity} 的 SynchedEntityData CompoundTag 槽内。
 *
 * <p><b>实现已按职责拆分，本类只做委托</b>（2026-10-23 重构，与 26.3 侧同构）：
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
 * 引用文件与 mixin 无需任何改动。citadel tag 的写入仍然只经 {@link PetDataSync#sync}（包内可见，
 * 不对外暴露第二条写入路径）。
 *
 * <p>方法分区沿用原顺序：一、归属与驯服判定 → 二、灯笼卸载判定 → 三、宠物床坐标 → 四、附魔读写与属性 →
 * 五、计时器 → 六、行为辅助 → 七、传送与近距离查询。
 *
 * @author yzqde
 * @date time 2025/1/9 7:52
 * @modified By: 2026-10-23 职责拆分为包内工具类，本类保留为兼容门面
 */
public class TameableUtils {

    /** 冻结计时 tag 键（真值在 {@link PetDataKeys#FROZEN_TIME_TAG}）。 */
    public static final String FROZEN_TIME_TAG = PetDataKeys.FROZEN_TIME_TAG;

    /** 治愈光环相位 tag 键（真值在 {@link PetDataKeys#HEALING_AURA_TIME_TAG}）。 */
    public static final String HEALING_AURA_TIME_TAG = PetDataKeys.HEALING_AURA_TIME_TAG;

    /**
     * 判定两个实体是否归属同一主人（双向检查：任意一方是另一方的宠物即视为同主）。
     * 兼容 {@link ModifiedToBeTameable}（本模组改造的可驯服生物）与原版 {@link TamableAnimal}；
     * target 本身是玩家（主人）时也返回 true。友好保护/宠物友军判定的高频入口。
     */
    public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
        return PetOwnershipUtils.hasSameOwnerAs(tameable, target);
    }

    /**
     * 实体随区块卸载时是否应记入迷途灯笼召回请求（由 ServerEvents#onEntityLeaveWorld 调用）。
     * 只认「跟随状态」：三态指令开启时 command==2（跟随）才召回；三态关闭时本模组宠物按「未停留=跟随」兜底；
     * 第三方宠物（如 Alex's Mobs，未实现 IComandableMob）读其 NBT 里 {@code *Command} 键（编号 1=跟随），
     * 都没有再按「原版可驯服动物未坐下=跟随」兜底。
     * 第三方扫描会序列化一遍实体附加数据，调用方注意别在热路径高频使用（本模组宠物走内存判定，无序列化）。
     */
    public static boolean shouldUnloadToLantern(LivingEntity tameable) {
        return PetOwnershipUtils.shouldUnloadToLantern(tameable);
    }

    /**
     * 判定实体是否为该玩家的宠物： alliedTo（团队/友军）或单向同主（entity 的主人是该玩家）。
     * 交互处理、保护判定的高频入口。
     */
    public static boolean isPetOf(Player player, Entity entity) {
        return PetOwnershipUtils.isPetOf(player, entity);
    }

    /** 重算物种开关表（配置加载后调用）。 */
    public static void refreshSpeciesGates() {
        PetOwnershipUtils.refreshSpeciesGates();
    }

    /**
     * 判定实体是否已被驯服。对美西螈/狐狸/兔子/青蛙这四类改造生物，除 isTame 外还要求
     * 对应的「可驯服」配置开关处于开启状态（防止数据残留的旧个体被当作宠物）；
     * 其余实体按 {@link ModifiedToBeTameable} 或 {@link TamableAnimal} 的 isTame 判定。
     */
    public static boolean isTamed(Entity entity) {
        return PetOwnershipUtils.isTamed(entity);
    }

    /** 实体是否属于「可被驯服」的类型（原版 {@link TamableAnimal} 或本模组 {@link ModifiedToBeTameable} 改造生物），不关心当前是否已被驯服。 */
    public static boolean couldBeTamed(Entity entity) {
        return PetOwnershipUtils.couldBeTamed(entity);
    }

    /** 取实体主人（在线玩家），兼容 {@link ModifiedToBeTameable} 与 {@link TamableAnimal} 两套归属体系；无主人或主人离线时返回 null。 */
    public static Entity getOwnerOf(Entity entity) {
        return PetOwnershipUtils.getOwnerOf(entity);
    }

    /** 取主人 UUID（不要求主人在线），两套归属体系兼容；实体无归属概念时返回 null。网络鉴权（校验包伪造）与档案系统都以此为据。 */
    public static UUID getOwnerUUIDOf(Entity entity) {
        return PetOwnershipUtils.getOwnerUUIDOf(entity);
    }

    /** 写主人 UUID。注意：实体若同时命中两类接口会连写两次（正常实体只命中其一）。 */
    public static void setOwnerUUIDOf(Entity entity, UUID uuid) {
        PetOwnershipUtils.setOwnerUUIDOf(entity, uuid);
    }

    /**
     * 查宠物某附魔的等级，未拥有/被配置禁用时返回 0。实体实现了 {@link PetSyncDataEntity} 时走
     * 实体级缓存（tag 变化才重新解析 NBT 列表），供 mixin/渲染/tick 热路径频繁调用。
     * 1.20 侧的键是 {@link Enchantment} 注册表实例本身（26.1 为 ResourceLocation/ResourceKey）。
     */
    public static int getEnchantLevel(LivingEntity entity, Enchantment enchantment) {
        return PetEnchantmentUtils.getEnchantLevel(entity, enchantment);
    }

    /** 把 citadel tag 里的附魔列表解析为 Enchantment → level 映射（供实体缓存重建用） */
    public static Map<Enchantment, Integer> buildEnchantCache(CompoundTag tag) {
        return PetEnchantmentUtils.buildEnchantCache(tag);
    }

    /** 是否拥有该附魔（等级 > 0），底层走 {@link #getEnchantLevel} 的缓存路径。 */
    public static boolean hasEnchant(LivingEntity entity, Enchantment enchantment) {
        return PetEnchantmentUtils.hasEnchant(entity, enchantment);
    }

    /**
     * 解析实体全部宠物附魔为 ResourceLocation → 等级映射（已按配置过滤被禁用的附魔）。
     * 实体从未有过附魔（citadel tag 无 {@code StoredPetEnchantments} 键）时返回 null，调用方需判空。
     */
    @Nullable
    public static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        return PetEnchantmentUtils.getEnchants(entity);
    }

    /** 生成附魔列表的展示文本（金色标题行 + 每条「附魔名 等级」，诅咒附魔显示红色），用于 tooltip/Jade 等界面。 */
    public static List<Component> getEnchantDescriptions(LivingEntity entity) {
        return PetEnchantmentUtils.getEnchantDescriptions(entity);
    }

    /** 实体缓存路径下判定是否拥有任意宠物附魔；无缓存的实体直接看 NBT 列表是否非空。 */
    public static boolean hasAnyEnchants(LivingEntity entity) {
        return PetEnchantmentUtils.hasAnyEnchants(entity);
    }

    /** 给宠物加一条附魔：已有同名附魔时只允许升级（现等级更高则维持不变），写入后触发防抖与属性重算。 */
    public static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment) {
        PetEnchantmentUtils.addEnchant(entity, enchantment);
    }

    /** addEnchant 的列表版本：在调用方提供的 {@link ListTag} 上原地修改后统一经 {@link #setEnchantmentTag} 写回（附魔被配置禁用时静默忽略）。 */
    public static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment, ListTag listtag) {
        PetEnchantmentUtils.addEnchant(entity, enchantment, listtag);
    }

    /** 清空全部宠物附魔（写入空列表，走统一收口：触发防抖 + 属性重算，卸下所有属性修饰符）。契约解除驯服/换项圈时使用。 */
    public static void clearEnchants(LivingEntity entity) {
        PetEnchantmentUtils.clearEnchants(entity);
    }

    /** 标记/清除「已佩戴项圈」状态（citadel tag 布尔键，随 sync 同步客户端）。戴上/摘下项圈时由交互处理调用。 */
    public static void setHasCollar(LivingEntity enchanted, boolean collar) {
        PetOwnershipUtils.setHasCollar(enchanted, collar);
    }

    /** 实体是否已佩戴本模组项圈（决定附魔是否生效、tooltip 是否显示附魔列表等）。 */
    public static boolean hasCollar(LivingEntity enchanted) {
        return PetOwnershipUtils.hasCollar(enchanted);
    }

    /** immune_frame 附魔的免疫帧计时（tick）；实体没有该附魔时恒为 0，不写 tag。 */
    public static int getImmuneTime(LivingEntity enchanted) {
        return PetStateUtils.getImmuneTime(enchanted);
    }

    public static void setImmuneTime(LivingEntity enchanted, int time) {
        PetStateUtils.setImmuneTime(enchanted, time);
    }

    /** 冷冻（frozen）剩余 tick 数。运行时存于 {@link PetSyncDataEntity} 独立 int 数据槽（每 tick 高频读写，避免整 tag 同步）；存档经 LivingEntityMixin 桥接回 {@code PetFrozenTime} NBT 键保证旧档兼容。 */
    public static int getFrozenTime(LivingEntity enchanted) {
        return PetStateUtils.getFrozenTime(enchanted);
    }

    /** 写冷冻计时（同上走独立数据槽，setter 需在 mixin 侧自行判等避免 0→0 空同步）。 */
    public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        PetStateUtils.setFrozenTimeTag(enchanted, time);
    }

    /** 暗影手当前攻击目标实体 ID（citadel tag 存储，无目标为 -1）。 */
    public static int getPetAttackTargetID(LivingEntity enchanted) {
        return PetStateUtils.getPetAttackTargetID(enchanted);
    }

    /** 按存储的实体 ID 解析暗影手攻击目标；目标已不在世界中时返回 null。 */
    @Nullable
    public static Entity getPetAttackTarget(LivingEntity enchanted) {
        return PetStateUtils.getPetAttackTarget(enchanted);
    }

    /** 设置暗影手攻击目标 ID（随 sync 同步客户端供渲染层使用）。 */
    public static void setPetAttackTarget(LivingEntity enchanted, int id) {
        PetStateUtils.setPetAttackTarget(enchanted, id);
    }

    /**
     * 暗影手（shadow_hands 附魔）状态机数据：cooldown 为出拳冷却 tick；
     * times 是各拳的已出次数数组；striking 标记进行中的拳索引数组。均存 citadel tag 并随 sync 同步
     * （渲染层读取 striking 做挥拳动画）。
     */
    public static int getShadowPunchCooldown(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchCooldown(enchanted);
    }

    public static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setShadowPunchCooldown(enchanted, time);
    }

    public static int[] getShadowPunchTimes(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchTimes(enchanted);
    }

    public static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        PetStateUtils.setShadowPunchTimes(enchanted, times);
    }

    public static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        PetStateUtils.setShadowPunchStriking(enchanted, times);
    }

    public static int[] getShadowPunchStriking(LivingEntity enchanted) {
        return PetStateUtils.getShadowPunchStriking(enchanted);
    }

    /** 唱片机跟随者的跟随目标 UUID（disc_jockey 召唤的宠物跟随唱片实体），citadel tag 存储；未设置返回 null。 */
    public static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        PetJukeboxUtils.setPetJukeboxUUID(enchanted, id);
    }

    public static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        return PetJukeboxUtils.getPetJukeboxUUID(enchanted);
    }

    /** 唱片机跟随者正在播放的唱片 ItemStack（完整序列化到 citadel tag）；未设置返回 {@link ItemStack#EMPTY}。 */
    public static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        PetJukeboxUtils.setPetJukeboxDisc(enchanted, stack);
    }

    public static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        return PetJukeboxUtils.getPetJukeboxDisc(enchanted);
    }

    /** psychic_wall（念力墙）附魔的施放冷却 tick，citadel tag 存储。 */
    public static int getPsychicWallCooldown(LivingEntity enchanted) {
        return PetStateUtils.getPsychicWallCooldown(enchanted);
    }

    public static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setPsychicWallCooldown(enchanted, time);
    }

    /** intimidation（威吓）附魔的冷却 tick：归零后才能再次触发威吓，被吓退的怪物数量按剩余冷却递减。 */
    public static int getIntimidationCooldown(LivingEntity enchanted) {
        return PetStateUtils.getIntimidationCooldown(enchanted);
    }

    public static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setIntimidationCooldown(enchanted, time);
    }

    /** blazing_protection（炽焰守护）的吸收护盾充能值与重充冷却 tick，citadel tag 存储。 */
    public static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        return PetStateUtils.getBlazingProtectionCooldown(enchanted);
    }

    public static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        PetStateUtils.setBlazingProtectionCooldown(enchanted, time);
    }

    /** healing_aura（治疗光环）的相位计时 tick。运行时存 {@link PetSyncDataEntity} 独立 int 数据槽（高频每 tick 读写），存档经 LivingEntityMixin 桥接回 {@code PetHealingAuraTime} NBT 键。 */
    public static int getHealingAuraTime(LivingEntity enchanted) {
        return PetStateUtils.getHealingAuraTime(enchanted);
    }

    public static void setHealingAuraTime(LivingEntity enchanted, int time) {
        PetStateUtils.setHealingAuraTime(enchanted, time);
    }

    /** healing_aura 的脉冲标记（当前 tick 是否该发治疗脉冲），citadel tag 布尔存储。 */
    public static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        return PetStateUtils.getHealingAuraImpulse(enchanted);
    }

    public static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        PetStateUtils.setHealingAuraImpulse(enchanted, impulse);
    }

    /** blazing_protection 已充能的护盾条数（受击时逐条抵消伤害），citadel tag 存储。 */
    public static int getBlazingProtectionBars(LivingEntity enchanted) {
        return PetStateUtils.getBlazingProtectionBars(enchanted);
    }

    public static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        PetStateUtils.setBlazingProtectionBars(enchanted, time);
    }

    /** 宠物绑定的宠物床坐标（citadel tag 存储）；未绑定时返回 null。宠物床复活/拆除时读写。 */
    @Nullable
    public static BlockPos getPetBedPos(LivingEntity enchanted) {
        return PetStateUtils.getPetBedPos(enchanted);
    }

    public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        PetStateUtils.setPetBedPos(enchanted, petBed);
    }

    /** 解除宠物床绑定（只置 HAS_PET_BED=false，坐标键保留），床被拆/放生时调用。 */
    public static void removePetBedPos(LivingEntity enchanted) {
        PetStateUtils.removePetBedPos(enchanted);
    }

    /** 宠物床所在维度（字符串形式如 "minecraft:overworld"）；旧数据缺该键时按主世界处理。 */
    public static String getPetBedDimension(LivingEntity enchanted) {
        return PetStateUtils.getPetBedDimension(enchanted);
    }

    public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        PetStateUtils.setPetBedDimension(enchanted, dimension);
    }

    /** health_boost 存档保护：实体卸载/离开世界时记录的「安全血量」（避免下限回退导致血量异常），重新 join 时恢复用。 */
    public static double getSafePetHealth(LivingEntity enchanted) {
        return PetOwnershipUtils.getSafePetHealth(enchanted);
    }

    public static void setSafePetHealth(LivingEntity enchanted, double health) {
        PetOwnershipUtils.setSafePetHealth(enchanted, health);
    }

    /** attract（吸引）附魔：每 8 tick 把 16 格内最多 max 只非同主动物吸引过来（清仇恨并寻路走向施放者）。 */
    public static void attractAnimals(LivingEntity attractor, int max) {
        PetEnchantmentBehaviorUtils.attractAnimals(attractor, max);
    }

    /** aggro（挑衅）附魔：每 400 tick 让 20 格内最近的一只怪物把施放者设为攻击目标（拉仇恨保护主人）。 */
    public static void aggroRandomMonsters(LivingEntity attractor) {
        PetEnchantmentBehaviorUtils.aggroRandomMonsters(attractor);
    }

    /** intimidation（威吓）附魔主逻辑：按等级间隔（或受击瞬间）威吓 10*level 格内怪物逃离，同时播放威吓粒子与吼声；冷却期只递减冷却不重复触发。 */
    public static void scareRandomMonsters(LivingEntity scary, int level) {
        PetEnchantmentBehaviorUtils.scareRandomMonsters(scary, level);
    }

    /** ore_scenting（矿石嗅探）附魔：每 interval tick 播放嗅探动作/粒子，第 30 tick 扫描 range 范围内矿石方块，在各矿点上生成存在 effectLength tick 的发光高亮实体（{@link HighlightedBlockEntity}）。 */
    public static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
        PetEnchantmentBehaviorUtils.detectRandomOres(attractor, interval, range, effectLength, maxOres);
    }

    /** blight（枯萎）附魔：每 200 tick 随机破坏身边花草/作物，并把草方块/耕地退化为泥土或砂土，配枯萎粒子。 */
    public static void destroyRandomPlants(LivingEntity living) {
        PetEnchantmentBehaviorUtils.destroyRandomPlants(living);
    }

    /** 治疗光环的受疗对象列表：4 格内同主且未满血的存活实体（含主人与其他宠物）。 */
    public static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        return PetEntityQueryUtils.getAuraHealables(pet);
    }

    /** 16 格内拥有 healing_aura 且光环相位空闲（时间==0）的宠物，受伤实体据此就近挑选治疗者。 */
    public static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        return PetEntityQueryUtils.getNearbyHealers(hurtOwner);
    }

    /** Insight（洞察）：黑暗中让周围敌对生物发光，范围随附魔等级提升 */
    public static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
        PetEnchantmentBehaviorUtils.applyGlowingEffect(livingEntity, enchantLevel);
    }

    /** 范围内的存活生物（不含自身与玩家），share / sonic_boom 用 */
    public static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        return PetEntityQueryUtils.getNearbyMobs(entity, range);
    }

    /** Sonic Boom（音波轰击）：目标周围敌人密集时对群体释放，否则对主目标释放 */
    public static void performSonicBook(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        PetEnchantmentBehaviorUtils.performSonicBook(maid, monster, serverLevel);
    }

    /** 监守者音波攻击的复刻：从施放者头上 1.3 格向目标射出 SONIC_BOOM 粒子轨迹，命中造成 10 点 sonic_boom 伤害并按击退抗性缩放的击退。 */
    public static void sonicBoomAttack(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        PetEnchantmentBehaviorUtils.sonicBoomAttack(maid, monster, serverLevel);
    }

    /** 跨端同步的下落距离（citadel tag 存储）：void_cloud 缓降等附魔据此在客户端与服务端保持一致判定。 */
    public static float getFallDistance(LivingEntity enchanted) {
        return PetStateUtils.getFallDistance(enchanted);
    }

    public static void setFallDistance(LivingEntity enchanted, float dist) {
        PetStateUtils.setFallDistance(enchanted, dist);
    }

    /** 僵尸化宠物标记（ZombiePet）：感染类效果把宠物转为敌对僵尸形态时置位，供渲染与行为判定。 */
    public static void setZombiePet(LivingEntity enchanted, boolean zombiefied) {
        PetStateUtils.setZombiePet(enchanted, zombiefied);
    }

    public static boolean isZombiePet(LivingEntity enchanted) {
        return PetStateUtils.isZombiePet(enchanted);
    }

    /** charisma（魅力）附魔对主人的交易折扣加成：统计 25 格内自家宠物的 10*等级之和，上限 50（对应原版村民折扣公式）。 */
    public static int getCharismaBonusForOwner(Player player) {
        return PetJukeboxUtils.getCharismaBonusForOwner(player);
    }

    /** tethered_teleport（拴链传送）宠物是否有资格随主人传送：三态开启时须为跟随态；否则须未坐下且在 10 格内。玩家传送/换维度事件据此筛选随行宠物。 */
    public static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        return PetEntityQueryUtils.isValidTeleporter(owner, animal);
    }

    /** xp_transfer（经验转移）附魔：3 格内经验球被吸向宠物，2 格内被吸收并直接把经验值发给主人（宠物本体不加血），64 平方格内受引力影响。 */
    public static void xpTransfer(LivingEntity living) {
        PetEnchantmentBehaviorUtils.xpTransfer(living);
    }

    /** rejuvenation（回复）附魔：未满血时吸收 3 格内经验球回复自身（超出上限的余额留回经验球），仅服务端生效。 */
    public static void absorbExpOrbs(LivingEntity living) {
        PetEnchantmentBehaviorUtils.absorbExpOrbs(living);
    }
}

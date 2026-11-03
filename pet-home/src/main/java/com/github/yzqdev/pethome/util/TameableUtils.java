package com.github.yzqdev.pethome.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * 全模组核心工具类（<b>门面</b>）：驯服判定与归属、附魔读写、宠物计时器、宠物床坐标、行为辅助（音波/嗅矿/威吓等）。 几乎所有 server 代码与 mixin 依赖它。
 * 本侧（1.21）宠物数据在 {@code LivingEntity} 的 SynchedEntityData CompoundTag 槽内（非 26.1 的附件）。
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
 * <p>本类不承载游戏逻辑——所有 public 方法一行转发，签名/参数/返回类型/可见性/静态调用方式与拆分前一致， 引用文件与 mixin 无需任何改动。citadel tag
 * 的写入仍然只经 {@link PetDataSync#sync}（包内可见， 不对外暴露第二条写入路径）。
 *
 * <p>方法分区沿用原顺序：一、归属与驯服判定 → 二、灯笼卸载判定 → 三、宠物床坐标 → 四、附魔读写与属性 → 五、计时器 → 六、行为辅助 → 七、传送与近距离查询 →
 * 八、disc_jockey / charisma。
 *
 * @author yzqde
 * @date time 2025/1/9 7:52
 * @modified By: 2026-10-23 职责拆分为包内工具类，本类保留为兼容门面
 */
public class TameableUtils {

  /** 冻结计时 tag 键（真值在 {@link PetDataKeys#FROZEN_TIME_TAG}）。 */
  public static final String FROZEN_TIME_TAG = PetDataKeys.FROZEN_TIME_TAG;

  /** 治愈光环相位 tag 键（真值在 {@link PetDataKeys#HEALING_AURA_TIME}）。 */
  public static final String HEALING_AURA_TIME = PetDataKeys.HEALING_AURA_TIME;

  /** 归属 UUID：兼容本 mod 改造动物（ModifiedToBeTameable）与原版 TamableAnimal；非驯服实体返回 {@code null} */
  public static UUID getOwnerUUIDOf(Entity entity) {
    return PetOwnershipUtils.getOwnerUUIDOf(entity);
  }

  public static boolean hasCollar(LivingEntity enchanted) {
    return PetOwnershipUtils.hasCollar(enchanted);
  }

  /**
   * 卸载时是否应记入迷途灯笼（召回等待）。只认「跟随状态」： 三态指令开启时 command==2（跟随）才召回；关闭时读实体 NBT 里任意 {@code *Command} 键 （兼容
   * Alex's Mobs 等第三方宠物，其编号 1=跟随，跳过本模组的 DICommand）， 都没有则按原版 TamableAnimal「未坐下=跟随」兜底。 由
   * ServerEvent.onEntityLeaveWorld 在实体随区块卸载时调用。
   */
  public static boolean shouldUnloadToLantern(LivingEntity tameable) {
    return PetOwnershipUtils.shouldUnloadToLantern(tameable);
  }

  /** health_boost 宠物的「卸载前安全血量」：离开世界时记录、join 时用它抬回当前血量（防血上限回落扣血） */
  public static double getSafePetHealth(LivingEntity enchanted) {
    return PetOwnershipUtils.getSafePetHealth(enchanted);
  }

  public static void setSafePetHealth(LivingEntity enchanted, double health) {
    PetOwnershipUtils.setSafePetHealth(enchanted, health);
  }

  /** 重算物种开关表（{@link PetHomeConfig} 配置加载后调用）。 */
  public static void refreshSpeciesGates() {
    PetOwnershipUtils.refreshSpeciesGates();
  }

  /**
   * 驯服判定。美西螈/狐狸/兔子/青蛙是本 mod 经 mixin 改造的可驯服动物， 需 {@code ModifiedToBeTameable.isTame()}
   * 且对应配置开关（tameable_axolotl 等）同时开启； 其余实体按 ModifiedToBeTameable / TamableAnimal 的 isTame 判定。
   */
  public static boolean isTamed(Entity entity) {
    return PetOwnershipUtils.isTamed(entity);
  }

  /** player 是否是 entity 的主人（或友军）：team 友军判定，或单向 owner 链匹配。 交互/保护/传送判定的高频入口，client/server 两端都可用。 */
  public static boolean isPetOf(Player player, Entity entity) {
    return PetOwnershipUtils.isPetOf(player, entity);
  }

  /** 绑定的宠物床坐标；未绑定（HasPetBed=false）返回 {@code null}。死亡复活/罗盘传送都以此为准 */
  @Nullable
  public static BlockPos getPetBedPos(LivingEntity enchanted) {
    return PetStateUtils.getPetBedPos(enchanted);
  }

  public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
    PetStateUtils.setPetBedPos(enchanted, petBed);
  }

  public static void removePetBedPos(LivingEntity enchanted) {
    PetStateUtils.removePetBedPos(enchanted);
  }

  /** 灯笼/复活需要维度信息：未记录时默认主世界。写入路径 setPetBedDimension 经 sync 同步 */
  public static String getPetBedDimension(LivingEntity enchanted) {
    return PetStateUtils.getPetBedDimension(enchanted);
  }

  public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
    PetStateUtils.setPetBedDimension(enchanted, dimension);
  }

  public static Entity getOwnerOf(Entity entity) {
    return PetOwnershipUtils.getOwnerOf(entity);
  }

  public static int getBlazingProtectionBars(LivingEntity enchanted) {
    return PetStateUtils.getBlazingProtectionBars(enchanted);
  }

  public static int getBlazingProtectionCooldown(LivingEntity enchanted) {
    return PetStateUtils.getBlazingProtectionCooldown(enchanted);
  }

  public static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
    PetStateUtils.setBlazingProtectionCooldown(enchanted, time);
  }

  public static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
    PetStateUtils.setBlazingProtectionBars(enchanted, time);
  }

  /** 清空宠物全部附魔（项圈替换/契约解除时调用），并触发属性修饰符重算 */
  public static void clearEnchants(LivingEntity entity) {
    PetEnchantmentUtils.clearEnchants(entity);
  }

  /** 有该宠物附魔即等级 &gt; 0 */
  public static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
    return PetEnchantmentUtils.hasEnchant(entity, enchantment);
  }

  /**
   * 附魔等级（mixin/渲染/tick 热路径入口）：PetSyncDataEntity 实现者走实体级缓存 （{@code ph_getEnchantCache}，tag
   * 引用比对失效），否则全量扫描 NBT 列表。无该附魔返回 0。
   */
  public static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
    return PetEnchantmentUtils.getEnchantLevel(entity, enchantment);
  }

  /** 把 citadel tag 里的附魔列表解析为附魔 ID → level 映射（供实体缓存重建用） */
  public static Map<ResourceLocation, Integer> buildEnchantCache(CompoundTag tag) {
    return PetEnchantmentUtils.buildEnchantCache(tag);
  }

  /** 宠物附魔描述行（金色标题 + 每条附魔名与等级；含 "curse" 的显示为红色）。 供 Jade/tooltip 等展示宠物附魔概览。 */
  public static List<Component> getEnchantDescriptions(LivingEntity entity) {
    return PetEnchantmentUtils.getEnchantDescriptions(entity);
  }

  /** 附魔列表 → {附魔 id → 等级}；无 StoredPetEnchantments 键时返回 {@code null}（与空列表区分） */
  public static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
    return PetEnchantmentUtils.getEnchants(entity);
  }

  public static CompoundTag storeEnchantment(@Nullable ResourceLocation pId, int pLevel) {
    return PetEnchantmentUtils.storeEnchantment(pId, pLevel);
  }

  /** 把物品附魔（{@code ItemEnchantments}，如项圈物品上记录的）整体写入宠物： 是替换而非追加——换项圈时旧附魔已先经 clearEnchants 落回旧项圈物品。 */
  public static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
    PetEnchantmentUtils.addEnchant(entity, itemEnchantments);
  }

  public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
    return PetOwnershipUtils.hasSameOwnerAs(tameable, target);
  }

  /** xp_transfer：吸附 3 格内经验球给主人（近距直接到账，8 格内施加吸力）；宠物自身不给经验 */
  public static void xpTransfer(LivingEntity living) {
    PetEnchantmentBehaviorUtils.xpTransfer(living);
  }

  /** rejuvenation：吸收 3 格内经验球给自己回血（满血停止；溢出部分折回经验球价值），仅服务端 */
  public static void absorbExpOrbs(LivingEntity living) {
    PetEnchantmentBehaviorUtils.absorbExpOrbs(living);
  }

  /** 治疗光环相位计时器：走 PH_HEALING_AURA_TIME 独立 int 数据槽（每 tick 增减只同步小整数），非数据槽实现者恒 0 */
  public static int getHealingAuraTime(LivingEntity enchanted) {
    return PetStateUtils.getHealingAuraTime(enchanted);
  }

  public static void setHealingAuraTime(LivingEntity enchanted, int time) {
    PetStateUtils.setHealingAuraTime(enchanted, time);
  }

  public static long getSonicboomAuraTime(LivingEntity enchanted) {
    return PetStateUtils.getSonicboomAuraTime(enchanted);
  }

  public static void setSonicboomAuraTime(LivingEntity enchanted, long time) {
    PetStateUtils.setSonicboomAuraTime(enchanted, time);
  }

  public static boolean getHealingAuraImpulse(LivingEntity enchanted) {
    return PetStateUtils.getHealingAuraImpulse(enchanted);
  }

  public static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
    PetStateUtils.setHealingAuraImpulse(enchanted, impulse);
  }

  public static List<LivingEntity> getAuraHealables(LivingEntity pet) {
    return PetEntityQueryUtils.getAuraHealables(pet);
  }

  public static int getPsychicWallCooldown(LivingEntity enchanted) {
    return PetStateUtils.getPsychicWallCooldown(enchanted);
  }

  public static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
    PetStateUtils.setPsychicWallCooldown(enchanted, time);
  }

  public static void attractAnimals(LivingEntity attractor, int max) {
    PetEnchantmentBehaviorUtils.attractAnimals(attractor, max);
  }

  public static int getPetAttackTargetID(LivingEntity enchanted) {
    return PetStateUtils.getPetAttackTargetID(enchanted);
  }

  @Nullable
  public static Entity getPetAttackTarget(LivingEntity enchanted) {
    return PetStateUtils.getPetAttackTarget(enchanted);
  }

  public static void setPetAttackTarget(LivingEntity enchanted, int id) {
    PetStateUtils.setPetAttackTarget(enchanted, id);
  }

  public static void aggroRandomMonsters(LivingEntity attractor) {
    PetEnchantmentBehaviorUtils.aggroRandomMonsters(attractor);
  }

  public static int getIntimidationCooldown(LivingEntity enchanted) {
    return PetStateUtils.getIntimidationCooldown(enchanted);
  }

  public static void setIntimidationCooldown(LivingEntity enchanted, int time) {
    PetStateUtils.setIntimidationCooldown(enchanted, time);
  }

  public static void scareRandomMonsters(LivingEntity scary, int level) {
    PetEnchantmentBehaviorUtils.scareRandomMonsters(scary, level);
  }

  /** 理论上可驯服（无需已驯服）：本 mod 改造动物或原版 TamableAnimal */
  public static boolean couldBeTamed(Entity entity) {
    return PetOwnershipUtils.couldBeTamed(entity);
  }

  public static void destroyRandomPlants(LivingEntity living) {
    PetEnchantmentBehaviorUtils.destroyRandomPlants(living);
  }

  public static void detectRandomOres(
      LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
    PetEnchantmentBehaviorUtils.detectRandomOres(attractor, interval, range, effectLength, maxOres);
  }

  /** 免疫帧剩余时间：仅免疫帧附魔存在时有效（无附魔读写都被跳过），由 EntityTickHandler 每 tick 递减 */
  public static int getImmuneTime(LivingEntity enchanted) {
    return PetStateUtils.getImmuneTime(enchanted);
  }

  public static void setImmuneTime(LivingEntity enchanted, int time) {
    PetStateUtils.setImmuneTime(enchanted, time);
  }

  /** 冰冻倒计时：走 PH_FROZEN_TIME 独立 int 数据槽（frost_fang 命中时由伤害管线写入），非数据槽实现者恒 0 */
  public static int getFrozenTime(LivingEntity enchanted) {
    return PetStateUtils.getFrozenTime(enchanted);
  }

  public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
    PetStateUtils.setFrozenTimeTag(enchanted, time);
  }

  public static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
    return PetEntityQueryUtils.getNearbyHealers(hurtOwner);
  }

  public static float getFallDistance(LivingEntity enchanted) {
    return PetStateUtils.getFallDistance(enchanted);
  }

  public static void setFallDistance(LivingEntity enchanted, float dist) {
    PetStateUtils.setFallDistance(enchanted, dist);
  }

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

  /**
   * tethered_teleport 的随行者判定（ServerEvent.teleportNearbyPets 的过滤条件）：
   * 需有附魔，且三态跟随（command==2）或原版未坐下且距主人 10 格内。
   */
  public static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
    return PetEntityQueryUtils.isValidTeleporter(owner, animal);
  }

  public static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
    PetEnchantmentBehaviorUtils.applyGlowingEffect(livingEntity, enchantLevel);
  }

  public static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
    return PetEntityQueryUtils.getNearbyMobs(entity, range);
  }

  /**
   * sonic_boom 附魔攻击：附近敌方生物多于 3 个时对全部目标齐射，否则只打当前目标； 服务端调用（sendParticles/伤害）。{@code sonicBoomAttack}
   * 画音波粒子轨迹并造成 10 点音波伤害+击退。
   */
  public static void performSonicBook(
      LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
    PetEnchantmentBehaviorUtils.performSonicBook(maid, monster, serverLevel);
  }

  public static void sonicBoomAttack(
      LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
    PetEnchantmentBehaviorUtils.sonicBoomAttack(maid, monster, serverLevel);
  }

  /** charisma（魅力，自 1.20 移植）对主人的交易折扣加成：25 格内自家宠物的 10×等级之和，上限 50。 */
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

  /** disc_jockey 唱片机当前播放的唱片 ItemStack（整体序列化到 citadel tag）；未设置返回 EMPTY。 */
  public static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
    return PetJukeboxUtils.getPetJukeboxDisc(enchanted);
  }

  public static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
    PetJukeboxUtils.setPetJukeboxDisc(enchanted, stack);
  }
}

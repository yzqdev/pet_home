package com.github.yzqdev.pethome.server.entity;


import com.github.yzqdev.pethome.PHConstants;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.mixin.EntityInvoker;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.network.PropertiesMessage;
import com.github.yzqdev.pethome.server.NbtKeys;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.core.registries.BuiltInRegistries;
import com.github.yzqdev.pethome.server.misc.PHTagRegistry;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宠物 API 主体：所有“宠物身上的数据”（附魔表、项圈标记、宠物床坐标、各冷却 / 进度数组、
 * 僵尸标记、安全血量等）都以私有常量为键存进宠物的 citadel tag（见 {@link CitadelEntityData}），
 * 本类统一读写并触发同步。归属判定走两条分支：{@code TamableAnimal}（原版宠物）与
 * {@link ModifiedToBeTameable}（Mixin 扩展物种：狐狸 / 兔子 / 美西螈 / 青蛙 / 马类）。
 *
 * <p>各 {@code getXxx/setXxx} 成对访问器（冷却、出拳数组、治疗光环计时等）读写模式一致：
 * 取 tag → 改键 → {@link #sync}，此处不再逐一注释。</p>
 */
public class TameableUtils {

    private static final String ENCHANTMENT_TAG = "StoredPetEnchantments";
    private static final String COLLAR_TAG = "HasPetCollar";
    private static final String IMMUNITY_TIME_TAG = "PetImmunityTimer";
    private static final String FROZEN_TIME_TAG = "PetFrozenTime";
    private static final String ATTACK_TARGET_ENTITY = "PetAttackTarget";
    private static final String SHADOW_PUNCH_TIMES = "PetShadowPunchTimes";
    private static final String SHADOW_PUNCH_COOLDOWN = "PetShadowPunchCooldown";
    private static final String PSYCHIC_WALL_COOLDOWN = "PetPsychicWallCooldown";
    private static final String INTIMIDATION_COOLDOWN = "PetIntimidationCooldown";
    private static final String SHADOW_PUNCH_STRIKING = "PetShadowPunchStriking";
    private static final String JUKEBOX_FOLLOWER_UUID = "PetJukeboxFollowerUUID";
    private static final String JUKEBOX_FOLLOWER_DISC = "PetJukeboxFollowerDisc";
    private static final String BLAZING_PROTECTION_BARS = "PetBlazingProtectionBars";
    private static final String BLAZING_PROTECTION_COOLDOWN = "PetBlazingProtectionCooldown";
    private static final String HEALING_AURA_TIME = "PetHealingAuraTime";
    private static final String HEALING_AURA_IMPULSE = "PetHealingAuraImpulse";
    private static final String HAS_PET_BED = "HasPetBed";
    private static final String PET_BED_X = "PetBedX";
    private static final String PET_BED_Y = "PetBedY";
    private static final String PET_BED_Z = "PetBedZ";
    private static final String PET_BED_DIMENSION = "PetBedDimension";
    private static final String FALL_DISTANCE_SYNC = "SyncedFallDistance";
    private static final String SAFE_PET_HEALTH = "SafePetHealth";
    private static final String COLLAR_SWAP_COOLDOWN = "CollarSwapCooldown";
    private static final UUID HEALTH_BOOST_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B166EEEEE");
    private static final UUID SPEED_BOOST_UUID = UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31744");

    private static final UUID SPEED_BOOST_AQUATIC_LAND_UUID = UUID.fromString("ff465ded-9040-4eb5-93a1-7bbe97c31745");
    // TOUGH 附魔属性修改器（自 1.21 移植）
    private static final UUID TOUGH_ARMOR_UUID = UUID.fromString("6f2a5c1d-93b7-4a2e-8d40-1c5f7a9b2e31");
    private static final UUID TOUGH_KNOCKBACK_UUID = UUID.fromString("8c4d9e2f-1a6b-4f70-b3e5-9d2c8a7f4b62");

    /** 两实体是否同主人（双向判定，兼容原版宠物与 Mixin 扩展物种的任意组合）。 */
    public static boolean hasSameOwnerAs(LivingEntity tameable, Entity target) {
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
     * 区块卸载时该宠物是否应被迷途灯笼收容（由 {@code ServerEvent.onEntityLeaveWorld} 调用）。
     * 只认「跟随状态」：三态指令开启时 command==2（跟随）才召回；三态关闭时本模组宠物按「未停留=跟随」兜底；
     * 第三方宠物（如 Alex's Mobs，未实现 IComandableMob）读其 NBT 里 {@code *Command} 键（编号 1=跟随），
     * 都没有再按「原版可驯服动物未坐下=跟随」兜底。
     * 第三方扫描会序列化一遍实体附加数据，调用方注意别在热路径高频使用（本模组宠物走内存判定，无序列化）。
     */
    public static boolean shouldUnloadToLantern(LivingEntity tameable) {
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
     * 读第三方宠物 NBT 里的 {@code *Command} 键（兼容 Alex's Mobs 等：其实体未实现 IComandableMob，编号 1=跟随）。
     * 必须跳过本模组自己的 {@code DICommand}：它按三态编号写（2 = 跟随），与第三方编号体系不通用——
     * 三态关闭时若把自家宠物的 DICommand 当第三方键读，跟随中的宠物会被误判不召回。
     * 经 {@link EntityInvoker} 序列化附加数据（该方法 protected，直接调用不可达）。
     */
    private static int readForeignCommand(LivingEntity tameable) {
        CompoundTag tag = new CompoundTag();
        ((EntityInvoker) tameable).pet_home$invokeAddAdditionalSaveData(tag);
        int command = -1;
        for (String s : tag.getAllKeys()) {
            if (s.endsWith("Command") && !NbtKeys.DI_COMMAND.equals(s) && tag.contains(s, 99)) {
                command = tag.getInt(s);
            }
        }
        return command;
    }

    /** 实体是否属于该玩家（同盟判定或主人即该玩家；null 实体返回 false）。 */
    public static boolean isPetOf(Player player, Entity entity) {
        return entity != null && (entity.isAlliedTo(player) || hasSameOwnerAsOneWay(entity, player));
    }

    /**
     * 改造物种 → {@code PetHomeConfig.tameable*} 开关的运行时视图（{@link #isTamed} 用）：
     * {@code null} 值表示该物种不是改造驯服对象（无需开关门）。马走 {@code TamableAnimal} 分支 +
     * {@code AbstractHorseMixin} 自身覆写 {@code isTame}，不经此表。惰性初始化，配置刷新后由
     * {@code PetHomeConfig.refreshCachedValues} 重算。
     */
    private static final Map<EntityType<?>, Boolean> SPECIES_TAME_GATES = new HashMap<>();

    /** 重算物种开关表（配置加载 / 配置界面保存后调用）。 */
    public static void refreshSpeciesGates() {
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
    public static boolean isTamed(Entity entity) {
        // 改造实体的 Mixin 接口在运行时可能未绑定，需先 instanceof 再判 isTame
        if (entity instanceof ModifiedToBeTameable ModifiedToBeTameable) {
            if (!ModifiedToBeTameable.isTame()) {
                return false;
            }
            Boolean gate = SPECIES_TAME_GATES.get(entity.getType());
            return gate == null || gate;
        }
        return entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame();
    }

    /** 是否属于本模组认定的“可驯服类型”（原版 {@code TamableAnimal} 或 Mixin 扩展物种），与是否真的已驯服无关。 */
    public static boolean couldBeTamed(Entity entity) {
        return entity instanceof ModifiedToBeTameable || entity instanceof TamableAnimal;
    }


    /** 取主人实体（两条归属分支），无主人返回 null。 */
    public static Entity getOwnerOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwner();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwner();
        }
        return null;
    }

    public static UUID getOwnerUUIDOf(Entity entity) {
        if (entity instanceof ModifiedToBeTameable) {
            return ((ModifiedToBeTameable) entity).getTameOwnerUUID();
        }
        if (entity instanceof TamableAnimal) {
            return ((TamableAnimal) entity).getOwnerUUID();
        }
        return null;
    }

    public static void setOwnerUUIDOf(Entity entity, UUID uuid) {
        if (entity instanceof ModifiedToBeTameable) {
            ((ModifiedToBeTameable) entity).setTameOwnerUUID(uuid);
        }
        if (entity instanceof TamableAnimal) {
            ((TamableAnimal) entity).setOwnerUUID(uuid);
        }
    }

    /** 写入整张附魔表：同时标记“已戴项圈”、设 20 tick 换装冷却，并触发 {@link #onUpdateEnchants} 重算属性修饰。 */
    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        Map<ResourceLocation, Integer> prevEnchants = getEnchants(enchanted);
        tag.put(ENCHANTMENT_TAG, enchants);
        tag.putInt(COLLAR_SWAP_COOLDOWN, 20);
        tag.putBoolean(COLLAR_TAG, true);
        sync(enchanted, tag);
        onUpdateEnchants(prevEnchants, enchanted);
    }

    /** 附魔变更后的属性修饰重算：HEALTH_BOOST +10/级 最大生命、SPEEDSTER +0.075/级 速度、TOUGH +3/级 护甲与击退抗性、两栖对水生生物的陆上加速 +0.13；IMMATURITY_CURSE 强制幼年（换 FALL_FLYING 姿态刷新客户端）。 */
    private static void onUpdateEnchants(@Nullable Map<ResourceLocation, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.SPEEDSTER);
        boolean amphib = hasEnchant(enchanted, PHEnchantmentRegistry.AMPHIBIOUS) && !enchanted.isInWaterOrBubble() && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        if (hasEnchant(enchanted, PHEnchantmentRegistry.IMMATURITY_CURSE) || prevEnchants != null && prevEnchants.keySet().contains(BuiltInRegistries.ENCHANTMENT.getKey(PHEnchantmentRegistry.IMMATURITY_CURSE))) {
            //change pose to update client
            enchanted.setPose(Pose.FALL_FLYING);
            AgeableMob ageable = (AgeableMob) enchanted;
            ageable.setBaby(true); // 直接设为幼年
            enchanted.refreshDimensions();
        }
        if (health != null) {
            if (healthExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(HEALTH_BOOST_UUID, "health boost pet upgrade", healthExtra * 10, AttributeModifier.Operation.ADDITION);
                if (health.hasModifier(attributemodifier)) {
                    health.removeModifier(attributemodifier);
                    health.addPermanentModifier(attributemodifier);
                } else {
                    health.addPermanentModifier(attributemodifier);
                }
            } else {
                health.removePermanentModifier(HEALTH_BOOST_UUID);
            }
        }
        if (speed != null) {
            if (speedExtra > 0) {
                AttributeModifier attributemodifier = new AttributeModifier(SPEED_BOOST_UUID, "speedster pet upgrade", speedExtra * 0.075F, AttributeModifier.Operation.ADDITION);
                if (speed.hasModifier(attributemodifier)) {
                    speed.removeModifier(attributemodifier);
                    speed.addPermanentModifier(attributemodifier);
                } else {
                    speed.addPermanentModifier(attributemodifier);
                }
            } else {
                speed.removePermanentModifier(SPEED_BOOST_UUID);
            }
            if (amphib) {
                AttributeModifier attributemodifier = new AttributeModifier(SPEED_BOOST_AQUATIC_LAND_UUID, "amphibious pet upgrade", 0.13F, AttributeModifier.Operation.ADDITION);
                if (speed.hasModifier(attributemodifier)) {
                    speed.removeModifier(attributemodifier);
                    speed.addPermanentModifier(attributemodifier);
                } else {
                    speed.addPermanentModifier(attributemodifier);
                }
            } else {
                speed.removePermanentModifier(SPEED_BOOST_AQUATIC_LAND_UUID);
            }
        }
        // TOUGH（稳固）：每级 +3 护甲与 +3 击退抗性（自 1.21 移植）
        int toughExtra = getEnchantLevel(enchanted, PHEnchantmentRegistry.TOUGH);
        AttributeInstance armor = enchanted.getAttribute(Attributes.ARMOR);
        AttributeInstance knockbackResistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (armor != null) {
            if (toughExtra > 0) {
                AttributeModifier armorBoost = new AttributeModifier(TOUGH_ARMOR_UUID, "tough pet armor upgrade", toughExtra * 3, AttributeModifier.Operation.ADDITION);
                if (armor.hasModifier(armorBoost)) {
                    armor.removeModifier(armorBoost);
                }
                armor.addPermanentModifier(armorBoost);
            } else {
                armor.removePermanentModifier(TOUGH_ARMOR_UUID);
            }
        }
        if (knockbackResistance != null) {
            if (toughExtra > 0) {
                AttributeModifier resBoost = new AttributeModifier(TOUGH_KNOCKBACK_UUID, "tough pet knockback resistance upgrade", toughExtra * 3, AttributeModifier.Operation.ADDITION);
                if (knockbackResistance.hasModifier(resBoost)) {
                    knockbackResistance.removeModifier(resBoost);
                }
                knockbackResistance.addPermanentModifier(resBoost);
            } else {
                knockbackResistance.removePermanentModifier(TOUGH_KNOCKBACK_UUID);
            }
        }
    }

    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    @Nullable
    private static ListTag getEnchantmentList(LivingEntity entity) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (tag.contains(ENCHANTMENT_TAG)) {
            return tag.getList(ENCHANTMENT_TAG, 10);
        }
        return null;
    }

    /** 附魔等级读取的单点入口：读取宠物 citadel tag 中的 {@code StoredPetEnchantments}；被配置 {@code <名>_enabled} 禁用的附魔一律返回 0 级（全部效果随之失效）。 */
    public static int getEnchantLevel(LivingEntity entity, Enchantment enchantment) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag != null && PetHomeMod.CONFIG.isEnchantEnabled(enchantment)) {
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (res != null && res.equals(BuiltInRegistries.ENCHANTMENT.getKey(enchantment))) {
                    return EnchantmentHelper.getEnchantmentLevel(compoundtag);
                }
            }
        }
        return 0;
    }

    /** 事件层最常用的存在性判断：等价于 {@link #getEnchantLevel} > 0（同样受配置禁用影响）。 */
    public static boolean hasEnchant(LivingEntity entity, Enchantment enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    /** 完整附魔表（注册名 → 等级；被配置禁用的不出现）；从未挂过附魔返回 {@code null}（注意与空表的区分）。 */
    @Nullable
    public static Map<ResourceLocation, Integer> getEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        if (listtag == null) {
            return null;
        }
        Map<ResourceLocation, Integer> enchants = new HashMap<>();
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation res = EnchantmentHelper.getEnchantmentId(compoundtag);
            if (PetHomeMod.CONFIG.isEnchantEnabled(res.getPath())) {
                enchants.put(res, EnchantmentHelper.getEnchantmentLevel(compoundtag));
            }
        }
        return enchants;
    }

    /** 附魔列表展示行（Jade 悬浮提示 / 配置 HUD 共用）：标题 + 逐条「名称 等级」，诅咒类红色。 */
    public static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ").append(Component.translatable("message.pet_home.enchantments").withStyle(ChatFormatting.GOLD)));
        Map<ResourceLocation, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {
                if (PetHomeMod.CONFIG.isEnchantEnabled(entry.getKey().getPath())) {
                    boolean isCurse = entry.getKey().getPath().contains("curse");
                    list.add(Component.translatable("enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath()).append(Component.literal(" ")).append(Component.translatable("enchantment.level." + entry.getValue())).withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));
                }
            }
        }
        return list;
    }

    /** 是否挂载过任意宠物附魔（不判配置开关）。 */
    public static boolean hasAnyEnchants(LivingEntity entity) {
        ListTag listtag = getEnchantmentList(entity);
        return listtag != null && !listtag.isEmpty();
    }

    /** 追加一条附魔（同 id 已存在时只升不降等级）；被配置禁用的附魔直接忽略。 */
    public static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment) {
        ListTag listtag = getEnchantmentList(entity);
        addEnchant(entity, enchantment, listtag);
    }

    /** {@link #addEnchant} 的批量形态：在调用方持有的 ListTag 上逐条写入后统一落 tag（项圈标签替换用）。 */
    public static void addEnchant(LivingEntity entity, EnchantmentInstance enchantment, ListTag listtag) {
        if (listtag != null && PetHomeMod.CONFIG.isEnchantEnabled(enchantment.enchantment)) {
            ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(enchantment.enchantment);
            boolean flag = true;
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
                if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
                    if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < enchantment.level) {
                        EnchantmentHelper.setEnchantmentLevel(compoundtag, enchantment.level);
                    }

                    flag = false;
                    break;
                }
            }
            if (flag) {
                listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, enchantment.level));
            }
            setEnchantmentTag(entity, listtag);
        }
    }

    /** 清空全部宠物附魔（走 {@link #setEnchantmentTag}，属性修饰随之重算）。 */
    public static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    /** 项圈标记（{@code HasPetCollar}）：决定换装时是否掉落旧项圈物品。 */
    public static void setHasCollar(LivingEntity enchanted, boolean collar) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(COLLAR_TAG, collar);
        sync(enchanted, tag);
    }

    public static boolean hasCollar(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(COLLAR_TAG) && tag.getBoolean(COLLAR_TAG);
    }

    public static int getImmuneTime(LivingEntity enchanted) {
        if (hasEnchant(enchanted, PHEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            return tag.getInt(IMMUNITY_TIME_TAG);
        }
        return 0;
    }

    public static void setImmuneTime(LivingEntity enchanted, int time) {
        if (hasEnchant(enchanted, PHEnchantmentRegistry.IMMUNITY_FRAME)) {
            CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
            tag.putInt(IMMUNITY_TIME_TAG, time);
            sync(enchanted, tag);
        }
    }

    public static int getFrozenTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(FROZEN_TIME_TAG);
    }

    public static void setFrozenTimeTag(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(FROZEN_TIME_TAG, time);
        sync(enchanted, tag);
    }

    public static int getPetAttackTargetID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(ATTACK_TARGET_ENTITY) ? -1 : tag.getInt(ATTACK_TARGET_ENTITY);
    }

    @Nullable
    public static Entity getPetAttackTarget(LivingEntity enchanted) {
        int i = getPetAttackTargetID(enchanted);
        return i == -1 ? null : enchanted.level().getEntity(i);
    }

    public static void setPetAttackTarget(LivingEntity enchanted, int id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(ATTACK_TARGET_ENTITY, id);
        sync(enchanted, tag);
    }

    public static int getShadowPunchCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(SHADOW_PUNCH_COOLDOWN);
    }

    public static void setShadowPunchCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(SHADOW_PUNCH_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchTimes(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_TIMES);
    }

    public static void setShadowPunchTimes(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_TIMES, times);
        sync(enchanted, tag);
    }

    public static void setShadowPunchStriking(LivingEntity enchanted, int[] times) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putIntArray(SHADOW_PUNCH_STRIKING, times);
        sync(enchanted, tag);
    }

    public static int[] getShadowPunchStriking(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getIntArray(SHADOW_PUNCH_STRIKING);
    }

    public static void setPetJukeboxUUID(LivingEntity enchanted, UUID id) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putUUID(JUKEBOX_FOLLOWER_UUID, id);
        sync(enchanted, tag);
    }

    public static UUID getPetJukeboxUUID(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(JUKEBOX_FOLLOWER_UUID) ? tag.getUUID(JUKEBOX_FOLLOWER_UUID) : null;
    }

    public static void setPetJukeboxDisc(LivingEntity enchanted, ItemStack stack) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.put(JUKEBOX_FOLLOWER_DISC, stack.save(new CompoundTag()));
        sync(enchanted, tag);
    }

    public static ItemStack getPetJukeboxDisc(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.contains(JUKEBOX_FOLLOWER_DISC) ? ItemStack.of(tag.getCompound(JUKEBOX_FOLLOWER_DISC)) : ItemStack.EMPTY;
    }

    public static int getPsychicWallCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(PSYCHIC_WALL_COOLDOWN);
    }

    public static void setPsychicWallCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(PSYCHIC_WALL_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int getIntimidationCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(INTIMIDATION_COOLDOWN);
    }

    public static void setIntimidationCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(INTIMIDATION_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int getBlazingProtectionCooldown(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(BLAZING_PROTECTION_COOLDOWN);
    }

    public static void setBlazingProtectionCooldown(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(BLAZING_PROTECTION_COOLDOWN, time);
        sync(enchanted, tag);
    }

    public static int getHealingAuraTime(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(HEALING_AURA_TIME);
    }

    public static void setHealingAuraTime(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(HEALING_AURA_TIME, time);
        sync(enchanted, tag);
    }


    public static boolean getHealingAuraImpulse(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getBoolean(HEALING_AURA_IMPULSE);
    }

    public static void setHealingAuraImpulse(LivingEntity enchanted, boolean impulse) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HEALING_AURA_IMPULSE, impulse);
        sync(enchanted, tag);
    }

    public static int getBlazingProtectionBars(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getInt(BLAZING_PROTECTION_BARS);
    }

    public static void setBlazingProtectionBars(LivingEntity enchanted, int time) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putInt(BLAZING_PROTECTION_BARS, time);
        sync(enchanted, tag);
    }

    /** 同步入口：写回 citadel tag（SynchedEntityData 通道，自动同步给追踪者）后，服务端把整个 tag 广播给全体玩家、客户端回发服务端（服务端校验主人后才落库）。 */
    private static void sync(LivingEntity enchanted, CompoundTag tag) {
        CitadelEntityData.setCitadelTag(enchanted, tag);
        if (!enchanted.level().isClientSide()) {
            Networking.sendEntityTagUpdateToAll(wrapEntityTag(tag, enchanted.getId()));
        } else {
            Networking.sendEntityTagUpdate(wrapEntityTag(tag, enchanted.getId()));
        }
    }

    /** citadel tag 同步包负载：业务 tag 打包 EntityId（五通道拆分后实体 id 随 tag 传输）。 */
    private static CompoundTag wrapEntityTag(CompoundTag tag, int entityId) {
        CompoundTag payload = tag == null ? new CompoundTag() : tag.copy();
        payload.putInt("EntityId", entityId);
        return payload;
    }

    /** 宠物床绑定坐标（{@code HasPetBed + PetBedX/Y/Z}）；未绑定时返回 null。 */
    @Nullable
    public static BlockPos getPetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        if (tag.getBoolean(HAS_PET_BED) && tag.contains(PET_BED_X) && tag.contains(PET_BED_Y) && tag.contains(PET_BED_Z)) {
            return new BlockPos(tag.getInt(PET_BED_X), tag.getInt(PET_BED_Y), tag.getInt(PET_BED_Z));
        }
        return null;
    }

    public static void setPetBedPos(LivingEntity enchanted, BlockPos petBed) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, true);
        tag.putInt(PET_BED_X, petBed.getX());
        tag.putInt(PET_BED_Y, petBed.getY());
        tag.putInt(PET_BED_Z, petBed.getZ());
        sync(enchanted, tag);
    }

    public static void removePetBedPos(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putBoolean(HAS_PET_BED, false);
        sync(enchanted, tag);
    }

    /** 宠物床所在维度（{@code PetBedDimension}），缺失时按主世界处理——复活流据此跨维度归位。 */
    public static String getPetBedDimension(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return !tag.contains(PET_BED_DIMENSION) ? "minecraft:overworld" : tag.getString(PET_BED_DIMENSION);
    }

    public static void setPetBedDimension(LivingEntity enchanted, String dimension) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putString(PET_BED_DIMENSION, dimension);
        sync(enchanted, tag);
    }

    /** HEALTH_BOOST 宠物离开世界时保存的“安全血量”（{@code SafePetHealth}），回到世界时恢复下限。 */
    public static double getSafePetHealth(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getDouble(SAFE_PET_HEALTH);
    }

    public static void setSafePetHealth(LivingEntity enchanted, double health) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putDouble(SAFE_PET_HEALTH, health);
        sync(enchanted, tag);
    }

    /** 牧羊人（SHEPHERD）：每 8 tick 把周围最近的至多 max 只动物（非同主人）引到身边并清仇恨。 */
    public static void attractAnimals(LivingEntity attractor, int max) {
        if ((attractor.tickCount + attractor.getId()) % 8 == 0) {
            Predicate<Entity> notOnTeam = (animal) -> !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Animal> list = attractor.level().getEntitiesOfClass(Animal.class, attractor.getBoundingBox().inflate(16, 8, 16), EntitySelector.NO_SPECTATORS.and(notOnTeam));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            for (int i = 0; i < Math.min(max, list.size()); i++) {
                Animal e = list.get(i);
                e.setTarget(null);
                e.setLastHurtByMob(null);
                e.getNavigation().moveTo(attractor, 1.1D);
            }

        }
    }

    /** 臭名昭著（INFAMY_CURSE）：每 400 tick 让最近的怪物以自己为目标（自找麻烦）。 */
    public static void aggroRandomMonsters(LivingEntity attractor) {
        if ((attractor.tickCount + attractor.getId()) % 400 == 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !hasSameOwnerAs((LivingEntity) animal, attractor) && animal.distanceTo(attractor) > 3 + attractor.getBbWidth() * 1.6F;
            List<Mob> list = attractor.level().getEntitiesOfClass(Mob.class, attractor.getBoundingBox().inflate(20, 8, 20), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(attractor::distanceToSqr));
            if (!list.isEmpty()) {
                list.get(0).setTarget(attractor);
            }
        }
    }

    /** 威吓（INTIMIDATION）：按等级决定触发间隔与范围，威吓粒子 + 音效后吓跑范围内怪物（受击立即触发，冷却 70×等级）。 */
    public static void scareRandomMonsters(LivingEntity scary, int level) {
        boolean interval = (scary.tickCount + scary.getId()) % Math.max(140, 600 - level * 200) == 0;
        if (interval || scary.hurtTime == 4 || getIntimidationCooldown(scary) > 0) {
            Predicate<Entity> notOnTeamAndMonster = (animal) -> animal instanceof Monster && !hasSameOwnerAs((LivingEntity) animal, scary) && animal.distanceTo(scary) > 3 + scary.getBbWidth() * 1.6F;
            List<PathfinderMob> list = scary.level().getEntitiesOfClass(PathfinderMob.class, scary.getBoundingBox().inflate(10 * level, 8 * level, 10 * level), EntitySelector.NO_SPECTATORS.and(notOnTeamAndMonster));
            list.sort(Comparator.comparingDouble(scary::distanceToSqr));
            if (!list.isEmpty()) {
                if (getIntimidationCooldown(scary) > 0 && !interval) {
                    setIntimidationCooldown(scary, getIntimidationCooldown(scary) - 1);
                } else {
                    Vec3 rots = list.get(0).getEyePosition().subtract(scary.getEyePosition()).normalize();
                    float f = Mth.sqrt((float) (rots.x * rots.x + rots.z * rots.z));
                    double yRot = Math.atan2(-rots.z, -rots.x) * (double) (180F / (float) Math.PI) + 90F;
                    double xRot = Math.atan2(-rots.y, f) * (double) (180F / (float) Math.PI);
                    scary.level().addParticle(PHParticleRegistry.INTIMIDATION, scary.getX(), scary.getY(), scary.getZ(), scary.getId(), xRot, yRot);
                    setIntimidationCooldown(scary, 70 * level);
                    if (scary instanceof Mob) {
                        ((Mob) scary).playAmbientSound();
                    }
                }
                for (PathfinderMob monster : list) {
                    Vec3 vec = LandRandomPos.getPosAway(monster, 11 * level, 7, scary.position());
                    if (vec != null) {
                        monster.getNavigation().moveTo(vec.x, vec.y, vec.z, 1.5D);
                    }
                }
            }
        }
    }

    /** 矿脉嗅探（ORE_SCENTING）世界交互：嗅探期低头摆动 + SNIFF 粒子，tick==30 时扫描 {@code PHTagRegistry.ORES} 标签方块并生成高亮实体。 */
    public static void detectRandomOres(LivingEntity attractor, int interval, int range, int effectLength, int maxOres) {
        int tick = (attractor.tickCount + attractor.getId()) % interval;
        if (tick <= 30) {
            attractor.xRotO = attractor.getXRot();
            attractor.setXRot((float) Math.sin(tick * 0.6F) * 30F);
            Vec3 look = attractor.getEyePosition().add(attractor.getViewVector(1.0F).scale(attractor.getBbWidth()));
            for (int i = 0; i < 3; i++) {
                double x = attractor.getRandomX(2.0F);
                double y = attractor.position().y;
                double z = attractor.getRandomZ(2.0F);
                attractor.level().addParticle(PHParticleRegistry.SNIFF, x, y, z, look.x, look.y, look.z);
            }
        }
        if (tick == 30) {
            List<BlockPos> ores = new ArrayList<>();
            BlockPos blockpos = attractor.blockPosition();
            int half = range / 2;
            for (int i = 0; i <= half && i >= -half; i = (i <= 0 ? 1 : 0) - i) {
                for (int j = 0; j <= range && j >= -range; j = (j <= 0 ? 1 : 0) - j) {
                    for (int k = 0; k <= range && k >= -range; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos offset = blockpos.offset(j, i, k);
                        BlockState state = attractor.level().getBlockState(offset);
                        if (state.is(PHTagRegistry.ORES)) {
                            if (ores.size() < maxOres) {
                                ores.add(offset);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            for (BlockPos ore : ores) {
                HighlightedBlockEntity highlight = PHEntityRegistry.HIGHLIGHTED_BLOCK.create(attractor.level());
                highlight.setPos(Vec3.atBottomCenterOf(ore));
                highlight.setLifespan(effectLength);
                highlight.setXRot(0);
                highlight.setYRot(0);
                attractor.level().addFreshEntity(highlight);
            }
        }
    }

    /** 枯萎诅咒（BLIGHT_CURSE）：每 200 tick 毁掉周围少量花草 / 作物，并把草方块、耕地退化成泥土 / 砂土。 */
    public static void destroyRandomPlants(LivingEntity living) {
        if ((living.tickCount + living.getId()) % 200 == 0) {
            int range = 2;
            List<BlockPos> plants = new ArrayList<>();
            List<BlockPos> grasses = new ArrayList<>();
            BlockPos blockpos = living.blockPosition();
            int half = range / 2;
            RandomSource r = living.getRandom();
            int maxPlants = 2 + r.nextInt(2);
            for (int i = 0; i <= half && i >= -half; i = (i <= 0 ? 1 : 0) - i) {
                for (int j = 0; j <= range && j >= -range; j = (j <= 0 ? 1 : 0) - j) {
                    for (int k = 0; k <= range && k >= -range; k = (k <= 0 ? 1 : 0) - k) {
                        BlockPos offset = blockpos.offset(j, i, k);
                        BlockState state = living.level().getBlockState(offset);
                        if (!state.isAir() && r.nextInt(4) == 0) {
                            if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.CROPS)) {
                                plants.add(offset);
                            } else if (state.is(BlockTags.DIRT) && !state.is(Blocks.DIRT) && !state.is(Blocks.COARSE_DIRT) || state.is(Blocks.FARMLAND)) {
                                grasses.add(offset);
                            }
                        }
                    }
                }
            }
            for (BlockPos plant : plants) {
                living.level().setBlockAndUpdate(plant, Blocks.AIR.defaultBlockState());
                for (int i = 0; i < 1 + r.nextInt(2); i++) {
                    living.level().addParticle(PHParticleRegistry.BLIGHT, plant.getX() + r.nextFloat(), plant.getY() + r.nextFloat(), plant.getZ() + r.nextFloat(), 0, 0.08F, 0);
                }
            }
            for (BlockPos dirt : grasses) {
                living.level().setBlockAndUpdate(dirt, r.nextBoolean() ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.DIRT.defaultBlockState());
                for (int i = 0; i < 1 + r.nextInt(2); i++) {
                    living.level().addParticle(PHParticleRegistry.BLIGHT, dirt.getX() + r.nextFloat(), dirt.getY() + 1, dirt.getZ() + r.nextFloat(), 0, 0.08F, 0);
                }
            }
        }
    }

    /** 治疗光环的可治疗对象：4 格内同主人且未满血的单位。 */
    public static List<LivingEntity> getAuraHealables(LivingEntity pet) {
        Predicate<Entity> hurtAndOnTeam = (animal) -> hasSameOwnerAs((LivingEntity) animal, pet) && animal.distanceTo(pet) < 4 && ((LivingEntity) animal).getHealth() < ((LivingEntity) animal).getMaxHealth();
        return pet.level().getEntitiesOfClass(LivingEntity.class, pet.getBoundingBox().inflate(4, 4, 4), EntitySelector.NO_SPECTATORS.and(hurtAndOnTeam));
    }

    /** 16×4×16 范围内处于待机（aura time==0）的治疗光环宠物，受击事件据此触发即时充能。 */
    public static List<LivingEntity> getNearbyHealers(LivingEntity hurtOwner) {
        Predicate<Entity> healer = (animal) -> hasSameOwnerAs((LivingEntity) animal, hurtOwner) && hasEnchant((LivingEntity) animal, PHEnchantmentRegistry.HEALING_AURA) && getHealingAuraTime((LivingEntity) animal) == 0;
        return hurtOwner.level().getEntitiesOfClass(LivingEntity.class, hurtOwner.getBoundingBox().inflate(16, 4, 16), EntitySelector.NO_SPECTATORS.and(healer));
    }

    // ===== 以下方法自 1.21 移植（insight / night_vision / sonic_boom / share 附魔依赖） =====

    /** Insight（洞察）：黑暗中让周围敌对生物发光，范围随附魔等级提升 */
    public static void applyGlowingEffect(LivingEntity livingEntity, int enchantLevel) {
        int range = enchantLevel * 15;
        livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(livingEntity.getX() - range, livingEntity.getY() - range,
                        livingEntity.getZ() - range, livingEntity.getX() + range, livingEntity.getY() + range, livingEntity.getZ() + range))
                .stream().filter(i -> i instanceof Enemy).forEach((entity) -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0)));
    }

    /** 范围内的存活生物（不含自身与玩家），share / sonic_boom 用 */
    public static List<LivingEntity> getNearbyMobs(LivingEntity entity, double range) {
        AABB area = new AABB(
                entity.getX() - range, entity.getY() - range, entity.getZ() - range,
                entity.getX() + range, entity.getY() + range, entity.getZ() + range
        );
        return entity.level().getEntitiesOfClass(LivingEntity.class, area, e ->
                e != entity && !(e instanceof Player)
        );
    }

    /** Sonic Boom（音波轰击）：目标周围敌人密集时对群体释放，否则对主目标释放 */
    public static void performSonicBook(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        Set<LivingEntity> livings = getNearbyMobs(maid, 5.0).stream().filter(i -> i instanceof Enemy).collect(Collectors.toSet());
        if (livings.size() > 3) {
            for (LivingEntity enemy : livings) {
                sonicBoomAttack(maid, enemy, serverLevel);
            }
        } else {
            sonicBoomAttack(maid, monster, serverLevel);
        }
    }

    /** 监守者音波的完整演出：粒子直线 + 音效 + 10 点 sonic_boom 伤害与击退（1.20.1 无 EntityAttachment，声波起点固定取 position + (0,1.3,0)）。 */
    public static void sonicBoomAttack(LivingEntity maid, LivingEntity monster, ServerLevel serverLevel) {
        // 1.20.1 没有 EntityAttachment：监守者本体代码用 position + (0, 1.3, 0) 作为声波起点
        Vec3 vec3 = maid.position().add(0.0, 1.3F, 0.0);
        Vec3 vec32 = monster.getEyePosition().subtract(vec3);
        Vec3 vec33 = vec32.normalize();
        int i = Mth.floor(vec32.length()) + 7;

        for (int j = 1; j < i; j++) {
            Vec3 vec34 = vec3.add(vec33.scale(j));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, vec34.x, vec34.y, vec34.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        maid.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
        if (monster.hurt(serverLevel.damageSources().sonicBoom(maid), 10.0F)) {
            double d = 0.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double e = 2.5 * (1.0 - monster.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            monster.push(vec33.x() * e, vec33.y() * d, vec33.z() * e);
        }
    }

    /** 虚空云下落时的坠落距离同步值（{@code SyncedFallDistance}），客户端渲染用。 */
    public static float getFallDistance(LivingEntity enchanted) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        return tag.getFloat(FALL_DISTANCE_SYNC);
    }

    public static void setFallDistance(LivingEntity enchanted, float dist) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        tag.putFloat(FALL_DISTANCE_SYNC, dist);
        sync(enchanted, tag);

    }


    /** 魅力（CHARISMA）折扣加成：25 格内自己宠物的 10×等级之和，上限 50（村民交易降价用）。 */
    public static int getCharismaBonusForOwner(Player player) {
        Predicate<Entity> pet = (animal) -> isTamed(animal) && isPetOf(player, animal);
        List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25, 8, 25), EntitySelector.NO_SPECTATORS.and(pet));
        int charismas = 0;
        for (LivingEntity entity : list) {
            charismas += 10 * getEnchantLevel(entity, PHEnchantmentRegistry.CHARISMA);
        }
        return Math.min(charismas, 50);
    }

    /** 拴魂传送资格：带 TETHERED_TELEPORT 且处于跟随态（指令 ==2，或原版宠物未坐下且 10 格内）。 */
    public static boolean isValidTeleporter(LivingEntity owner, Mob animal) {
        if (hasEnchant(animal, PHEnchantmentRegistry.TETHERED_TELEPORT)) {
            if (animal instanceof IComandableMob commandableMob) {
                return commandableMob.getCommand() == 2;
            } else if (animal instanceof TamableAnimal tame) {
                return !tame.isOrderedToSit() && animal.distanceTo(owner) < 10;
            }
        }
        return false;
    }

    /** 经验转移（XP_TRANSFER）：吸附 3 格内经验球并直接把经验给主人（磁吸半径 8 格）。 */
    public static void xpTransfer(LivingEntity living) {
        for (ExperienceOrb experienceorb : living.level().getEntitiesOfClass(ExperienceOrb.class, living.getBoundingBox().inflate(3D))) {

            Vec3 vec3 = new Vec3(living.getX() - experienceorb.getX(), living.getY() + (double) living.getEyeHeight() / 2.0D - experienceorb.getY(), living.getZ() - experienceorb.getZ());
            double d0 = vec3.lengthSqr();
            if (d0 < 2.0D) {
                Entity owner = TameableUtils.getOwnerOf(living);
                if (owner instanceof Player player) {
                    player.giveExperiencePoints(experienceorb.value);
                }
                experienceorb.discard();
//            float h = living.getHealth() + experienceorb.value;
//            living.setHealth(h);
//            if (h - living.getMaxHealth() > 0) {
//                experienceorb.value = (int) Math.floor(h - living.getMaxHealth());
//                break;
//            } else {
//                experienceorb.discard();
//            }
            }
            if (d0 < 64.0D) {
                double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
                experienceorb.setDeltaMovement(experienceorb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
            }
        }
    }


    /** 复苏（REJUVENATION）：吸附经验球给自己回血（溢出部分留在球里）。 */
    public static void absorbExpOrbs(LivingEntity living) {
        if (living.getHealth() < living.getMaxHealth() && !living.level().isClientSide()) {
            for (ExperienceOrb experienceorb : living.level().getEntitiesOfClass(ExperienceOrb.class, living.getBoundingBox().inflate(3D))) {
                if (living.getHealth() >= living.getMaxHealth()) {
                    break;
                }
                Vec3 vec3 = new Vec3(living.getX() - experienceorb.getX(), living.getY() + (double) living.getEyeHeight() / 2.0D - experienceorb.getY(), living.getZ() - experienceorb.getZ());
                double d0 = vec3.lengthSqr();
                if (d0 < 2.0D) {
                    float h = living.getHealth() + experienceorb.value;
                    living.setHealth(h);
                    if (h - living.getMaxHealth() > 0) {
                        experienceorb.value = (int) Math.floor(h - living.getMaxHealth());
                        break;
                    } else {
                        experienceorb.discard();
                    }
                }
                if (d0 < 64.0D) {
                    double d1 = 1.0D - Math.sqrt(d0) / 8.0D;
                    experienceorb.setDeltaMovement(experienceorb.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.5D)));
                }
            }
        }
    }
}

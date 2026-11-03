package com.github.yzqdev.pethome.util;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.datagen.PHEnchantments;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * 宠物附魔数据的读写与属性同步（package-private）：附魔缓存查询、列表写入、附魔变更后的属性修饰符重算。
 *
 * <p>缓存语义（拆分后保持不变）：{@link #getEnchantLevel} / {@link #getEnchants} 走 {@link
 * PHAttachments#ENCHANT_CACHE}（{@link PetEnchantCache}）——只读当前附件实例并按<b>引用比对</b>，附件没换实例就不重扫 NBT；
 * {@link #getEnchants} 在实体未挂附件或无附魔键时返回 {@code null}，返回的 Map 不可修改。写入一定经 {@link PetDataSync#sync}
 * 换附件实例，缓存随之失效。
 */
final class PetEnchantmentUtils {

    private PetEnchantmentUtils() {
    }

    /** 清空宠物全部附魔（走 {@link #setEnchantmentTag}，会顺带把项圈置为已戴上并触发属性重算）。 */
    static void clearEnchants(LivingEntity entity) {
        setEnchantmentTag(entity, new ListTag());
    }

    /** 整表覆写附魔列表（addEnchant/clearEnchants 的共同出口）。副作用：置位项圈、写 20 tick 换项圈防抖、随后按前后差量重算属性。 */
    private static void setEnchantmentTag(LivingEntity enchanted, ListTag enchants) {
        CompoundTag tag = CitadelEntityData.getOrCreateCitadelTag(enchanted);
        Map<Identifier, Integer> prevEnchants = getEnchants(enchanted);
        tag.put(PetDataKeys.ENCHANTMENT_TAG, enchants);
        tag.putInt(PetDataKeys.COLLAR_SWAP_COOLDOWN, 20);
        tag.putBoolean(PetDataKeys.COLLAR_TAG, true);
        PetDataSync.sync(enchanted, tag);
        onUpdateEnchants(prevEnchants, enchanted);
    }

    /**
     * 实体是否为水生生物类别（WATER_CREATURE / UNDERGROUND_WATER_CREATURE / WATER_AMBIENT），onUpdateEnchants
     * 用它决定两栖附魔的陆地加速。
     */
    private static boolean isWaterCreature(LivingEntity enchanted) {
        return enchanted.getType().getCategory() == MobCategory.WATER_CREATURE
                || enchanted.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE
                || enchanted.getType().getCategory() == MobCategory.WATER_AMBIENT;
    }

    /**
     * 附魔变化后的属性同步：按当前等级重挂 health_boost(+10/级)/speedster/tough(+3 护甲与击退抗性)
     * 永久修饰符，两栖水生生物额外加陆地速度；immaturity_curse 新增或刚移除时把实体设为幼年并刷新碰撞箱。
     */
    private static void onUpdateEnchants(
            @Nullable Map<Identifier, Integer> prevEnchants, LivingEntity enchanted) {
        int healthExtra = getEnchantLevel(enchanted, PHEnchantments.HEALTH_BOOST);
        int speedExtra = getEnchantLevel(enchanted, PHEnchantments.SPEEDSTER);
        int toughExtra = getEnchantLevel(enchanted, PHEnchantments.TOUGH);
        boolean amphib = hasEnchant(enchanted, PHEnchantments.AMPHIBIOUS)
                && !enchanted.isInWater()
                && isWaterCreature(enchanted);
        AttributeInstance health = enchanted.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speed = enchanted.getAttribute(Attributes.MOVEMENT_SPEED);
        var armor = enchanted.getAttribute(Attributes.ARMOR);
        var resistance = enchanted.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (hasEnchant(enchanted, PHEnchantments.IMMATURITY_CURSE)
                || prevEnchants != null
                        && prevEnchants.containsKey(PHEnchantments.IMMATURITY_CURSE.registry())) {
            // 切换 Pose 以触发维度同步，让客户端立即看到幼年形态
            enchanted.setPose(Pose.FALL_FLYING);
            AgeableMob ageable = (AgeableMob) enchanted;
            ageable.setBaby(true); // 直接设为幼年
            enchanted.refreshDimensions();
        }
        if (armor != null && resistance != null) {
            AttributeModifier armorBoost = new AttributeModifier(
                    PetDataKeys.ARMOR_BOOST_UUID, toughExtra * 3, AttributeModifier.Operation.ADD_VALUE);
            AttributeModifier resBoost = new AttributeModifier(
                    PetDataKeys.RESISTANCE_BOOST_UUID,
                    toughExtra * 3,
                    AttributeModifier.Operation.ADD_VALUE);
            if (toughExtra > 0) {
                if (armor.hasModifier(armorBoost.id())) {
                    armor.removeModifier(armorBoost);
                    armor.addPermanentModifier(armorBoost);
                } else {
                    armor.addPermanentModifier(armorBoost);
                }
                if (resistance.hasModifier(resBoost.id())) {
                    resistance.removeModifier(resBoost);
                    resistance.addPermanentModifier(resBoost);
                } else {
                    resistance.addPermanentModifier(resBoost);
                }
            } else {
                armor.removeModifier(PetDataKeys.ARMOR_BOOST_UUID);
                resistance.removeModifier(PetDataKeys.RESISTANCE_BOOST_UUID);
            }
        }
        if (health != null) {
            AttributeModifier healthBoostPetUpgrade = new AttributeModifier(
                    PetDataKeys.HEALTH_BOOST_UUID,
                    healthExtra * 10,
                    AttributeModifier.Operation.ADD_VALUE);

            if (healthExtra > 0) {
                if (health.hasModifier(healthBoostPetUpgrade.id())) {
                    health.removeModifier(healthBoostPetUpgrade);
                    health.addPermanentModifier(healthBoostPetUpgrade);
                } else {
                    health.addPermanentModifier(healthBoostPetUpgrade);
                }
            } else {

                health.removeModifier(healthBoostPetUpgrade);
            }
        }
        if (speed != null) {
            AttributeModifier speedsterPetUpgrade = new AttributeModifier(
                    PetDataKeys.SPEED_BOOST_UUID,
                    speedExtra * 0.075F,
                    AttributeModifier.Operation.ADD_VALUE);

            if (speedExtra > 0) {
                if (speed.hasModifier(speedsterPetUpgrade.id())) {
                    speed.removeModifier(speedsterPetUpgrade);
                    speed.addPermanentModifier(speedsterPetUpgrade);
                } else {
                    speed.addPermanentModifier(speedsterPetUpgrade);
                }
            } else {
                speed.removeModifier(speedsterPetUpgrade);
            }
            AttributeModifier speed_aqua = new AttributeModifier(
                    PetDataKeys.SPEED_BOOST_AQUATIC_LAND_UUID,
                    0.13F,
                    AttributeModifier.Operation.ADD_VALUE);

            if (amphib) {
                if (speed.hasModifier(speed_aqua.id())) {
                    speed.removeModifier(speed_aqua);
                    speed.addPermanentModifier(speed_aqua);
                } else {
                    speed.addPermanentModifier(speed_aqua);
                }
            } else {
                speed.removeModifier(speed_aqua);
            }
        }
    }

    /** {@link #getEnchantLevel} 的布尔形态。 */
    static boolean hasEnchant(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        return getEnchantLevel(entity, enchantment) > 0;
    }

    /**
     * 查宠物单个附魔等级（mixin/渲染/tick 热路径）。
     * 走 {@link PetEnchantCache}：只读当前附件实例并按引用比对，附件没换实例就不重扫 NBT；
     * 被配置禁用的附魔（{@code enchantments.<name>_enabled=false}）一律返回 0——本侧保留了
     * NF 侧没有的附魔启用开关，因此缓存命中后仍要做一次配置检查。
     */
    static int getEnchantLevel(LivingEntity entity, ResourceKey<Enchantment> enchantment) {
        CompoundTag live = CitadelEntityData.getExistingCitadelTagOrNull(entity);
        // Fabric attachment 无 getOrCreate 语义：未挂缓存时挂一个新实例（对齐 NF attachment getData 的自动初始化）
        PetEnchantCache cache = entity.getAttached(PHAttachments.ENCHANT_CACHE);
        if (cache == null) {
            cache = new PetEnchantCache();
            entity.setAttached(PHAttachments.ENCHANT_CACHE, cache);
        }
        Integer level = cache.rebuildIfStale(live).get(enchantment.identifier());
        if (level == null || !PetHomeConfig.isEnchantEnabled(enchantment)) {
            return 0;
        }
        return level;
    }

    /**
     * 组装宠物附魔列表的展示行（标题 + 每条「附魔名 等级」，诅咒显示红色）。
     * 语言键为 {@code enchantment.<namespace>.<path>} + {@code enchantment.level.<n>}，
     * Jade tooltip 与宠物信息面板共用。
     */
    static List<Component> getEnchantDescriptions(LivingEntity entity) {
        List<Component> list = new ArrayList<>();
        list.add(Component.literal("   ")
                .append(Component.translatable("message.pet_home.enchantments")
                        .withStyle(ChatFormatting.GOLD)));
        Map<Identifier, Integer> map = getEnchants(entity);
        if (map != null) {
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {

                boolean isCurse = entry.getKey().getPath().contains("curse");
                list.add(Component.translatable(
                                "enchantment." + entry.getKey().getNamespace() + "." + entry.getKey().getPath())
                        .append(Component.literal(" "))
                        .append(Component.translatable("enchantment.level." + entry.getValue()))
                        .withStyle(isCurse ? ChatFormatting.RED : ChatFormatting.AQUA));
            }
        }
        return list;
    }

    /**
     * 取宠物全部附魔（{@code id → level}）；实体未挂 citadel 附件或无附魔键时返回 {@code null}（调用方需判空）。
     * 结果来自 {@link PetEnchantCache}（引用比对缓存），不得修改返回的 Map。
     */
    @Nullable
    static Map<Identifier, Integer> getEnchants(LivingEntity entity) {
        CompoundTag live = CitadelEntityData.getExistingCitadelTagOrNull(entity);
        if (live == null || !live.contains(PetDataKeys.ENCHANTMENT_TAG)) {
            return null;
        }
        // 附魔 tooltip（Jade 每帧触发）同样走实体缓存，避免每次整 tag 拷贝+重建映射
        PetEnchantCache cache = entity.getAttached(PHAttachments.ENCHANT_CACHE);
        if (cache == null) {
            cache = new PetEnchantCache();
            entity.setAttached(PHAttachments.ENCHANT_CACHE, cache);
        }
        return cache.rebuildIfStale(live);
    }

    /** 构造附魔列表的单个条目 tag：{@code id} 存 {@link Identifier} 字符串、{@code lvl} 存 short（三侧同格式）。 */
    static CompoundTag storeEnchantment(@Nullable Identifier pId, int pLevel) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", String.valueOf((Object) pId));
        compoundtag.putShort("lvl", (short) pLevel);
        return compoundtag;
    }

    /** 把 {@link ItemEnchantments}（附魔书内容）整表写入宠物附魔列表——覆盖旧列表而非追加（对齐 DI 原版附魔书行为）。 */
    static void addEnchant(LivingEntity entity, ItemEnchantments itemEnchantments) {
        ListTag listTag = new ListTag();

        for (var entry : itemEnchantments.entrySet()) {
            var en = entry.getKey().unwrapKey().orElseThrow().identifier();
            var compound = storeEnchantment(en, entry.getIntValue());
            listTag.add(compound);
        }
        setEnchantmentTag(entity, listTag);
    }
}

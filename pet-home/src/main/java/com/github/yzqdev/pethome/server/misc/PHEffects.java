package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;


/**
 * 药水效果注册：目前只有 {@code drunk}（醉酒，{@link DrunkEffect}，宠物通过
 * {@code server/event} 的附魔/事件管线吃到，如嗅探或进食相关逻辑），Fabric 原生注册。
 */
public class PHEffects {

    public static final Holder.Reference<MobEffect> DRUNK = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
            ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "drunk")),
            new DrunkEffect(MobEffectCategory.HARMFUL, 6684723, false));

    /** 空实现：仅用于触发本类静态初始化（注册发生在这时）。 */
    public static void init() {
    }
}

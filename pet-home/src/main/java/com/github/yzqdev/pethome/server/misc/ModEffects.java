package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 药水效果注册：Fabric 原生 {@code Registry.register}。
 * 字段保留 {@link Holder.Reference}（原版 addEffect/hasEffect 接收 Holder&lt;MobEffect&gt;）。
 */
public class ModEffects {

    public static final Holder.Reference<MobEffect> DRUNK = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
            ResourceKey.create(BuiltInRegistries.MOB_EFFECT.key(), ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "drunk")),
            new DrunkEffect(MobEffectCategory.HARMFUL, 6684723, false));

    public static void init() {
    }
}

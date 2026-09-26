package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 模组自定义状态效果注册表。 */
public class ModEffects {


    /** 混乱（Chaos / Violent 附魔依赖） */
    public static final MobEffect DRUNK = Registry.register(BuiltInRegistries.MOB_EFFECT,
            new ResourceLocation(PetHomeMod.MODID, "drunk"),
            new DrunkEffect(MobEffectCategory.HARMFUL, 6684723));

    public static void init() {
    }
}

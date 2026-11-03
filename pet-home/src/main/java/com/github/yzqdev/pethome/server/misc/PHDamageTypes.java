package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/** 模组自定义伤害类型（数据包 {@code data/pet_home/damage_type/} 定义，代码侧仅持 Key）。 */
public class PHDamageTypes {

    /** 生命虹吸的 0 伤害占位源：把"转移伤害"打到宠物身上以取消原伤害，同时让引擎知晓宠物本 tick 已受伤。 */
    public static final ResourceKey<DamageType> SIPHON = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PetHomeMod.MODID, "siphon"));

    /** 构造生命虹吸占位伤害源。 */
    public static DamageSource causeSiphonDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(SIPHON));

    }
}

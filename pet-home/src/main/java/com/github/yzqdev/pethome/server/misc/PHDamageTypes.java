package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/**
 * 伤害类型注册（26.1 伤害类型走数据包 {@code data/pet_home/damage_type/}，
 * 这里只持 ResourceKey）：{@code siphon} 供"虹吸"附魔吸血伤害使用，
 * 独立伤害类型可被盾牌格挡判定/死亡消息与原版伤害区分。
 */
public class PHDamageTypes {

    public static final ResourceKey<DamageType> SIPHON = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "siphon"));

    /** 用 {@code siphon} 类型构造伤害源（按持有者注册表解析）。 */
    public static DamageSource causeSiphonDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).get(SIPHON).orElseThrow());

    }
}

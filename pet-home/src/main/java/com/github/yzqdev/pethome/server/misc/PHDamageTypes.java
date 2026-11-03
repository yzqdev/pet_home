package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/** 模组自定义伤害类型注册与构造：目前仅有虹吸（siphon，{@code pet_home:siphon}）——宠物附魔吸取生命时使用，伤害归因不落到玩家。 */
public class PHDamageTypes {

  public static final ResourceKey<DamageType> SIPHON =
      ResourceKey.create(
          Registries.DAMAGE_TYPE,
          ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "siphon"));

  /** 构造一次虹吸伤害源（伤害类型注册表查找，需传入当前世界的 {@code RegistryAccess}）。 */
  public static DamageSource causeSiphonDamage(RegistryAccess registryAccess) {
    return new DamageSource(
        registryAccess.registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(SIPHON));
  }
}

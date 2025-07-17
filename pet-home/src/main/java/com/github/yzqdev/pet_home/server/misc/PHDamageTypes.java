package com.github.yzqdev.pet_home.server.misc;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class PHDamageTypes {

    public static final ResourceKey<DamageType> SIPHON = ResourceKey.create(Registries.DAMAGE_TYPE,  ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "siphon"));

    public static DamageSource causeSiphonDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(SIPHON));

    }
}

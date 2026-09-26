package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class PHDamageTypes {

    public static final ResourceKey<DamageType> SIPHON = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(PetHomeMod.MODID, "siphon"));

    public static DamageSource causeSiphonDamage(RegistryAccess registryAccess) {
        return new DamageSource(registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).get(SIPHON).orElseThrow());

    }
}

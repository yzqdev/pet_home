package com.github.yzqdev.pet_home.server.misc;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PHParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, PetHomeMod.MODID);
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType> DEFLECTION_SHIELD = DEF_REG.register("deflection_shield", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  MAGNET = DEF_REG.register("magnet", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  ZZZ = DEF_REG.register("zzz", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  GIANT_POP = DEF_REG.register("giant_pop", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  SIMPLE_BUBBLE = DEF_REG.register("simple_bubble", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  VAMPIRE = DEF_REG.register("vampire", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  SNIFF = DEF_REG.register("sniff", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  PSYCHIC_WALL = DEF_REG.register("psychic_wall", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  INTIMIDATION = DEF_REG.register("intimidation", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  BLIGHT = DEF_REG.register("blight", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType>  LANTERN_BUGS = DEF_REG.register("lantern_bugs", () -> new SimpleParticleType(false));    public static final DeferredHolder<ParticleType<?>,SimpleParticleType> QUESTION_MARK_PARTICLE_TYPE = DEF_REG.register("question_mark_particle", () -> new SimpleParticleType(false));

}

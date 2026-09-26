package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.particles.ParticleType;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class DIParticleRegistry {

    public static final SimpleParticleType DEFLECTION_SHIELD = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "deflection_shield"), FabricParticleTypes.simple());
    public static final SimpleParticleType MAGNET = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "magnet"), FabricParticleTypes.simple());
    public static final SimpleParticleType ZZZ = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "zzz"), FabricParticleTypes.simple());
    public static final SimpleParticleType GIANT_POP = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "giant_pop"), FabricParticleTypes.simple());
    public static final SimpleParticleType SIMPLE_BUBBLE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "simple_bubble"), FabricParticleTypes.simple());
    public static final SimpleParticleType VAMPIRE = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "vampire"), FabricParticleTypes.simple());
    public static final SimpleParticleType SNIFF = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "sniff"), FabricParticleTypes.simple());
    public static final SimpleParticleType PSYCHIC_WALL = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "psychic_wall"), FabricParticleTypes.simple());
    public static final SimpleParticleType INTIMIDATION = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "intimidation"), FabricParticleTypes.simple());
    public static final SimpleParticleType BLIGHT = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "blight"), FabricParticleTypes.simple());
    /** 问号粒子（混乱之脑：混乱的怪物头顶显示） */
    public static final SimpleParticleType QUESTION_MARK = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "question_mark_particle"), FabricParticleTypes.simple());
    public static final SimpleParticleType LANTERN_BUGS = Registry.register(BuiltInRegistries.PARTICLE_TYPE, new ResourceLocation(PetHomeMod.MODID, "lantern_bugs"), FabricParticleTypes.simple());


    public static void init() {
    }
}

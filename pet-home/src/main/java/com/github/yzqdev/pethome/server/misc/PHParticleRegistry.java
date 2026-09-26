package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/** 粒子类型注册：Fabric 原生 {@code Registry.register}。 */
public class PHParticleRegistry {

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final PHParticleType DEFLECTION_SHIELD = register("deflection_shield");
    public static final PHParticleType MAGNET = register("magnet");
    public static final PHParticleType ZZZ = register("zzz");
    public static final PHParticleType GIANT_POP = register("giant_pop");
    public static final PHParticleType SIMPLE_BUBBLE = register("simple_bubble");
    public static final PHParticleType VAMPIRE = register("vampire");
    public static final PHParticleType SNIFF = register("sniff");
    public static final PHParticleType PSYCHIC_WALL = register("psychic_wall");
    public static final PHParticleType INTIMIDATION = register("intimidation");
    public static final PHParticleType BLIGHT = register("blight");
    public static final PHParticleType LANTERN_BUGS = register("lantern_bugs");
    public static final PHParticleType QUESTION_MARK_PARTICLE_TYPE = register("question_mark_particle");

    private static PHParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id(name), new PHParticleType(false));
    }

    public static void init() {
    }
}

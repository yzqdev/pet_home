package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class PHParticleRegistry {

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final SimpleParticleType DEFLECTION_SHIELD = register("deflection_shield");
    public static final SimpleParticleType MAGNET = register("magnet");
    public static final SimpleParticleType ZZZ = register("zzz");
    public static final SimpleParticleType GIANT_POP = register("giant_pop");
    public static final SimpleParticleType SIMPLE_BUBBLE = register("simple_bubble");
    public static final SimpleParticleType VAMPIRE = register("vampire");
    public static final SimpleParticleType SNIFF = register("sniff");
    public static final SimpleParticleType PSYCHIC_WALL = register("psychic_wall");
    public static final SimpleParticleType INTIMIDATION = register("intimidation");
    public static final SimpleParticleType BLIGHT = register("blight");
    public static final SimpleParticleType LANTERN_BUGS = register("lantern_bugs");
    public static final SimpleParticleType QUESTION_MARK_PARTICLE_TYPE = register("question_mark_particle");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id(name), new SimpleParticleType(false));
    }

    public static void init() {
    }
}

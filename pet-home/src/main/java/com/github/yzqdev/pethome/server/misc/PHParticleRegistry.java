package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * 粒子类型注册（全部 {@code SimpleParticleType}，注册名即 id）：
 * 覆盖附魔特效（偏转护盾/磁铁/吸血/嗅探/念力墙/威慑/枯萎）、宠物床 ZZZ、
 * 捕网爆开、简易气泡、灯笼萤火虫、宠物困惑问号；客户端渲染类见 {@code client/particle}，
 * 纹理由 datagen 的 {@code PHParticleProvider} 生成。
 */
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

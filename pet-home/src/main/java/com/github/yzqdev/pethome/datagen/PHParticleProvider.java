package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

/**
 * 粒子描述 JSON（{@code assets/pet_home/particles/*.json}）datagen：用 NeoForge 的
 * {@link ParticleDescriptionProvider} 按粒子类型注册贴图序列。与 26.3 侧同清单
 * （lantern_bugs/sniff 用 minecraft:generic_0/1，simple_bubble 用 minecraft:bubble）；
 * {@code deflection_shield} / {@code intimidation} 走 PhantomParticleGroup 自定义渲染，
 * 无贴图描述，不生成。
 */
public class PHParticleProvider extends ParticleDescriptionProvider {

    public PHParticleProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(PHParticleRegistry.BLIGHT.get(), id("blight"));
        this.spriteSet(PHParticleRegistry.GIANT_POP.get(), id("giant_pop"));
        this.spriteSet(
                PHParticleRegistry.LANTERN_BUGS.get(),
                Identifier.withDefaultNamespace("generic_0"),
                Identifier.withDefaultNamespace("generic_1"));
        this.spriteSet(PHParticleRegistry.MAGNET.get(), id("magnet_0"), id("magnet_1"), id("magnet_2"));
        this.spriteSet(
                PHParticleRegistry.PSYCHIC_WALL.get(),
                id("psychic_wall_0"),
                id("psychic_wall_1"),
                id("psychic_wall_2"),
                id("psychic_wall_3"));
        this.spriteSet(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE.get(), id("question_mark_particle"));
        this.spriteSet(PHParticleRegistry.SIMPLE_BUBBLE.get(), Identifier.withDefaultNamespace("bubble"));
        this.spriteSet(
                PHParticleRegistry.SNIFF.get(),
                Identifier.withDefaultNamespace("generic_0"),
                Identifier.withDefaultNamespace("generic_1"));
        this.spriteSet(PHParticleRegistry.VAMPIRE.get(), id("vampire"));
        this.spriteSet(PHParticleRegistry.ZZZ.get(), id("zzz"));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, path);
    }
}

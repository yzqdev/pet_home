package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ParticleDescriptionProvider;

/**
 * 粒子描述 JSON（{@code assets/pet_home/particles/*.json}）datagen：用 Forge 的
 * {@link ParticleDescriptionProvider} 按粒子类型注册贴图序列。lantern_bugs/sniff 用
 * minecraft:generic_0/1，simple_bubble 用 minecraft:bubble。
 */
public class PHParticleProvider extends ParticleDescriptionProvider {

    public PHParticleProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    public String getName() {
        return "Particle Descriptions - " + PetHomeMod.MODID;
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(PHParticleRegistry.BLIGHT.get(), id("blight"));
        this.spriteSet(PHParticleRegistry.GIANT_POP.get(), id("giant_pop"));
        this.spriteSet(
                PHParticleRegistry.LANTERN_BUGS.get(),
                ResourceLocation.withDefaultNamespace("generic_0"),
                ResourceLocation.withDefaultNamespace("generic_1"));
        this.spriteSet(PHParticleRegistry.MAGNET.get(), id("magnet_0"), id("magnet_1"), id("magnet_2"));
        this.spriteSet(
                PHParticleRegistry.PSYCHIC_WALL.get(),
                id("psychic_wall_0"),
                id("psychic_wall_1"),
                id("psychic_wall_2"),
                id("psychic_wall_3"));
        this.spriteSet(PHParticleRegistry.QUESTION_MARK.get(), id("question_mark_particle"));
        this.spriteSet(PHParticleRegistry.SIMPLE_BUBBLE.get(), ResourceLocation.withDefaultNamespace("bubble"));
        this.spriteSet(
                PHParticleRegistry.SNIFF.get(),
                ResourceLocation.withDefaultNamespace("generic_0"),
                ResourceLocation.withDefaultNamespace("generic_1"));
        this.spriteSet(PHParticleRegistry.VAMPIRE.get(), id("vampire"));
        this.spriteSet(PHParticleRegistry.ZZZ.get(), id("zzz"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, path);
    }
}

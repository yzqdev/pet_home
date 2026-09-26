package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;


public class PHParticleProvider extends FabricCodecDataProvider<PHParticleProvider.ParticleTextureList> {

    public record ParticleTextureList(List<Identifier> textures) {
        public static final Codec<ParticleTextureList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.listOf().fieldOf("textures").forGetter(ParticleTextureList::textures)
        ).apply(inst, ParticleTextureList::new));
    }

    public PHParticleProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture, PackOutput.Target.RESOURCE_PACK, "particles", ParticleTextureList.CODEC);
    }

    @Override
    public String getName() {
        return "Particle Descriptions";
    }

    @Override
    protected void configure(BiConsumer<Identifier, ParticleTextureList> provider, HolderLookup.Provider registries) {
        provider.accept(id("blight"), of("pet_home:blight"));
        provider.accept(id("deflection_shield"), of());
        provider.accept(id("giant_pop"), of("pet_home:giant_pop"));
        provider.accept(id("intimidation"), of());
        provider.accept(id("lantern_bugs"), of("minecraft:generic_0", "minecraft:generic_1"));
        provider.accept(id("magnet"), of("pet_home:magnet_0", "pet_home:magnet_1", "pet_home:magnet_2"));
        provider.accept(id("psychic_wall"), of("pet_home:psychic_wall_0", "pet_home:psychic_wall_1", "pet_home:psychic_wall_2", "pet_home:psychic_wall_3"));
        provider.accept(id("question_mark_particle"), of("pet_home:question_mark_particle"));
        provider.accept(id("simple_bubble"), of("minecraft:bubble"));
        provider.accept(id("sniff"), of("minecraft:generic_0", "minecraft:generic_1"));
        provider.accept(id("vampire"), of("pet_home:vampire"));
        provider.accept(id("zzz"), of("pet_home:zzz"));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PetHomeMod.MODID, path);
    }

    private static ParticleTextureList of(String... textures) {
        return new ParticleTextureList(java.util.Arrays.stream(textures).map(Identifier::parse).toList());
    }
}

package com.github.yzqdev.pethome.datagen;

import com.github.yzqdev.pethome.PetHomeMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

/**
 * 粒子描述 JSON（{@code assets/pet_home/particles/*.json}）datagen：为每种贴图粒子声明贴图序列。
 * Fabric 无官方粒子描述 provider，用 {@link FabricCodecDataProvider} 输出。lantern_bugs/sniff 用
 * minecraft:generic_0/1，simple_bubble 用 minecraft:bubble；{@code deflection_shield} /
 * {@code intimidation} 走 CUSTOM 渲染无贴图描述。
 */
public class PHParticleProvider
    extends FabricCodecDataProvider<PHParticleProvider.ParticleTextureList> {

  /** 粒子描述数据：贴图标识符列表及其编解码器（对应原版 particles JSON 格式）。 */
  public record ParticleTextureList(List<ResourceLocation> textures) {
    public static final Codec<ParticleTextureList> CODEC =
        RecordCodecBuilder.create(
            inst ->
                inst.group(
                        ResourceLocation.CODEC
                            .listOf()
                            .fieldOf("textures")
                            .forGetter(ParticleTextureList::textures))
                    .apply(inst, ParticleTextureList::new));
  }

  public PHParticleProvider(FabricDataOutput output) {
    super(output, PackOutput.Target.RESOURCE_PACK, "particles", ParticleTextureList.CODEC);
  }

  @Override
  public String getName() {
    return "Particle Descriptions - " + PetHomeMod.MODID;
  }

  @Override
  protected void configure(BiConsumer<ResourceLocation, ParticleTextureList> provider) {
    provider.accept(id("blight"), of("blight"));
    provider.accept(id("giant_pop"), of("giant_pop"));
    provider.accept(id("lantern_bugs"), of("minecraft:generic_0", "minecraft:generic_1"));
    provider.accept(id("magnet"), of("magnet_0", "magnet_1", "magnet_2"));
    provider.accept(
        id("psychic_wall"),
        of("psychic_wall_0", "psychic_wall_1", "psychic_wall_2", "psychic_wall_3"));
    provider.accept(id("question_mark_particle"), of("question_mark_particle"));
    provider.accept(id("simple_bubble"), of("minecraft:bubble"));
    provider.accept(id("sniff"), of("minecraft:generic_0", "minecraft:generic_1"));
    provider.accept(id("vampire"), of("vampire"));
    provider.accept(id("zzz"), of("zzz"));
  }

  private static ResourceLocation id(String path) {
    return new ResourceLocation(PetHomeMod.MODID, path);
  }

  private static ParticleTextureList of(String... textures) {
    return new ParticleTextureList(
        java.util.Arrays.stream(textures).map(r -> new ResourceLocation(r)).toList());
  }
}

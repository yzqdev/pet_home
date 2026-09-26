package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/** 音效注册：Fabric 原生 {@code Registry.register}（立即注册）。 */
public class PHSoundRegistry {

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, name);
    }

    public static final SoundEvent COLLAR_TAG = register("collar_tag");
    public static final SoundEvent MAGNET_LOOP = register("magnet_loop");
    public static final SoundEvent CHAIN_LIGHTNING = register("chain_lightning");
    public static final SoundEvent GIANT_BUBBLE_INFLATE = register("giant_bubble_inflate");
    public static final SoundEvent GIANT_BUBBLE_POP = register("giant_bubble_pop");
    public static final SoundEvent PET_BED_USE = register("pet_bed_use");
    public static final SoundEvent DRUM = register("drum");
    public static final SoundEvent PSYCHIC_WALL = register("psychic_wall");
    public static final SoundEvent PSYCHIC_WALL_DEFLECT = register("psychic_wall_deflect");
    public static final SoundEvent BLAZING_PROTECTION = register("blazing_protection");

    private static SoundEvent register(String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id(name), SoundEvent.createVariableRangeEvent(id(name)));
    }

    public static void init() {
    }
}

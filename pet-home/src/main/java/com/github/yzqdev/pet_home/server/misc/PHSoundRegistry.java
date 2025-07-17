package com.github.yzqdev.pet_home.server.misc;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PHSoundRegistry {

    public static final DeferredRegister<SoundEvent> DEF_REG = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PetHomeMod.MODID);

    public static final DeferredHolder<SoundEvent,SoundEvent> COLLAR_TAG = createSoundEvent("collar_tag");
    public static final DeferredHolder<SoundEvent,SoundEvent> MAGNET_LOOP = createSoundEvent("magnet_loop");
    public static final DeferredHolder<SoundEvent,SoundEvent> CHAIN_LIGHTNING = createSoundEvent("chain_lightning");
    public static final DeferredHolder<SoundEvent,SoundEvent> GIANT_BUBBLE_INFLATE = createSoundEvent("giant_bubble_inflate");
    public static final DeferredHolder<SoundEvent,SoundEvent> GIANT_BUBBLE_POP = createSoundEvent("giant_bubble_pop");
    public static final DeferredHolder<SoundEvent,SoundEvent> PET_BED_USE = createSoundEvent("pet_bed_use");
    public static final DeferredHolder<SoundEvent,SoundEvent> DRUM = createSoundEvent("drum");
    public static final DeferredHolder<SoundEvent,SoundEvent> PSYCHIC_WALL = createSoundEvent("psychic_wall");
    public static final DeferredHolder<SoundEvent,SoundEvent> PSYCHIC_WALL_DEFLECT = createSoundEvent("psychic_wall_deflect");
    public static final DeferredHolder<SoundEvent,SoundEvent> BLAZING_PROTECTION = createSoundEvent("blazing_protection");

    private static DeferredHolder<SoundEvent,SoundEvent> createSoundEvent(final String soundName) {
        return DEF_REG.register(soundName, () -> SoundEvent.createVariableRangeEvent( ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, soundName)));
    }
}

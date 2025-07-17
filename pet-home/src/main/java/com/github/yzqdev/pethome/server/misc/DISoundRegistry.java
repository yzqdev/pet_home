package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DISoundRegistry {

    public static final DeferredRegister<SoundEvent> DEF_REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PetHomeMod.MODID);

    public static final RegistryObject<SoundEvent> COLLAR_TAG = createSoundEvent("collar_tag");
    public static final RegistryObject<SoundEvent> MAGNET_LOOP = createSoundEvent("magnet_loop");
    public static final RegistryObject<SoundEvent> CHAIN_LIGHTNING = createSoundEvent("chain_lightning");
    public static final RegistryObject<SoundEvent> GIANT_BUBBLE_INFLATE = createSoundEvent("giant_bubble_inflate");
    public static final RegistryObject<SoundEvent> GIANT_BUBBLE_POP = createSoundEvent("giant_bubble_pop");
    public static final RegistryObject<SoundEvent> PET_BED_USE = createSoundEvent("pet_bed_use");
    public static final RegistryObject<SoundEvent> DRUM = createSoundEvent("drum");
    public static final RegistryObject<SoundEvent> PSYCHIC_WALL = createSoundEvent("psychic_wall");
    public static final RegistryObject<SoundEvent> PSYCHIC_WALL_DEFLECT = createSoundEvent("psychic_wall_deflect");
    public static final RegistryObject<SoundEvent> BLAZING_PROTECTION = createSoundEvent("blazing_protection");

    private static RegistryObject<SoundEvent> createSoundEvent(final String soundName) {
        return DEF_REG.register(soundName, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, soundName)));
    }
}

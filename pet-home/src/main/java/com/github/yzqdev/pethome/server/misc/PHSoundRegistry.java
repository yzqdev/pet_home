package com.github.yzqdev.pethome.server.misc;

import com.github.yzqdev.pethome.PetHomeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/** 音效注册：Fabric 原生 {@code Registry.register}（立即注册）；字段名即用途（项圈标签 / 磁力循环音 / 连锁闪电 / 巨型泡泡充放气 / 宠物床使用 / 指令鼓 / 念力墙生成与格挡 / 烈焰保护触发）。 */
public class PHSoundRegistry {


    public static final SoundEvent COLLAR_TAG = createSoundEvent("collar_tag");
    public static final SoundEvent MAGNET_LOOP = createSoundEvent("magnet_loop");
    public static final SoundEvent CHAIN_LIGHTNING = createSoundEvent("chain_lightning");
    public static final SoundEvent GIANT_BUBBLE_INFLATE = createSoundEvent("giant_bubble_inflate");
    public static final SoundEvent GIANT_BUBBLE_POP = createSoundEvent("giant_bubble_pop");
    public static final SoundEvent PET_BED_USE = createSoundEvent("pet_bed_use");
    public static final SoundEvent DRUM = createSoundEvent("drum");
    public static final SoundEvent PSYCHIC_WALL = createSoundEvent("psychic_wall");
    public static final SoundEvent PSYCHIC_WALL_DEFLECT = createSoundEvent("psychic_wall_deflect");
    public static final SoundEvent BLAZING_PROTECTION = createSoundEvent("blazing_protection");

    /** 注册可变音量音效（注册名与音效事件同名）。 */
    private static SoundEvent createSoundEvent(final String soundName) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, new ResourceLocation(PetHomeMod.MODID, soundName), SoundEvent.createVariableRangeEvent(new ResourceLocation(PetHomeMod.MODID, soundName)));
    }

    public static void init() {
    }
}

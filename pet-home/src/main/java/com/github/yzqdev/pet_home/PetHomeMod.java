package com.github.yzqdev.pet_home;

import com.github.yzqdev.pet_home.datagen.loot.PHLootRegistry;
import com.github.yzqdev.pet_home.server.block.PHBlockRegistry;
import com.github.yzqdev.pet_home.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pet_home.server.entity.PHEntityRegistry;
import com.github.yzqdev.pet_home.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pet_home.server.item.PHItemRegistry;

import com.github.yzqdev.pet_home.server.misc.PHCreativeTabRegistry;
import com.github.yzqdev.pet_home.server.misc.PHPOIRegistry;
import com.github.yzqdev.pet_home.server.misc.PHParticleRegistry;
import com.github.yzqdev.pet_home.server.misc.PHSoundRegistry;
import com.github.yzqdev.pet_home.server.misc.ModEffects;
import com.github.yzqdev.pet_home.worldgen.PHVillagePieceRegistry;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;


@Mod(PetHomeMod.MODID)
public class PetHomeMod {

    public static final String MODID = "pet_home";

    public static final Logger LOGGER = LogUtils.getLogger();

    public PetHomeMod(IEventBus modEventBus, ModContainer modContainer) {

        PHVillagePieceRegistry.DEF_REG.register(modEventBus);
        PHBlockRegistry.DEF_REG.register(modEventBus);
        PHEntityRegistry.DEF_REG.register(modEventBus);
        PHPOIRegistry.DEF_REG.register(modEventBus);
        PHVillagerRegistry.DEF_REG.register(modEventBus);
        PHSoundRegistry.DEF_REG.register(modEventBus);
        PHTileEntityRegistry.DEF_REG.register(modEventBus);
        PHItemRegistry.DEF_REG.register(modEventBus);
        PHParticleRegistry.DEF_REG.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);

        PHCreativeTabRegistry.DEF_REG.register(modEventBus);

        PHLootRegistry.DEF_REG.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, PetHomeConfig.SPEC);

    }


}

package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.datagen.loot.PHLootRegistry;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.server.misc.*;
import com.github.yzqdev.pethome.worldgen.PHVillagePieceRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;


@Mod(PetHomeMod.MODID)
public class PetHomeMod {

    public static final String MODID = "pet_home";

    public static final PHModLogger LOGGER = PHModLogger.getInstance();

    public PetHomeMod(IEventBus modEventBus, ModContainer modContainer) {

        PHVillagePieceRegistry.DEF_REG.register(modEventBus);
        PHBlockRegistry.DEF_REG.register(modEventBus);

        PHPOIRegistry.DEF_REG.register(modEventBus);
        PHVillagerRegistry.DEF_REG.register(modEventBus);
        PHSoundRegistry.DEF_REG.register(modEventBus);
        PHDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        PHTileEntityRegistry.DEF_REG.register(modEventBus);
        PHEntityRegistry.DEF_REG.register(modEventBus);
        PHItemRegistry.DEF_REG.register(modEventBus);
        PHParticleRegistry.DEF_REG.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);

        PHCreativeTabRegistry.DEF_REG.register(modEventBus);

        PHLootRegistry.DEF_REG.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, PetHomeConfig.SPEC);

    }


}

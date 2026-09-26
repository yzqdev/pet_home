package com.github.yzqdev.pethome;


import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.platform.ServerRef;
import com.github.yzqdev.pethome.server.PHDataComponents;
import com.github.yzqdev.pethome.server.block.PHBlockRegistry;
import com.github.yzqdev.pethome.server.block.PHTileEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.PHEntityRegistry;
import com.github.yzqdev.pethome.server.entity.PHVillagerRegistry;
import com.github.yzqdev.pethome.server.event.EntityTickHandler;
import com.github.yzqdev.pethome.server.event.PlayerInteractEntityHandler;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import com.github.yzqdev.pethome.datagen.loot.PetHomeLootInjector;
import com.github.yzqdev.pethome.server.misc.ModEffects;
import com.github.yzqdev.pethome.server.misc.PHCreativeTabRegistry;
import com.github.yzqdev.pethome.server.misc.PHPOIRegistry;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import com.github.yzqdev.pethome.server.misc.PHSoundRegistry;
import com.github.yzqdev.pethome.util.PHAttachments;
import com.github.yzqdev.pethome.worldgen.PHVillagePieceRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PetHomeMod implements ModInitializer {
    public static final String MODID = "pet_home";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {

        PetHomeConfig.load();


        PHAttachments.init();
        // 世界级数据 / 网络
        Networking.registerPayloadTypes();
        ServerRef.init();


        PHVillagePieceRegistry.init();
        PHItemRegistry.init();
        PHBlockRegistry.init();

        PHPOIRegistry.init();
        PHVillagerRegistry.init();

        PoiTypes.registerBlockStates(PHPOIRegistry.PET_BED, PHPOIRegistry.PET_BED.value().matchingStates());
        PHActivityRegistry.init();
        PHSoundRegistry.init();
        PHDataComponents.init();
        PHTileEntityRegistry.init();
        PHEntityRegistry.init();

        PHParticleRegistry.init();
        ModEffects.init();

        PHCreativeTabRegistry.init();


        EntityTickHandler.init();
        PlayerInteractEntityHandler.init();
        ServerEvent.init();

       PetHomeLootInjector.init();
    }
}

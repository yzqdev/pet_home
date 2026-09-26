package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.client.ClientModEvents;
import com.github.yzqdev.pethome.client.PetInfoHudOverlay;
import com.github.yzqdev.pethome.client.gui.PetHomeConfigCommand;
import com.github.yzqdev.pethome.client.particle.*;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class PetHomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Networking.initClient();

        ClientModEvents.registerEntityRenderers();
        ClientModEvents.registerLayers();
        ClientModEvents.registerTooltipComponents();
        ClientModEvents.registerItemTooltips();
        setupParticles();

        PetInfoHudOverlay.init();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> PetHomeConfigCommand.register(dispatcher));


        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PetHomeConfig.save());
    }

    private static void setupParticles() {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.DEFLECTION_SHIELD, new ParticleDeflectionShield.Factory());
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.SNIFF, ParticleSniff.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.PSYCHIC_WALL, ParticlePsychicWall.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.INTIMIDATION, new ParticleIntimidation.Factory());
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.BLIGHT, ParticleBlight.Factory::new);
        ParticleFactoryRegistry.getInstance().register(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE, ParticleQuestionMark.Factory::new);
    }
}

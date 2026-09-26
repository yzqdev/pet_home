package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.client.ClientModEvents;
import com.github.yzqdev.pethome.client.PetInfoHudOverlay;
import com.github.yzqdev.pethome.client.gui.PetHomeConfigCommand;
import com.github.yzqdev.pethome.client.particle.*;
import com.github.yzqdev.pethome.network.Networking;
import com.github.yzqdev.pethome.server.misc.DIParticleRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;


public class PetHomeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Networking.initClient();

        ClientModEvents.registerEntityRenderers();
        ClientModEvents.registerItemProperties();
        ClientModEvents.registerLayers();
        ClientModEvents.registerTooltipComponents();
        ClientModEvents.registerItemTooltips();
        setupParticles();

        PetInfoHudOverlay.init();

        // 设置界面入口：ModMenu 的“配置”按钮（见 PetHomeModMenu）+ 客户端命令 /pet_home_config
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> PetHomeConfigCommand.register(dispatcher));

        // 退出游戏时把设置界面里的改动落盘
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PetHomeConfig.save());
    }

    private static void setupParticles() {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(DIParticleRegistry.DEFLECTION_SHIELD, new ParticleDeflectionShield.Factory());
        registry.register(DIParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
        registry.register(DIParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
        registry.register(DIParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
        registry.register(DIParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
        registry.register(DIParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
        registry.register(DIParticleRegistry.SNIFF, ParticleSniff.Factory::new);
        registry.register(DIParticleRegistry.PSYCHIC_WALL, ParticlePsychicWall.Factory::new);
        registry.register(DIParticleRegistry.INTIMIDATION, new ParticleIntimidation.Factory());
        registry.register(DIParticleRegistry.BLIGHT, ParticleBlight.Factory::new);
        registry.register(DIParticleRegistry.LANTERN_BUGS, ParticleLanternBugs.Factory::new);
        registry.register(DIParticleRegistry.QUESTION_MARK, ParticleQuestionMark.Factory::new);
    }
}

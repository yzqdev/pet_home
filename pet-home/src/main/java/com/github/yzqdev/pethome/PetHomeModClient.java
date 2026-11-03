package com.github.yzqdev.pethome;

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


/**
 * 客户端入口（{@link ClientModInitializer}）：注册客户端网络接收器 → 实体渲染器 / 物品属性 /
 * 宠物叠加渲染层 / tooltip → 粒子工厂 → 宠物信息 HUD → {@code /pet_home_config} 客户端命令。
 * 退出游戏时把配置改动落盘。
 */
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

    /** 为 {@link PHParticleRegistry} 里注册的每种粒子类型挂接客户端工厂（服务端只注册类型本身，无渲染）。 */
    private static void setupParticles() {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(PHParticleRegistry.DEFLECTION_SHIELD, new ParticleDeflectionShield.Factory());
        registry.register(PHParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
        registry.register(PHParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
        registry.register(PHParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
        registry.register(PHParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
        registry.register(PHParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
        registry.register(PHParticleRegistry.SNIFF, ParticleSniff.Factory::new);
        registry.register(PHParticleRegistry.PSYCHIC_WALL, ParticlePsychicWall.Factory::new);
        registry.register(PHParticleRegistry.INTIMIDATION, new ParticleIntimidation.Factory());
        registry.register(PHParticleRegistry.BLIGHT, ParticleBlight.Factory::new);
        registry.register(PHParticleRegistry.LANTERN_BUGS, ParticleLanternBugs.Factory::new);
        registry.register(PHParticleRegistry.QUESTION_MARK, ParticleQuestionMark.Factory::new);
    }
}

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
 * 客户端入口（{@code ClientModInitializer}，仅物理客户端加载）。
 *
 * <p>负责：客户端网络接收器、实体渲染器/叠加层/tooltip 注册、粒子工厂、 宠物信息 HUD、{@code /pet_home_config} 客户端命令；客户端退出时保存配置。
 */
public class PetHomeModClient implements ClientModInitializer {

  /**
   * Fabric 客户端入口回调。先接好 {@code Networking.initClient()} 的 S2C 接收器， 再依次注册渲染/tooltip/粒子/HUD/命令；末尾挂
   * {@code CLIENT_STOPPING} 以便退出游戏时把配置 GUI 的修改落盘。
   */
  @Override
  public void onInitializeClient() {
    Networking.registerClientReceivers();

    ClientModEvents.registerEntityRenderers();
    ClientModEvents.registerLayers();
    ClientModEvents.registerItemProperties();
    ClientModEvents.registerTooltipComponents();
    ClientModEvents.registerItemTooltips();
    setupParticles();

    PetInfoHudOverlay.init();

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> PetHomeConfigCommand.register(dispatcher));

    ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PetHomeConfig.save());
  }

  private static void setupParticles() {
    PetHomeMod.LOGGER.debug("Registered particle factories");
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.DEFLECTION_SHIELD, new ParticleDeflectionShield.Factory());
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.SNIFF, ParticleSniff.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.PSYCHIC_WALL, ParticlePsychicWall.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.INTIMIDATION, new ParticleIntimidation.Factory());
    ParticleFactoryRegistry.getInstance()
        .register(PHParticleRegistry.BLIGHT, ParticleBlight.Factory::new);
    ParticleFactoryRegistry.getInstance()
        .register(
            PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE, ParticleQuestionMark.Factory::new);
  }
}

package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.particle.*;
import com.github.yzqdev.pethome.client.render.OreColorRegistry;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.WeakHashMap;
import java.util.Map;

/**
 * 客户端杂项事件：粒子工厂注册、矿石高亮描边色、暗影手渲染数据。
 * {@code shadowPunchRenderData}（WeakHashMap）只能由客户端写（EntityTickHandler 客户端分支），
 * 服务端写无人消费且实体卸载后泄漏。
 *
 * @author yzqde
 * @date time 2025/1/9 8:27
 * @modified By:
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientGameEvents {
    /** 暗影手渲染插值：每 Mob → 上帧出拳进度数组（渲染层每帧读取） */
    public static Map<Entity, int[]> shadowPunchRenderData = new WeakHashMap<>();

    /** MOD bus 监听注册入口（构造期由 PetHomeModClient 调用一次） */
    public static void registerClientListeners(IEventBus iEventBus) {
        iEventBus.addListener(ClientGameEvents::setupParticles);


    }

    /** 由 EntityTickHandler 客户端分支每 tick 写入（服务端勿调） */
    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }


    /** 注册全部 11 种粒子的工厂（special 两个 / sprite set 其余） */
    public static void setupParticles(RegisterParticleProvidersEvent event) {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        event.registerSpecial(PHParticleRegistry.DEFLECTION_SHIELD.get(), new ParticleDeflectionShield.Factory());
        event.registerSpriteSet(PHParticleRegistry.MAGNET.get(), ParticleMagnet.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.ZZZ.get(), ParticleZZZ.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.GIANT_POP.get(), ParticleGiantPop.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.SIMPLE_BUBBLE.get(), ParticleSimpleBubble.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.VAMPIRE.get(), ParticleVampire.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.SNIFF.get(), ParticleSniff.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.PSYCHIC_WALL.get(), ParticlePsychicWall.Factory::new);
        event.registerSpecial(PHParticleRegistry.INTIMIDATION.get(), new ParticleIntimidation.Factory());
        event.registerSpriteSet(PHParticleRegistry.BLIGHT.get(), ParticleBlight.Factory::new);
        event.registerSpriteSet(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE.get(), ParticleQuestionMark.Factory::new);
    }

    /** 矿石嗅探高亮方块按矿石方块态取描边色（OreColorRegistry） */
    @SubscribeEvent
    public static void onOutlineColor(EventGetOutlineColor event) {
        if (event.getEntityIn() instanceof HighlightedBlockEntity) {
            event.setColor(OreColorRegistry.getBlockColor(((HighlightedBlockEntity) event.getEntityIn()).getBlockState()));

            event.setResult(EventGetOutlineColor.Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void onAttackEntityFromClientEvent(InputEvent.InteractionKeyMappingTriggered event) {
    }

}

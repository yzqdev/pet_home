package com.github.yzqdev.pet_home.client;

import com.github.yzqdev.pet_home.PetHomeMod;
import com.github.yzqdev.pet_home.client.particle.ParticleBlight;
import com.github.yzqdev.pet_home.client.particle.ParticleDeflectionShield;
import com.github.yzqdev.pet_home.client.particle.ParticleGiantPop;
import com.github.yzqdev.pet_home.client.particle.ParticleIntimidation;
import com.github.yzqdev.pet_home.client.particle.ParticleMagnet;
import com.github.yzqdev.pet_home.client.particle.ParticlePsychicWall;
import com.github.yzqdev.pet_home.client.particle.ParticleQuestionMark;
import com.github.yzqdev.pet_home.client.particle.ParticleSimpleBubble;
import com.github.yzqdev.pet_home.client.particle.ParticleSniff;
import com.github.yzqdev.pet_home.client.particle.ParticleVampire;
import com.github.yzqdev.pet_home.client.particle.ParticleZZZ;
import com.github.yzqdev.pet_home.client.render.OreColorRegistry;
import com.github.yzqdev.pet_home.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pet_home.server.misc.PHParticleRegistry;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yzqde
 * @date time 2025/1/9 8:27
 * @modified By:
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientProxy {
    public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();
    public static void registerClientListeners(IEventBus iEventBus) {
        iEventBus.addListener(ClientProxy::setupParticles);


    }

    public static  void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }


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
//        event.registerSpriteSet(DIParticleRegistry.LANTERN_BUGS.get(), ParticleLanternBugs.Factory::new);
    }
    @SubscribeEvent
    public static void onOutlineColor(EventGetOutlineColor event) {
        if(event.getEntityIn() instanceof HighlightedBlockEntity){
            event.setColor(OreColorRegistry.getBlockColor(((HighlightedBlockEntity) event.getEntityIn()).getBlockState()));

            event.setResult(EventGetOutlineColor.Result.ALLOW );
        }
    }
    @SubscribeEvent
    public static void onAttackEntityFromClientEvent(InputEvent.InteractionKeyMappingTriggered event) {
    }

}

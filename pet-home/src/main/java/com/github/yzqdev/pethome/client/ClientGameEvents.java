package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.particle.ParticleBlight;
import com.github.yzqdev.pethome.client.particle.ParticleDeflectionShield;
import com.github.yzqdev.pethome.client.particle.ParticleGiantPop;
import com.github.yzqdev.pethome.client.particle.ParticleIntimidation;
import com.github.yzqdev.pethome.client.particle.ParticleMagnet;
import com.github.yzqdev.pethome.client.particle.ParticlePsychicWall;
import com.github.yzqdev.pethome.client.particle.ParticleQuestionMark;
import com.github.yzqdev.pethome.client.particle.ParticleSimpleBubble;
import com.github.yzqdev.pethome.client.particle.ParticleSniff;
import com.github.yzqdev.pethome.client.particle.ParticleVampire;
import com.github.yzqdev.pethome.client.particle.ParticleZZZ;
import com.github.yzqdev.pethome.client.render.OreColorRegistry;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;


public class ClientGameEvents {
    public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();


    public static void registerClientListeners() {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
        registry.register(PHParticleRegistry.DEFLECTION_SHIELD, ParticleDeflectionShield.Factory::new);
        registry.register(PHParticleRegistry.MAGNET, ParticleMagnet.Factory::new);
        registry.register(PHParticleRegistry.ZZZ, ParticleZZZ.Factory::new);
        registry.register(PHParticleRegistry.GIANT_POP, ParticleGiantPop.Factory::new);
        registry.register(PHParticleRegistry.SIMPLE_BUBBLE, ParticleSimpleBubble.Factory::new);
        registry.register(PHParticleRegistry.VAMPIRE, ParticleVampire.Factory::new);
        registry.register(PHParticleRegistry.SNIFF, ParticleSniff.Factory::new);
        registry.register(PHParticleRegistry.PSYCHIC_WALL, ParticlePsychicWall.Factory::new);
        registry.register(PHParticleRegistry.INTIMIDATION, ParticleIntimidation.Factory::new);
        registry.register(PHParticleRegistry.BLIGHT, ParticleBlight.Factory::new);
        registry.register(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE, ParticleQuestionMark.Factory::new);
    }

    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }


    public static int resolveOutlineColor(EventGetOutlineColor event) {
        if (event.getEntityIn() instanceof HighlightedBlockEntity highlighted) {
            event.setColor(OreColorRegistry.getBlockColor(highlighted.getBlockState()));
            event.setResult(EventGetOutlineColor.Result.ALLOW);
        }
        return event.getColor();
    }
}

package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.client.particle.*;
import com.github.yzqdev.pethome.client.render.OreColorRegistry;
import com.github.yzqdev.pethome.server.entity.HighlightedBlockEntity;
import com.github.yzqdev.pethome.server.misc.PHParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * 客户端事件聚合：粒子工厂注册、矿石高亮描边色、暗影手渲染插值数据（仅客户端写）；
 * 攻击前的客户端取消检查当前为空实现（友好保护实际走服务端 EntityHurtHandler）。
 *
 * @author yzqde
 * @date time 2025/1/9 8:27
 * @modified By:
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientGameEvents {
    /** 暗影手出拳进度的客户端插值数据（WeakHashMap 随实体回收）。只能客户端写；服务端写无人消费且泄漏。 */
    public static Map<Entity, int[]> shadowPunchRenderData = new WeakHashMap<>();

    /** 客户端 mod 总线监听注册（PetHomeModClient 构造时调用）：粒子、粒子组与 GUI 层（宠物信息面板）。 */
    public static void registerClientListeners(IEventBus iEventBus) {
        iEventBus.addListener(ClientGameEvents::setupParticles);
        iEventBus.addListener(ClientGameEvents::setupParticleGroups);
        iEventBus.addListener(PetInfoHudOverlay::registerGuiLayers);


    }

    /**
     * disc_jockey（唱片骑师，自 1.20 移植）随从点唱机音效登记表：实体 id → 循环音效实例。 仅
     * FollowingJukeboxEntity#handleEntityEvent（66 播放中 / 67 空闲）在客户端调用。
     */
    public static final java.util.Map<Integer, DiscJockeySound> DISC_JOCKEY_SOUND_MAP =
            new java.util.HashMap<>();

    /** 随从点唱机的音效状态更新，仅在实体事件包到达客户端时调用（66=开播 / 67=停播）。 */
    public static void updateEntityStatus(Entity entity, byte updateKind) {
        if (entity instanceof com.github.yzqdev.pethome.server.entity.FollowingJukeboxEntity jukebox) {
            net.minecraft.sounds.SoundEvent record = jukebox.getRecordSound();
            if (entity.isAlive() && updateKind == 66) {
                DiscJockeySound sound;
                if (record != null
                        && (DISC_JOCKEY_SOUND_MAP.get(entity.getId()) == null
                        || DISC_JOCKEY_SOUND_MAP.get(entity.getId()).getRecordSound() != record)) {
                    sound = new DiscJockeySound(record, jukebox);
                    DISC_JOCKEY_SOUND_MAP.put(entity.getId(), sound);
                } else {
                    sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                }
                if (sound != null
                        && !Minecraft.getInstance().getSoundManager().isActive(sound)
                        && sound.canPlaySound()
                        && sound.isNearest()) {
                    Minecraft.getInstance().getSoundManager().play(sound);
                }
            }
            if (updateKind == 67 || record == null) {
                if (DISC_JOCKEY_SOUND_MAP.containsKey(entity.getId())) {
                    DiscJockeySound sound = DISC_JOCKEY_SOUND_MAP.get(entity.getId());
                    DISC_JOCKEY_SOUND_MAP.remove(entity.getId());
                    Minecraft.getInstance().getSoundManager().stop(sound);
                }
            }
        }
    }

    /** EntityTickHandler 客户端侧调用：写入该实体的暗影手出拳进度（渲染插值用）。 */
    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }


    /** 注册 9 种贴图粒子工厂 + 2 种自定义渲染粒子（护盾模型/恐吓幻影）。 */
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

    /** 注册自定义渲染粒子组（护盾/恐吓幻影，PH_PHANTOM 渲染类型）。 */
    public static void setupParticleGroups(RegisterParticleGroupsEvent event) {
        event.register(PhantomParticleGroup.PHANTOM, PhantomParticleGroup::new);
    }

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

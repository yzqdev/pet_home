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
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.WeakHashMap;


/**
 * 客户端游戏事件与粒子 provider 注册（仅客户端，{@code PetHomeModClient} 调用）。
 */
public class ClientGameEvents {
    /**
     * 暗影手出拳进度的客户端插值数据（实体 → 每拳进度数组，{@code LayerPetOverlays} 渲染拳头用）。
     * 用 WeakHashMap 以实体为弱键——实体卸载被 GC 后条目自动回收，HashMap 会在实体卸载后泄漏。
     */
    public static Map<Entity, int[]> shadowPunchRenderData = new WeakHashMap<>();


    /** 注册 9 种贴图粒子工厂 + 2 种自定义渲染粒子（护盾模型/恐吓幻影，PhantomParticleGroup）。 */
    public static void registerClientListeners() {
        PetHomeMod.LOGGER.debug("Registered particle factories");
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
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
        registry.register(PHParticleRegistry.QUESTION_MARK_PARTICLE_TYPE, ParticleQuestionMark.Factory::new);
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


    /** 矿石高亮框的轮廓色：按方块贴图平均色（{@code OreColorRegistry}，26.1 无逐像素读取时回退浅灰）。 */
    public static int resolveOutlineColor(EventGetOutlineColor event) {
        if (event.getEntityIn() instanceof HighlightedBlockEntity highlighted) {
            event.setColor(OreColorRegistry.getBlockColor(highlighted.getBlockState()));
            event.setResult(EventGetOutlineColor.Result.ALLOW);
        }
        return event.getColor();
    }
}

package com.github.yzqdev.pethome.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * @author yzqde
 * @date time 2025/1/9 8:27
 * @modified By:
 *     <p>Fabric port: 粒子/渲染器注册在 PetHomeModClient (client entrypoint) 完成。 本类保持
 *     common-safe（EntityTickHandler 在双端调用 updateVisualDataForMob）。
 *     <p>暗影之手（SHADOW_HANDS）的客户端渲染数据缓存：服务端与客户端 tick 各写入一次， 渲染层 {@code LayerPetOverlays} 以本 map
 *     为上一帧值、以 citadel 数据里的当前值为终值做插值， 让出拳动画在实体追踪间隔之间平滑过渡。仅客户端有意义。
 */
public class ClientGameEvents {
  /** 实体 → 每只手的出拳进度（索引对应第 i 只手）；{@code EntityTickHandler} 每 tick 覆盖写入，实体卸载时不清理（随弱引用自然过期）。 */
  public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();

  /** 记录某实体当前的暗影之手出拳进度数组（双端可调，客户端用于下一帧插值）。 */
  public static void updateVisualDataForMob(Entity entity, int[] arr) {
    shadowPunchRenderData.put(entity, arr);
  }

  public static final Map<Integer, DiscJockeySound> DISC_JOCKEY_SOUND_MAP = new HashMap<>();

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
}

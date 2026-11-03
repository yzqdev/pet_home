package com.github.yzqdev.pethome.server.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.Entity;

/**
 * 项圈换装后的实体 tick 抑制表（每维度一份，由 {@code ServerEvent} 的静态 Map 持有）： 换项圈/写附魔后短暂屏蔽 {@code EntityTickMixin}
 * 的处理，避免刚写好的附魔数据在同一 tick 内被重复消费。
 */
public class CollarTickTracker {

  private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();

  /** 把实体加入抑制表并设定剩余 tick 数。 */
  public void addBlockedEntityTick(UUID uuid, int duration) {
    this.blockedCollarTagUpdates.put(uuid, duration);
  }

  public boolean isEntityBlocked(Entity entity) {
    return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
  }

  /** 每维度 tick 递减剩余抑制时间（由 {@code ServerEvent.onServerTick} 驱动）。 */
  public void tick() {
    if (!blockedCollarTagUpdates.isEmpty()) {
      Set<UUID> blockedUUIDs = blockedCollarTagUpdates.keySet();
      for (UUID uuid : blockedUUIDs) {
        int set = blockedCollarTagUpdates.get(uuid) - 1;
        if (set < 0) {
          blockedCollarTagUpdates.remove(uuid);
        } else {
          blockedCollarTagUpdates.put(uuid, set);
        }
      }
    }
  }
}

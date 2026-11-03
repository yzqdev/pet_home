package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 项圈换装后的实体 tick 抑制表（UUID → 剩余 tick）。每维度一份（{@code ServerEvent} 的 map 持有），
 * {@code tick()} 由世界 tick 推进倒计时；用于换项圈后短暂屏蔽项圈渲染 / 附魔 tick，避免旧数据闪现。
 */
public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    public void tick(){
        if(!blockedCollarTagUpdates.isEmpty()){
            Set<UUID> blockedUUIDs = blockedCollarTagUpdates.keySet();
            for(UUID uuid : blockedUUIDs){
                int set = blockedCollarTagUpdates.get(uuid) - 1;
                if(set < 0){
                    blockedCollarTagUpdates.remove(uuid);
                }else{
                    blockedCollarTagUpdates.put(uuid, set);
                }
            }
        }
    }
}

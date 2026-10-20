package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 换项圈后的防抖表（每维度一个实例，见 ServerEvent.COLLAR_TICK_TICKER_MAP）：
 * 项圈装备/取下后 5 tick 内 {@code canTickCollar} 返回 false，
 * 避免项圈附魔 tick 逻辑在数据刚落槽时重复触发。计时由 ServerEvent.onServerTick 每 tick 递减。
 */
public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    /** 把实体加入屏蔽表（duration 为剩余 tick 数，目前固定 5） */
    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    /** 屏蔽期内返回 true（EntityTickHandler 据此跳过该实体本 tick 的附魔逻辑） */
    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    public void tick(){
        if(blockedCollarTagUpdates.isEmpty()){
            return;
        }
        // 遍历 entrySet 并通过 iterator 删除，避免在 keySet 迭代中结构性修改导致 CME
        Iterator<Map.Entry<UUID, Integer>> iterator = blockedCollarTagUpdates.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<UUID, Integer> entry = iterator.next();
            int set = entry.getValue() - 1;
            if(set < 0){
                iterator.remove();
            }else{
                entry.setValue(set);
            }
        }
    }
}

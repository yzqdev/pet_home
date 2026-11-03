package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 项圈交互防抖表：戴上/换项圈触发附魔迁移后的 20 tick 内屏蔽后续项圈更新，
 * 防止同一次交互被多事件重复处理。实例持有于 ServerEvents，服务端每 tick 调 {@link #tick()} 递减。
 */
public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    /** 为实体加入 duration tick 的屏蔽期。 */
    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    /** 该实体是否仍在屏蔽期内（项圈更新处理前检查）。 */
    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    /** 每 tick 递减剩余屏蔽时间，归零移除（entrySet 迭代器删除，避免 CME）。 */
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

package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

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

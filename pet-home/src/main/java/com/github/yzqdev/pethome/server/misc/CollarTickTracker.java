package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 换项圈后的防抖计时器：戴/换项圈会连续多 tick 重写 citadel tag（清旧附魔+写新附魔），
 * 短时间内冻结项圈相关 tick，避免同帧反复触发。每维度一个实例（ServerEvent 维护）。
 */
public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    /** 对实体起 {@code duration} tick 的防抖窗口。 */
    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    /** 该实体是否仍在防抖窗口内（每 tick 剩余数由 {@link #tick()} 递减）。 */
    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    /** 每维度 tick 递减（ServerEvent.onServerTick 调用）：到 0 移除防抖窗口。 */
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

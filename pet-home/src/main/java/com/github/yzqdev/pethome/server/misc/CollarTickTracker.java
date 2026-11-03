package com.github.yzqdev.pethome.server.misc;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 单个维度的项圈 tick 抑制表（UUID → 剩余抑制 tick 数），由 {@code CollarTickManager}
 * 按维度持有；{@code EntityTickHandler.init()} 在 {@code END_LEVEL_TICK} 里驱动 {@link #tick()} 递减。
 */
public class CollarTickTracker {

    private final Map<UUID, Integer> blockedCollarTagUpdates = new HashMap<>();


    /** 为实体登记 {@code duration} tick 的抑制窗口（期间跳过项圈标签更新）。 */
    public void addBlockedEntityTick(UUID uuid, int duration){
        this.blockedCollarTagUpdates.put(uuid, duration);
    }

    /** 实体是否仍在抑制窗口内（剩余 tick 数 &gt; 0）。 */
    public boolean isEntityBlocked(Entity entity){
        return this.blockedCollarTagUpdates.getOrDefault(entity.getUUID(), 0) > 0;
    }

    /** 每 tick 递减全部倒计时，归零即解除抑制并移除条目。 */
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

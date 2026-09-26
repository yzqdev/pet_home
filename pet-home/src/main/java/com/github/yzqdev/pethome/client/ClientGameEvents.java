package com.github.yzqdev.pethome.client;

import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yzqde
 * @date time 2025/1/9 8:27
 * @modified By:
 *
 * Fabric port: 粒子/渲染器注册在 PetHomeModClient (client entrypoint) 完成。
 * 本类保持 common-safe（EntityTickHandler 在双端调用 updateVisualDataForMob）。
 */
public class ClientGameEvents {
    public static Map<Entity, int[]> shadowPunchRenderData = new HashMap<>();

    public static void updateVisualDataForMob(Entity entity, int[] arr) {
        shadowPunchRenderData.put(entity, arr);
    }
}

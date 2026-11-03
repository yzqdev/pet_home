package com.github.yzqdev.pethome.client.compass;

import java.util.UUID;

/**
 * 罗盘列表的单条数据（S2C {@code pet_compass_data} 打包结果的客户端视图）：档案静态字段 + 动态状态（加载/失效）+ 实时状态（血量/指令/附魔，仅在线宠物有值）。
 * 纯数据载体，不含行为；由 {@link CompassPacket} 从网络包解析，{@link CompassListState} 持有列表，渲染与点击命中共用。
 */
public record CompassEntry(
        UUID petId,
        String name,
        String type,
        String dim,
        int x,
        int y,
        int z,
        int dist,
        boolean loaded,
        boolean alive,
        /** 宠物在收存容器中（召回宝盒/生物球）：实体不存在，按容器位置显示 */
        boolean inBall,
        /** 收存容器种类：recall = 召回宝盒，net = 生物球（捕网）；决定状态文案 */
        String ballKind,
        /** 记录已失效：坐标所在区块已加载却查不到实体（转让/被清除/跨版本存档），区别于「区块未加载暂时够不着」 */
        boolean stale,
        boolean sameDim,
        long lastTime,
        /* 实时状态（仅在线宠物有值）：血量/最大血量、三态指令编号（-1 = 非本模组三态宠物）、附魔摘要串。 */
        float health,
        float maxHealth,
        int cmd,
        String enchants) {
}

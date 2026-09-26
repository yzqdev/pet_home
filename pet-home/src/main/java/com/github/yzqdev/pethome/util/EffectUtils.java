package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class EffectUtils {
    public static void summonLightning(LivingEntity entity) {
        // 获取实体的坐标
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        // 创建闪电实体
        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
        lightning.setPos(x + 10, y, z);

        // 将闪电添加到世界中 (26.1: 只有 ServerLevel 能真正生成实体)
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(lightning);
        }
    }
}
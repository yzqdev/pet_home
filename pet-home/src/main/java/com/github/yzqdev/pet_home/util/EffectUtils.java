package com.github.yzqdev.pet_home.util;

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

        // 将闪电添加到世界中
        entity.level().addFreshEntity(lightning);
    }
}

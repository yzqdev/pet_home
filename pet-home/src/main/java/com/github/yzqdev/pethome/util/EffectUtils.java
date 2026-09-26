package com.github.yzqdev.pethome.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class EffectUtils {
    public static void summonLightning(LivingEntity entity) {

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();


        LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
        lightning.setPos(x + 10, y, z);


        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(lightning);
        }
    }
}
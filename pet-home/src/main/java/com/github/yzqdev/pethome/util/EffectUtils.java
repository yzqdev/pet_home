package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

/** 效果类小工具（附魔 handler 用的特殊效果触发）。 */
public class EffectUtils {
  /** 在实体东侧 10 格处生成一道真实闪电（未设 setVisualOnly，会造成伤害/起火）；仅服务端 addFreshEntity 有效。 */
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

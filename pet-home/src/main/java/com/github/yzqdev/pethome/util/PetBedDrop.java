package com.github.yzqdev.pethome.util;

import net.minecraft.world.entity.LivingEntity;

/** 宠物床绑定状态的轻量判断工具（供女仆等第三方实体 mixin 复用，判断该生物是否绑定了宠物床）。 */
public class PetBedDrop {

  public static boolean hasPetBedPos(LivingEntity maid) {
    return TameableUtils.isTamed(maid) && TameableUtils.getPetBedPos(maid) != null;
  }
}

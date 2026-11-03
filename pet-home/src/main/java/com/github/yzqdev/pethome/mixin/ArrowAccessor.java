package com.github.yzqdev.pethome.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invoker：暴露私有的 {@code AbstractArrow#setPierceLevel}（穿透箭打到宠物时清穿透，见 ServerEvent.onProjectileImpactEvent）。 */
@Mixin(AbstractArrow.class)
public interface ArrowAccessor {

    // 26.1: AbstractArrow#setPierceLevel 变为私有，通过 Invoker 暴露出来
    @Invoker("setPierceLevel")
    void invokeSetPierceLevel(byte pierceLevel);

}
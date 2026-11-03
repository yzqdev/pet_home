package com.github.yzqdev.pethome.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code AbstractArrow} 访问器：{@link Invoker} 暴露私有的 {@code setPierceLevel}
 * （连锁闪电附魔给箭设置穿透等级用）。
 */
@Mixin(AbstractArrow.class)
public interface ArrowAccessor {

    // 26.1: AbstractArrow#setPierceLevel 变为私有，通过 Invoker 暴露出来
    @Invoker("setPierceLevel")
    void invokeSetPierceLevel(byte pierceLevel);

}
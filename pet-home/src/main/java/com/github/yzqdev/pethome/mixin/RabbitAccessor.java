package com.github.yzqdev.pethome.mixin;

import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code Rabbit} 访问器：{@link Invoker} 暴露私有的 {@code setVariant}
 * （"邪恶胡萝卜"把兔子转成邪恶兔变体时用）。
 */
@Mixin(Rabbit.class)
public interface RabbitAccessor {

    // 26.1: Rabbit#setVariant 变为私有，通过 Invoker 暴露出来
    @Invoker("setVariant")
    void invokeSetVariant(Rabbit.Variant variant);

}
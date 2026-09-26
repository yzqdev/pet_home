package com.github.yzqdev.pethome.mixin;

import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Rabbit.class)
public interface RabbitAccessor {

    // 26.1: Rabbit#setVariant 变为私有，通过 Invoker 暴露出来
    @Invoker("setVariant")
    void invokeSetVariant(Rabbit.Variant variant);

}
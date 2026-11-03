package com.github.yzqdev.pethome.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker：暴露 protected 的 {@code Entity#addAdditionalSaveData}（读第三方宠物 {@code *Command} 键时只需要
 * 附加数据；{@code saveWithoutId} 会连带打包 Brain/属性/药效等大块状态，实体卸载路径上调用不值得）。
 * 该方法在 {@link Entity} 上是 abstract protected，Invoker 虚分派到子类实现。
 */
@Mixin(Entity.class)
public interface EntityInvoker {

    @Invoker("addAdditionalSaveData")
    void pet_home$invokeAddAdditionalSaveData(CompoundTag tag);
}

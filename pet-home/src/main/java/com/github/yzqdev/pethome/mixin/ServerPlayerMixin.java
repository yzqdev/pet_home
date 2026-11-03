package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.event.ServerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 替代 Forge EntityTeleportEvent / EntityTravelToDimensionEvent：
 * 玩家传送时把有"拴魂"类附魔的宠物一起带走。
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    /** 注入 {@code ServerPlayer#changeDimension} 头部：玩家跨维度传送时把附近的"拴魂"宠物一并转移到目标维度。 */
    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
    private void ph_onChangeDimension(ServerLevel targetLevel, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerEvent.onEntityTravelToDimension(self, targetLevel);
    }

    /**
     * 注入 {@code ServerPlayer#teleportTo(ServerLevel,double,double,double,float,float)} 头部：
     * 1.20 的 /tp（{@code TeleportCommand.performTeleport}）对玩家最终汇入此方法（同维度分支走
     * {@code connection.teleport}，跨维度分支走 {@code changeDimension}，由 {@link #ph_onChangeDimension}
     * 处理）。旧入口 {@code teleportTo(DDD)V} 已不被 /tp 经过（known-issues 2026-11-03），故迁移至此。
     * HEAD 时玩家仍在原维度，同维度直传携宠语义不变。
     */
    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At("HEAD"))
    private void ph_onTeleportTo(
            ServerLevel targetLevel,
            double x,
            double y,
            double z,
            float yRot,
            float xRot,
            CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!self.level().isClientSide()) {
            ServerEvent.onEntityTeleport(self, new net.minecraft.world.phys.Vec3(x, y, z));
        }
    }
}

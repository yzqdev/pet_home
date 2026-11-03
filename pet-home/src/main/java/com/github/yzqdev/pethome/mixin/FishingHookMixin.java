package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.FeatherEntity;
import com.github.yzqdev.pethome.server.item.PHItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 羽毛钓竿的钓鱼钩行为适配：{@code @Redirect} tick 里的流体查询——FeatherEntity 视作不入水
 * （直接落到底部引导宠物）；{@code shouldStopFishing} 按羽毛钓竿持有判定收杆。
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    protected FishingHookMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    /** 重定向 {@code FishingHook#tick} 内的 {@code Level#getFluidState} 调用：探测羽毛（FeatherEntity）谎报不在水中，使钩子直接下坠落底引导宠物。 */
    @Redirect(
            method = {"tick()V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState di_getFluidState(Level level, BlockPos pos) {
        return (Projectile)this instanceof FeatherEntity ? Fluids.EMPTY.defaultFluidState() : level.getFluidState(pos);
    }

    /** 注入 {@code FishingHook#shouldStopFishing} 头部（可取消）：探测羽毛按"主手/副手持羽毛钓竿且在 32 格内"判定收杆，替代原版鱼竿逻辑。 */
    @Inject(
            method = {"shouldStopFishing(Lnet/minecraft/world/entity/player/Player;)Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_shouldStopFishing(Player player, CallbackInfoReturnable<Boolean> cir) {
        if((Projectile)this instanceof FeatherEntity){
            ItemStack itemstack = player.getMainHandItem();
            ItemStack itemstack1 = player.getOffhandItem();
            boolean flag = itemstack.is(PHItemRegistry.FEATHER_ON_A_STICK);
            boolean flag1 = itemstack1.is(PHItemRegistry.FEATHER_ON_A_STICK);
            if (!this.isRemoved() && this.isAlive() && (flag || flag1) && this.distanceToSqr(player) < 1024.0D) {
                cir.setReturnValue(false);
            } else {
                this.discard();
                cir.setReturnValue(true);
            }
        }
    }
}

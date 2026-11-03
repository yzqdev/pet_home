package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@code @Redirect} 玩家攻击的横扫 {@code isAlliedTo} 判定：自己的宠物也视为「不横扫」，
 * 避免横扫攻击波及友军宠物。
 */
@Mixin(Player.class)
public class PlayerMixin {

    /** 重定向 {@code Player#attack} 里的 {@code isAlliedTo} 调用：自己的宠物也算"盟友"，横扫攻击不再波及宠物。 */
    @Redirect(
            method = {"attack(Lnet/minecraft/world/entity/Entity;)V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean di_onSweepAttack_isAlliedTo(Player player, Entity entity) {
        return TameableUtils.isPetOf(player, entity) || player.isAlliedTo(entity);
    }
}

package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 玩家攻击横扫判定重定向：{@code isPetOf} 的宠物（含友军链）不触发横扫特效，
 * 避免玩家挥击自家宠物旁的空气时误伤/触发保护提示。
 */
@Mixin(Player.class)
public class PlayerMixin {

    @Redirect(
            method = {"Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V"},
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

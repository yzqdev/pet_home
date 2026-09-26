package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerMixin {

    @Redirect(
            // 26.1: isAlliedTo 检查从 attack 移入 doSweepAttack。
            // 注意 NeoForge 版的原签名（多带 DamageSource / AABB 参数）是 NeoForge patch 过的，
            // Fabric/原版的 doSweepAttack 参数列表不同，这里只按方法名匹配；
            // require = 0 保证签名再变时只降级不崩溃（表现为「横扫不会误伤友方宠物」这一条失效）。
            method = "doSweepAttack",
            remap = true,
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean di_onSweepAttack_isAlliedTo(Player player, Entity entity) {
        return TameableUtils.isPetOf(player, entity) || player.isAlliedTo(entity);
    }
}

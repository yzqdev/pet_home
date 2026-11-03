package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * charisma ：对村民声望（交易折扣基数）追加主人附近宠物的 charisma 加成。
 * 注入 {@code Villager#getPlayerReputation} TAIL，复用原版折扣公式。
 */
@Mixin(Villager.class)
public class VillagerMixin {

    @Inject(
            at = @At("TAIL"),
            cancellable = true,
            method = "getPlayerReputation(Lnet/minecraft/world/entity/player/Player;)I"
    )
    private void di_getPlayerReputation(Player player, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + TameableUtils.getCharismaBonusForOwner(player));
    }
}

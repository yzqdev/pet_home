package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 村民好感度加成：CHARISMA（魅力）附魔按周围宠物等级给主人交易折扣（见 {@code TameableUtils.getCharismaBonusForOwner}）。 */
@Mixin(Villager.class)
public class VillagerMixin {


    /** 注入 {@code Villager#getPlayerReputation} 尾部：在原版好感度上叠加 CHARISMA（魅力）附魔带来的宠物数量加成（正数 = 交易更便宜）。 */
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"getPlayerReputation(Lnet/minecraft/world/entity/player/Player;)I"},
            cancellable = true
    )
    private void di_getPlayerReputation(Player player, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + TameableUtils.getCharismaBonusForOwner(player));
    }
}

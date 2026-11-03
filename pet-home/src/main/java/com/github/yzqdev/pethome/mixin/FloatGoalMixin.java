package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 漂浮 Goal 调整：带 AMPHIBIOUS 的宠物不启用原版漂浮（由两栖导航接管水中移动）。 */
@Mixin(FloatGoal.class)
public abstract class FloatGoalMixin extends Goal {

    @Shadow
    @Final
    private Mob mob;


    /** 注入 {@code FloatGoal#canUse} 头部（可取消）：驯服且带 AMPHIBIOUS（两栖）附魔的宠物禁用漂浮 Goal，水中移动交给两栖导航。 */
    @Inject(
            at = {@At("HEAD")},
            remap = true,
            method = {"canUse()Z"},
            cancellable = true
    )
    private void di_canUse(CallbackInfoReturnable<Boolean> cir){
        if(TameableUtils.isTamed(mob) && TameableUtils.hasEnchant(mob, PHEnchantmentRegistry.AMPHIBIOUS)){
            cir.setReturnValue(false);
        }
    }

}

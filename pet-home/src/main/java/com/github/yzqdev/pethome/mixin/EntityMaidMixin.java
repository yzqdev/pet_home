package com.github.yzqdev.pethome.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 车万女仆（可选联动）：绑定了宠物床的女仆死亡时不掉落装备。
 * <p>
 * tlm 以 modCompileOnly 参与编译，AP 才能写出 dropEquipment 的 intermediary refmap
 * （生产环境缺这条映射时注入点匹配不到，require = 0 会静默失效）；这里已经修复
 */
@Mixin(EntityMaid.class)
public abstract class EntityMaidMixin {

    @Inject(
            method = "dropEquipment",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void pethome$dropEquipment(CallbackInfo ci) {
        if (TameableUtils.getPetBedPos((LivingEntity) (Object) this) != null) {
            ci.cancel();
        }
    }
}

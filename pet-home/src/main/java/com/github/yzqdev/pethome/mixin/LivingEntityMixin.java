package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 注入 {@code LivingEntity}：TAIL 可取消改写 {@code getWaterSlowDown}——两栖附魔宠物在水中几乎不减速（0.98）。
 */
@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<? extends Entity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(
            method = {"getWaterSlowDown()F"},
            remap = true,
            at = @At(value = "TAIL"),
            cancellable = true
    )
    private void di_getWaterSlowdown(CallbackInfoReturnable<Float> cir) {
        if(TameableUtils.isTamed(this) && isLandAndSea()){
            cir.setReturnValue(0.98F);
        }
    }

    private boolean isLandAndSea(){
        return TameableUtils.hasEnchant(((LivingEntity) (Entity)this), PHEnchantments.AMPHIBIOUS);
    }

}

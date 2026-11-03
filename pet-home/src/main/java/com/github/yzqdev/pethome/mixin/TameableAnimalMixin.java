package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.datagen.PHEnchantments;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code TamableAnimal} 注入（对狼/猫/鹦鹉等原版宠物生效）：
 * {@code considersEntityAsAlly} 判同主人宠物互为友军（友军伤害保护）；
 * {@code canTeleportTo} 尾部允许携带"两栖"附魔的宠物传送到水中位置
 * （原版禁止，会导致水中跟随失败）。
 */
@Mixin(TamableAnimal.class)
public abstract class TameableAnimalMixin extends Animal {

    protected TameableAnimalMixin(EntityType<? extends Animal> an, Level lvl) {
        super(an, lvl);
    }

    @Inject(
            // 26.1: TamableAnimal 不再覆写 isAlliedTo(Entity)，同类盟友判断改为 considersEntityAsAlly
            method = {"considersEntityAsAlly(Lnet/minecraft/world/entity/Entity;)Z"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void di_isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
        if(TameableUtils.hasSameOwnerAs(this, other)){
            cir.setReturnValue(true);
        }
    }
    // 26.1：canTeleportTo 移到 TamableAnimal——两栖附魔允许传送到水中（自 FollowOwnerGoalMixin 搬家）
    @Inject(
            at = {@At("TAIL")},
            remap = true,
            method = {"canTeleportTo(Lnet/minecraft/core/BlockPos;)Z"},
            cancellable = true
    )
    private void di_canTeleportTo(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (TameableUtils.hasEnchant((LivingEntity) (Object) this, PHEnchantments.AMPHIBIOUS) && this.level().isWaterAt(pos)) {
            cir.setReturnValue(true);
        }
    }

}

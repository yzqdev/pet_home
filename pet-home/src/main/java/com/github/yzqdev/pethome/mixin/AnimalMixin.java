package com.github.yzqdev.pethome.mixin;


import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.IComandableMob;
import com.github.yzqdev.pethome.server.entity.IFrog;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {


    protected AnimalMixin(EntityType<? extends AgeableMob> type, Level lvl) {
        super(type, lvl);
    }

    @Inject(
            method = {"mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/AgeableMob;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            ),
            cancellable = true
    )
    private void di_onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if((Mob)this instanceof IFrog && ((IFrog)this).onFrogInteract(player, hand)){
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
        if((Mob)this instanceof Fox && this instanceof IComandableMob && this instanceof ModifiedToBeTameable tame && tame.isTame() && tame.getTameOwnerUUID().equals(player.getUUID()) && PetHomeMod.CONFIG.tameableFox.get() && PetHomeMod.CONFIG.trinaryCommandSystem.get()
                // 手持该宠物食物且原版会消费它时不拦——回血（不满血）/ 繁殖（成年可恋爱）/ 幼年加速成长；三态只接管「原版这次右键什么也不做」的情况
                && !(((Fox)(Mob)this).isFood(player.getItemInHand(hand)) && (((Mob)this).getHealth() < ((Mob)this).getMaxHealth() || ((Fox)(Mob)this).isBaby() || (((Fox)(Mob)this).getAge() == 0 && ((Fox)(Mob)this).canFallInLove())))){
            player.swing(hand, true);
            cir.setReturnValue(((IComandableMob)this).playerSetCommand(player, (Animal)(Mob)this));
        }
    }
}

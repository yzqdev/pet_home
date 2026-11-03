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

/**
 * 动物交互入口调整：{@code mobInteract} 里先处理青蛙自定义交互（{@code IFrog}）与
 * 狐狸主人的三态指令轮换，命中即返回 SUCCESS 阻断原版逻辑。
 */
@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {


    protected AnimalMixin(EntityType<? extends AgeableMob> type, Level lvl) {
        super(type, lvl);
    }

    /** 注入 {@code Animal#mobInteract} 转发到 {@code AgeableMob#mobInteract} 处（可取消）：青蛙自定义交互、驯服狐狸主人的指令轮换，命中即 SUCCESS 阻断原版。 */
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
        if((Mob)this instanceof Fox && this instanceof IComandableMob && this instanceof ModifiedToBeTameable tame && tame.isTame() && tame.getTameOwnerUUID().equals(player.getUUID()) && PetHomeMod.CONFIG.tameableFox.get() && PetHomeMod.CONFIG.trinaryCommandSystem.get()){
            player.swing(hand, true);
            cir.setReturnValue(((IComandableMob)this).playerSetCommand(player, (Animal)(Mob)this));
        }
    }
}

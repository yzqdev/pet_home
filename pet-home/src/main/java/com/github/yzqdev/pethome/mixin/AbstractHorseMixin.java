package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 马类（马/驴/骡等）驯服扩展：实现 {@link ModifiedToBeTameable}（桥接原版 isTamed/getOwnerUUID），
 * 骑乘 tick 支持 AMPHIBIOUS 水上骑行。
 */
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements ModifiedToBeTameable {

    @Shadow
    protected float playerJumpPendingScale;
    @Shadow
    protected boolean isJumping;
    @Shadow
    private boolean allowStandSliding;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract boolean isTamed();

    @Shadow
    public abstract void setTamed(boolean tame);

    @Shadow
    public abstract void setIsJumping(boolean jumping);

    @Shadow
    public abstract boolean isStanding();

    @Shadow @Nullable public abstract UUID getOwnerUUID();

    @Shadow public abstract void setOwnerUUID(@org.jetbrains.annotations.Nullable UUID uuid);

    /** 注入 {@code AbstractHorse#tickRidden} 头部（可取消）：AMPHIBIOUS 马在水中时改为水中骑行操控（转向/浮沉/跳跃上浮），拦截原版骑乘逻辑。 */
    @Inject(
            method = {"tickRidden(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;)V"},
            remap = true,
            at = {@At("HEAD")},
            cancellable = true
    )
    private void di_tickRidden(Player rider, Vec3 vec3, CallbackInfo ci) {
        if (this.isAlive() && this.isVehicle() && this.isInWaterOrBubble() && TameableUtils.hasEnchant(this, PHEnchantmentRegistry.AMPHIBIOUS)) {
            LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
            this.setYRot(livingentity.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(livingentity.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
            float f = livingentity.xxa * 0.5F;
            float f1 = livingentity.zza;
            if (f1 <= 0.0F) {
                f1 *= 0.25F;
            }
            float down = 0.08F;
            if (playerJumpPendingScale != 0) {
                down -= playerJumpPendingScale;
                playerJumpPendingScale = 0;
            }
            if (this.isControlledByLocalInstance()) {
                this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
                super.travel(new Vec3(f, vec3.y - down, f1));
            } else if (livingentity instanceof Player) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D).add(0, -down, 0));
            }
            this.calculateEntityAnimation(false);
            this.tryCheckInsideBlocks();
            ci.cancel();
        }
    }

    public boolean isTame() {
        return this.isTamed() && PetHomeMod.CONFIG.tameableHorse.get();
    }

    public void setTame(boolean tame) {
        this.setTamed(tame);
    }

    public UUID getTameOwnerUUID(){
        return this.getOwnerUUID();
    }
    public void setTameOwnerUUID(@Nullable UUID uuid){
        this.setOwnerUUID(uuid);
    }

    @Nullable
    public LivingEntity getTameOwner() {
        try {
            UUID uuid = this.getTameOwnerUUID();
            return uuid == null ? null : this.level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public boolean isFollowingOwner() {
        return false;
    }

    public boolean isStayingStill() {
        return false;
    }

    public boolean isValidAttackTarget(LivingEntity target) {
        return false;
    }
}

package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.entity.ai.AmphibiousPathNavigation;
import com.github.yzqdev.pethome.server.entity.ai.AquaticMoveControl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mob 行为调整：AMPHIBIOUS 宠物入水时动态切换两栖导航器 / 出水还原；LINKED_INVENTORY
 * 拾取物直接转入主人背包；MUFFLED 宠物静音（不播环境音）。
 */
@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    @Shadow protected MoveControl moveControl;
    @Shadow protected PathNavigation navigation;
    @Shadow  public abstract void setPathfindingMalus(BlockPathTypes water, float v);
    private boolean hasWaterEnchantNavigator = false;
    private MoveControl prevMoveControl;
    private PathNavigation prevNavigation;

    protected MobMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    /** 注入 {@code Mob#tick} 尾部：AMPHIBIOUS 宠物入水时切两栖导航器与水中移动控制，出水还原（状态缓存避免每 tick 重建）。 */
    @Inject(
            method = {"tick()V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    private void di_tick(CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && !level().isClientSide()){
            if(TameableUtils.hasEnchant(this, PHEnchantmentRegistry.AMPHIBIOUS) && !((LivingEntity)this instanceof Axolotl)){
                if(!hasWaterEnchantNavigator && this.isInWaterOrBubble()){
                    this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
                    this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0F);
                    prevMoveControl = moveControl;
                    prevNavigation = navigation;
                    moveControl = new AquaticMoveControl((Mob)(LivingEntity)this);
                    navigation = new AmphibiousPathNavigation((Mob)(LivingEntity)this, level());
                    hasWaterEnchantNavigator = true;
                }
                if(hasWaterEnchantNavigator && !this.isInWaterOrBubble()) {
                    moveControl = prevMoveControl;
                    navigation = prevNavigation;
                    hasWaterEnchantNavigator = false;
                }
            }else if(hasWaterEnchantNavigator){
                moveControl = prevMoveControl;
                navigation = prevNavigation;
                hasWaterEnchantNavigator = false;
            }
        }
    }


    /** 注入 {@code Mob#pickUpItem} 头部：LINKED_INVENTORY（连锁背包）宠物把拾取物直接塞进主人背包，背包满则把物品搬到主人身边；取消原版"自己吃掉/捡起"逻辑。 */
    @Inject(
            method = {"pickUpItem(Lnet/minecraft/world/entity/item/ItemEntity;)V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_pickUpItem(ItemEntity item, CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantmentRegistry.LINKED_INVENTORY)){
            Entity owner = TameableUtils.getOwnerOf(this);
            if(owner instanceof Player){
                ci.cancel();
                if(((Player) owner).addItem(item.getItem())){
                    item.discard();
                }else{
                    item.copyPosition(owner);
                }
            }

        }
    }

    /** 注入 {@code Mob#playAmbientSound} 头部：MUFFLED（消音）宠物不再播放环境音。 */
    @Inject(
            method = {"playAmbientSound()V"},
            remap = true,
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void di_playAmbientSound(CallbackInfo ci) {
        if(TameableUtils.isTamed(this) && TameableUtils.hasEnchant(this, PHEnchantmentRegistry.MUFFLED)){
            ci.cancel();
        }
    }
}

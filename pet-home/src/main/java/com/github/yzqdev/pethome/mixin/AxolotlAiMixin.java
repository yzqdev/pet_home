package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.server.enchantment.PHEnchantmentRegistry;
import com.github.yzqdev.pethome.server.entity.PHActivityRegistry;
import com.github.yzqdev.pethome.server.entity.ModifiedToBeTameable;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianFollowOwnerBehavior;
import com.github.yzqdev.pethome.server.entity.ai.AmphibianStayBehavior;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.axolotl.AxolotlAi;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 美西螈脑部改造：驯服个体注入 follow/stay 自定义活动（{@code PHActivityRegistry}），并调整诱惑物与追击速度。 */
@Mixin(AxolotlAi.class)
public class AxolotlAiMixin {

    /** 注入 {@code AxolotlAi#makeBrain} 的装死活动初始化点：追加 AXOLOTL_FOLLOW / AXOLOTL_STAY 两个自定义活动。 */
    @Inject(
            method = {"makeBrain(Lnet/minecraft/world/entity/ai/Brain;)Lnet/minecraft/world/entity/ai/Brain;"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/axolotl/AxolotlAi;initPlayDeadActivity(Lnet/minecraft/world/entity/ai/Brain;)V"
            )
    )
    private static void di_makeBrain(Brain<Axolotl> brain, CallbackInfoReturnable<Brain<?>> cir) {
        brain.addActivity(PHActivityRegistry.AXOLOTL_FOLLOW, ImmutableList.of(Pair.of(0, new AmphibianFollowOwnerBehavior(0.3F, 0.6F))));
        brain.addActivity(PHActivityRegistry.AXOLOTL_STAY, ImmutableList.of(Pair.of(0, new AmphibianStayBehavior())));
    }

    /** 注入 {@code AxolotlAi#updateActivity} 头部（可取消）：驯服美西螈按指令切换活动——停留走 AXOLOTL_STAY；跟随按 装死>战斗>跟随 候选链。 */
    @Inject(
            method = {"updateActivity(Lnet/minecraft/world/entity/animal/axolotl/Axolotl;)V"},
            remap = true,
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void di_updateActivity(Axolotl axolotl, CallbackInfo ci) {
        Brain<Axolotl> brain = axolotl.getBrain();
        Activity activity = brain.getActiveNonCoreActivity().orElse(null);
        if (activity != Activity.PLAY_DEAD && !axolotl.isPlayingDead() && axolotl instanceof ModifiedToBeTameable ModifiedToBeTameable) {
            if (ModifiedToBeTameable.isStayingStill()) {
                brain.setActiveActivityIfPossible(PHActivityRegistry.AXOLOTL_STAY);
                ci.cancel();
            } else if (ModifiedToBeTameable.isFollowingOwner()) {
                brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.PLAY_DEAD, Activity.FIGHT, PHActivityRegistry.AXOLOTL_FOLLOW));
                ci.cancel();
            }
        }
    }


    /** 注入 {@code AxolotlAi#getTemptations} 尾部：诱惑物并入热带鱼（与原版驯食物一致，保证驯服后的诱惑行为可用）。 */
    @Inject(
            method = {"getTemptations()Lnet/minecraft/world/item/crafting/Ingredient;"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getTemptationItems(CallbackInfoReturnable<Ingredient> cir) {
        cir.setReturnValue(Ingredient.of(java.util.stream.Stream.concat(java.util.stream.Stream.of(cir.getReturnValue().getItems()), java.util.stream.Stream.of(Ingredient.of(Items.TROPICAL_FISH).getItems()))));
    }

    /** 注入 {@code AxolotlAi#getSpeedModifierChasing} 尾部：追击速度改写，并叠加 SPEEDSTER（疾速）附魔加成。 */
    @Inject(
            method = {"getSpeedModifierChasing(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierChasing(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, PHEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    /** 注入 {@code AxolotlAi#getSpeedModifierFollowingAdult} 尾部：跟随成年个体速度改写，并叠加 SPEEDSTER 附魔加成。 */
    @Inject(
            method = {"getSpeedModifierFollowingAdult(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifierFollowingAdult(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, PHEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.6F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.1F);
    }

    /** 注入 {@code AxolotlAi#getSpeedModifier} 尾部：普通游动速度改写，并叠加 SPEEDSTER 附魔加成。 */
    @Inject(
            method = {"getSpeedModifier(Lnet/minecraft/world/entity/LivingEntity;)F"},
            remap = true,
            at = @At(
                    value = "TAIL"
            ),
            cancellable = true
    )
    private static void di_getSpeedModifier(LivingEntity axolotl, CallbackInfoReturnable<Float> cir) {
        int speedsterLevel = TameableUtils.getEnchantLevel(axolotl, PHEnchantmentRegistry.SPEEDSTER);
        cir.setReturnValue(axolotl.isInWaterOrBubble() ? 0.5F + speedsterLevel * 0.05F : 0.15F + speedsterLevel * 0.15F);
    }


}

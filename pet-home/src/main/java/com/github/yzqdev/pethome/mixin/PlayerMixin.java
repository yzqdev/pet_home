package com.github.yzqdev.pethome.mixin;

import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 玩家近战攻击友军保护：@Redirect {@code Player#attack} 里的 {@code isAlliedTo} 判定，
 * 使目标为自家宠物时同样视为友军——配合击退/扫击等逻辑，避免主人一拳打飞宠物。
 */
@Mixin(Player.class)
public class PlayerMixin {

  @Redirect(
      method = {
        "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V"
      },
      remap = true,
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/player/Player;isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z"))
  private boolean di_onSweepAttack_isAlliedTo(Player player, Entity entity) {
    return TameableUtils.isPetOf(player, entity) || player.isAlliedTo(entity);
  }
}

package com.github.yzqdev.pethome.compat.jade;

import com.github.yzqdev.pethome.PetHomeConfig;
import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.util.IComandableMob;
import com.github.yzqdev.pethome.util.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PetHomeComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(
      ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
    // Jade 按 uid 自动生成 plugin_pet_home.collar_tag 开关，玩家关闭后整段不显示
    if (!iPluginConfig.get(this)) {
      return;
    }

    if (entityAccessor.getEntity() instanceof LivingEntity livingEntity) {
      var enchants = TameableUtils.getEnchantDescriptions(livingEntity);

      // 对齐 26.1/26.3：驯服宠物 + 三态指令系统开启时，显示漫游/停留/跟随状态
      if (livingEntity instanceof IComandableMob cmd
          && TameableUtils.isTamed(livingEntity)
          && PetHomeConfig.trinaryCommandSystem) {
        iTooltip.add(
            Component.translatable(
                    "message.pet_home.command_" + cmd.getCommand(), livingEntity.getDisplayName())
                .withStyle(ChatFormatting.YELLOW));
      }

      var hasPetbed = TameableUtils.getPetBedPos(livingEntity);
      if (hasPetbed != null) {
        iTooltip.add(
            Component.translatable(LangDefinition.has_pet_bed_at_pos, hasPetbed.toShortString())
                .withStyle(ChatFormatting.RED));
      }
      if (enchants.size() > 1) {
        for (Component enchant : enchants) {
          iTooltip.add(enchant);
        }
      }
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag");
  }
}

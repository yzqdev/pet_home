package com.github.yzqdev.pethome.compat.jade;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.datagen.LangDefinition;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade 实体悬浮提示组件（枚举单例）：显示宠物的宠物床绑定位置与项圈附魔列表
 * （读本地同步的 citadel 数据，与 {@code PetInfoHudOverlay} 同源）。
 */
public enum PetHomeComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {

    if (entityAccessor.getEntity() instanceof LivingEntity livingEntity){
      // 附魔列表首项为实体名标题，size>1 才代表存在实际附魔条目
      var enchants=TameableUtils.getEnchantDescriptions(livingEntity);
      var hasPetbed=TameableUtils.getPetBedPos(livingEntity);
      if (hasPetbed!=null){
        iTooltip.add(Component.translatable(LangDefinition.has_pet_bed_at_pos, hasPetbed.toShortString()).withStyle(ChatFormatting.RED));
      }
     if (enchants.size()>1){
       for (Component enchant : enchants) {
         iTooltip.add(enchant);
       }
     }

    }

  }


  @Override
  public ResourceLocation getUid() {
    return new ResourceLocation(PetHomeMod.MODID, "collar_tag");
  }
}
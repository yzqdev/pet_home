package com.github.yzqdev.pethome.compat.jade;

import com.github.yzqdev.pethome.PetHomeMod;
import com.github.yzqdev.pethome.server.entity.TameableUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public enum PetHomeComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  @Override
  public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {

    if (entityAccessor.getEntity() instanceof LivingEntity livingEntity){
      var enchants=TameableUtils.getEnchantDescriptions(livingEntity);
      var hasPetbed=TameableUtils.getPetBedPos(livingEntity);
      if (hasPetbed!=null){
        iTooltip.add(Component.literal("已绑定宠物床("+hasPetbed.toShortString()+")").withStyle(ChatFormatting.RED));
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
    return ResourceLocation.fromNamespaceAndPath(PetHomeMod.MODID, "collar_tag");
  }
}
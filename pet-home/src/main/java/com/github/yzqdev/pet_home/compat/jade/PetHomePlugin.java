package com.github.yzqdev.pet_home.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PetHomePlugin implements IWailaPlugin {

  @Override
  public void register(IWailaCommonRegistration registration) {
    IWailaPlugin.super.register(registration);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerEntityComponent(PetHomeComponentProvider.INSTANCE, LivingEntity.class);
  }
}
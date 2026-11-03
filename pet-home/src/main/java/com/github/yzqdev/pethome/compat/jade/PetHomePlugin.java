package com.github.yzqdev.pethome.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade 插件入口（{@code @WailaPlugin}）：给全部 LivingEntity 注册宠物提示组件（可选依赖 Jade）。
 */
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
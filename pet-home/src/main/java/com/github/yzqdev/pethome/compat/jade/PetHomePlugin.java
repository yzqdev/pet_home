package com.github.yzqdev.pethome.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade（What The Am I Looking At）插件入口（{@code fabric.mod.json} 的 {@code jade} entrypoint，软依赖）。
 * 仅客户端注册：给所有 {@code LivingEntity} 挂 {@link PetHomeComponentProvider}，在其悬浮提示中补充 宠物床坐标与项圈附魔；服务端侧
 * {@code register} 无需实现。
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

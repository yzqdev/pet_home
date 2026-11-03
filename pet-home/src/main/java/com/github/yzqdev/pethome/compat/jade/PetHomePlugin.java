package com.github.yzqdev.pethome.compat.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade（实体悬浮提示）联动插件入口：服务端侧无额外注册，客户端侧把
 * {@link PetHomeComponentProvider} 挂到所有 {@code LivingEntity} 上。
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
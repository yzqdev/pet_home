package com.github.yzqdev.pethome.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.Mod;

/** 专用服务端代理：S2C 客户端专属处理（写本地实体数据槽）在此为空实现。 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerProxy {
    public void handleEntityTagUpdate(CompoundTag tag, int entityID) {
    }

}
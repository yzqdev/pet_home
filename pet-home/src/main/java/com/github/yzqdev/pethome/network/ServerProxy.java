package com.github.yzqdev.pethome.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerProxy {
    public void handlePropertiesPacket(String propertyID, CompoundTag compound, int entityID) {
    }

}
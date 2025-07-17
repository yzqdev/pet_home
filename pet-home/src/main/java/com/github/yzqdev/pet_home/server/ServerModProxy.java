package com.github.yzqdev.pet_home.server;

import com.github.yzqdev.pet_home.PetHomeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
@EventBusSubscriber(modid = PetHomeMod.MODID,bus = EventBusSubscriber.Bus.MOD)
public class ServerModProxy {
    @SubscribeEvent
    public static void fmlLoad(final FMLCommonSetupEvent event){

    }
}

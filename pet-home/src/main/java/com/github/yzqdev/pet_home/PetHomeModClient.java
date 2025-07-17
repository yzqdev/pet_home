package com.github.yzqdev.pet_home;

import com.github.yzqdev.pet_home.client.ClientProxy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PetHomeMod.MODID, dist = Dist.CLIENT)
public class PetHomeModClient {
    public PetHomeModClient(IEventBus modEventBus, ModContainer modContainer) {
        ClientProxy.registerClientListeners(modEventBus);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, modListScreen) -> new ConfigurationScreen(container, modListScreen, ModConfigScreen::new));
    }


}

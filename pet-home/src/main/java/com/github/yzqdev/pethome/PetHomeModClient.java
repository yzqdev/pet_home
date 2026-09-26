package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = PetHomeMod.MODID, dist = Dist.CLIENT)
public class PetHomeModClient {
    public PetHomeModClient(IEventBus modEventBus, ModContainer modContainer) {
        ClientGameEvents.registerClientListeners(modEventBus);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, modListScreen) -> new ConfigurationScreen(container, modListScreen, ModConfigScreen::new));
    }


}

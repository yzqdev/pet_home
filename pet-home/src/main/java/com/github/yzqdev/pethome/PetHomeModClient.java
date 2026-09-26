package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.client.PetInfoHudOverlay;
import com.github.yzqdev.pethome.client.EntityRegEvent;
import com.github.yzqdev.pethome.client.gui.PetHomeConfigCommand;
import com.github.yzqdev.pethome.network.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class PetHomeModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Networking.registerClientReceivers();

		ClientGameEvents.registerClientListeners();

		PetInfoHudOverlay.init();

		EntityRegEvent.registerEntityRender();
		EntityRegEvent.registerLayer();
		EntityRegEvent.registerTooltipComponents();
		EntityRegEvent.registerItemTooltips();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				PetHomeConfigCommand.register(dispatcher));
	}
}

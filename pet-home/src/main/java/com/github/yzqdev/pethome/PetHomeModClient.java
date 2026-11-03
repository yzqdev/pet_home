package com.github.yzqdev.pethome;

import com.github.yzqdev.pethome.client.ClientGameEvents;
import com.github.yzqdev.pethome.client.PetInfoHudOverlay;
import com.github.yzqdev.pethome.client.EntityRegEvent;
import com.github.yzqdev.pethome.client.gui.PetHomeConfigCommand;
import com.github.yzqdev.pethome.network.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/**
 * 客户端入口（{@code ClientModInitializer}）。只做注册接线：
 * 客户端包接收器 → 粒子 provider → 宠物信息 HUD → 实体渲染器/叠加层/tooltip → {@code /pet_home_config} 客户端命令。
 */
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

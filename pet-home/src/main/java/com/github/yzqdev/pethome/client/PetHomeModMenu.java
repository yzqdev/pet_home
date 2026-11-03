package com.github.yzqdev.pethome.client;

import com.github.yzqdev.pethome.client.gui.PetHomeConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


/**
 * ModMenu 集成入口：把「配置」按钮指向 {@link PetHomeConfigScreen}。
 */
@Environment(EnvType.CLIENT)
public class PetHomeModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PetHomeConfigScreen::new;
    }
}

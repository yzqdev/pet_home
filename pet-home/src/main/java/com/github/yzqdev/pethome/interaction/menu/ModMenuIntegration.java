package com.github.yzqdev.pethome.interaction.menu;

import com.github.yzqdev.pethome.client.gui.PetHomeConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** ModMenu 联动入口（fabric.mod.json 的 {@code modmenu} entrypoint）： 把模组配置屏幕挂到 ModMenu 的"配置"按钮上。 */
@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return PetHomeConfigScreen::new;
  }
}

package com.matyrobbrt.keybindbundles.compat;

import com.matyrobbrt.keybindbundles.config.KBClothConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return KBClothConfigScreen::create;
    }
}

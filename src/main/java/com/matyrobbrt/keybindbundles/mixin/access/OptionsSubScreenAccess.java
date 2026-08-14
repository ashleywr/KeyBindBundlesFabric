package com.matyrobbrt.keybindbundles.mixin.access;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenAccess {
    @Accessor("lastScreen")
    Screen kbb$getLastScreen();
}

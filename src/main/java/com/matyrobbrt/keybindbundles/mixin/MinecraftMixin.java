package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "setScreen")
    private void restoreBundleKeys(@Nullable Screen screen, CallbackInfo ci) {
        if (screen != null) {
            KeybindSelectionOverlay.INSTANCE.discard();
            KeyMappingUtil.restoreAll();
        }
    }
}

package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(at = @At("HEAD"), method = "onPress", cancellable = true)
    private void handleKeybindBundleClick(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == GLFW.GLFW_RELEASE) {
            KeyMappingUtil.clearSuppressedPhysicalKey(InputConstants.Type.MOUSE.getOrCreate(button));
        }

        var minecraft = Minecraft.getInstance();
        if (KeybindSelectionOverlay.INSTANCE.getDisplayedKeybind() != null && minecraft.screen == null && button <= GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            var mouse = minecraft.mouseHandler;
            double mouseX = mouse.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth();
            double mouseY = mouse.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight();

            KeybindSelectionOverlay.INSTANCE.mouseClick(mouseX, mouseY, button, action);
            ci.cancel();
        }
    }
}

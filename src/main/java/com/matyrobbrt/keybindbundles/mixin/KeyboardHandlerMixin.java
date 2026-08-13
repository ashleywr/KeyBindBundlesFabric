package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(at = @At("HEAD"), method = "keyPress")
    private void clearConsumedBundleKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_RELEASE && key > 0) {
            KeyMappingUtil.clearSuppressedPhysicalKey(InputConstants.getKey(key, -1));
        }
    }
}

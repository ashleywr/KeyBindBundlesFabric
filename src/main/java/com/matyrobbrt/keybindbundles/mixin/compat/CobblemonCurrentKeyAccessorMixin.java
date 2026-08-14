package com.matyrobbrt.keybindbundles.mixin.compat;

import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.cobblemon.mod.common.client.keybind.CurrentKeyAccessorKt")
public class CobblemonCurrentKeyAccessorMixin {
    // Cobblemon renders some prompts from its raw bound-key helper instead of KeyMapping text.
    // If the action is intentionally unbound because it lives in a bundle, show the bundle key.
    @Inject(at = @At("HEAD"), method = "boundKey", cancellable = true, remap = false)
    private static void getBundleAwareBoundKey(KeyMapping mapping, CallbackInfoReturnable<InputConstants.Key> cir) {
        var bundleKey = KeyMappingUtil.bundleAwareBoundKey(mapping);
        if (bundleKey != null) {
            cir.setReturnValue(bundleKey);
        }
    }
}

package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.ModKeyBindBundles;
import com.matyrobbrt.keybindbundles.PriorityKeyMapping;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import com.matyrobbrt.keybindbundles.ii.KeyMappingExtension;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantValue")
@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingExtension {

    @Shadow
    public static void resetMapping() {
    }

    @Shadow
    private InputConstants.Key key;

    @Shadow
    private int clickCount;

    @Unique
    @Nullable
    private InputConstants.Key previousKey;

    @Shadow
    @Final
    private String name;

    @Inject(at = @At("HEAD"), method = "set", cancellable = true)
    private static void suppressConsumedBundleKey(InputConstants.Key key, boolean isDown, CallbackInfo ci) {
        if (!KeyMappingUtil.shouldSuppressPhysicalKey(key)) {
            return;
        }

        if (isDown) {
            ci.cancel();
        } else {
            KeyMappingUtil.clearSuppressedPhysicalKey(key);
        }
    }

    @Inject(at = @At("HEAD"), method = "compareTo", cancellable = true)
    private void priorityCompare(KeyMapping other, CallbackInfoReturnable<Integer> cir) {
        if (other instanceof PriorityKeyMapping && !(((Object)this) instanceof PriorityKeyMapping)) {
            cir.setReturnValue(1);
        }
    }

    @Inject(at = @At("HEAD"), method = "saveString", cancellable = true)
    private void sakeCorrectKey(CallbackInfoReturnable<String> cir) {
        // This is to make sure that we don't save any key as being bound to the bundle trigger
        // if it happens to be saved while it's being held down and captured by us
        if (key == ModKeyBindBundles.BUNDLE_TRIGGER_KEY && previousKey != null) {
            cir.setReturnValue(previousKey.getName());
        }
    }

    @Override
    public void takeOverForBundle() {
        previousKey = key;
        key = ModKeyBindBundles.BUNDLE_TRIGGER_KEY;
        resetMapping();
    }

    @Override
    public void restoreToOriginalKey() {
        if (previousKey != null) {
            key = previousKey;
            previousKey = null;
            resetMapping();
        }
    }

    @Override
    public void incrementClickCount() {
        clickCount++;
    }

    @Override
    public void kbb$unregister() {
        key = InputConstants.UNKNOWN;
        KeyMapping.ALL.remove(name);
        resetMapping();
    }
}

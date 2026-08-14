package com.matyrobbrt.keybindbundles;

import com.matyrobbrt.keybindbundles.ii.KeyMappingExtension;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.KeybindResolver;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KeyMappingUtil {
    // Some mods, including Cobblemon's party throw binding, do their work when a key is released
    // after observing it down during a client tick. Keep simulated taps alive long enough for that.
    private static final int TAP_RELEASE_DELAY_TICKS = 2;

    @Nullable
    public static KeyMapping getByName(String name) {
        return KeyMapping.ALL.get(name);
    }

    private static final Minecraft MC = Minecraft.getInstance();
    public static final List<KeyMapping> KEYS_TAKEN_OVER = new ArrayList<>();
    private static final Set<String> SUPPRESSED_PHYSICAL_KEYS = ConcurrentHashMap.newKeySet();
    private static final List<PendingRelease> PENDING_RELEASES = new ArrayList<>();

    /**
     * This method emulates a press of the keymapping and handles special cases
     * like the fullscreen and screenshot buttons (see {@link KeyboardHandler#keyPress(long, int, int, int, int)}).
     */
    public static void press(KeyMapping mapping) {
        if (mapping == MC.options.keyFullscreen) {
            MC.getWindow().toggleFullScreen();
            MC.options.fullscreen().set(MC.getWindow().isFullscreen());
            return;
        } else if (mapping == MC.options.keyScreenshot) {
            Screenshot.grab(
                    MC.gameDirectory,
                    MC.getMainRenderTarget(),
                    message -> MC.execute(() -> MC.gui.getChat().addMessage(message))
            );
            return;
        }

        takeOver(mapping);
        try {
            sendSyntheticKeyPress(GLFW.GLFW_PRESS);
        } finally {
            ((KeyMappingExtension) mapping).restoreToOriginalKey();
        }
    }

    public static void click(KeyMapping map) {
        ((KeyMappingExtension) map).incrementClickCount();
    }

    public static void tap(KeyMapping mapping) {
        press(mapping);
        if (KEYS_TAKEN_OVER.contains(mapping)) {
            scheduleRelease(mapping);
        }
    }

    private static void scheduleRelease(KeyMapping mapping) {
        PENDING_RELEASES.add(new PendingRelease(mapping, TAP_RELEASE_DELAY_TICKS));
    }

    public static void release(KeyMapping map) {
        if (!KEYS_TAKEN_OVER.contains(map)) {
            return;
        }

        ((KeyMappingExtension) map).restoreToOriginalKey();
        if (map.isDown()) map.setDown(false);
        KEYS_TAKEN_OVER.remove(map);
    }

    public static void restoreAll() {
        if (!KEYS_TAKEN_OVER.isEmpty()) {
            for (KeyMapping keyMapping : KEYS_TAKEN_OVER) {
                ((KeyMappingExtension) keyMapping).restoreToOriginalKey();
                if (keyMapping.isDown()) keyMapping.setDown(false);
            }
            KEYS_TAKEN_OVER.clear();
        }
        PENDING_RELEASES.clear();
    }

    public static void tickScheduledReleases() {
        for (int i = PENDING_RELEASES.size() - 1; i >= 0; i--) {
            var pending = PENDING_RELEASES.get(i);
            pending = pending.ticked();
            if (pending.ready()) {
                release(pending.mapping());
                PENDING_RELEASES.remove(i);
            } else {
                PENDING_RELEASES.set(i, pending);
            }
        }
    }

    public static void suppressPhysicalKeyUntilRelease(KeyMapping mapping) {
        var keyName = mapping.saveString();
        if (keyName.equals(InputConstants.UNKNOWN.getName())) {
            return;
        }

        SUPPRESSED_PHYSICAL_KEYS.add(keyName);
        releaseOtherMappingsWithKey(keyName, mapping);
        Minecraft.getInstance().execute(() -> releaseOtherMappingsWithKey(keyName, mapping));
    }

    public static boolean shouldSuppressPhysicalKey(InputConstants.Key key) {
        return SUPPRESSED_PHYSICAL_KEYS.contains(key.getName());
    }

    public static void clearSuppressedPhysicalKey(InputConstants.Key key) {
        SUPPRESSED_PHYSICAL_KEYS.remove(key.getName());
    }

    private static void releaseOtherMappingsWithKey(String keyName, KeyMapping except) {
        for (KeyMapping mapping : KeyMapping.ALL.values()) {
            if (mapping != except && !KEYS_TAKEN_OVER.contains(mapping) && mapping.saveString().equals(keyName)) {
                mapping.setDown(false);
            }
        }
    }

    private static void takeOver(KeyMapping mapping) {
        ((KeyMappingExtension) mapping).takeOverForBundle();
        if (!KEYS_TAKEN_OVER.contains(mapping)) {
            KEYS_TAKEN_OVER.add(mapping);
        }
    }

    private static void sendSyntheticKeyPress(int action) {
        MC.keyboardHandler.keyPress(MC.getWindow().getWindow(), ModKeyBindBundles.SPECIAL_KEY_CODE, -1, action, 0);
    }

    public static Component displayName(KeyMapping mapping) {
        if (mapping instanceof KeyBindBundleManager.RadialKeyMapping radial) {
            return radial.getDisplayName();
        }
        return Component.translatable(mapping.getName());
    }

    @Nullable
    public static Component bundleAwareTranslatedKeyMessage(KeyMapping mapping) {
        if (!mapping.isUnbound()) {
            return null;
        }

        var bundleMapping = KeyBindBundleManager.findFirstBundleContaining(mapping.getName());
        if (bundleMapping != null && !bundleMapping.isUnbound()) {
            return bundleMapping.getTranslatedKeyMessage();
        }
        return null;
    }

    @Nullable
    public static InputConstants.Key bundleAwareBoundKey(KeyMapping mapping) {
        if (!mapping.isUnbound()) {
            return null;
        }

        var bundleMapping = KeyBindBundleManager.findFirstBundleContaining(mapping.getName());
        if (bundleMapping != null && !bundleMapping.isUnbound()) {
            return InputConstants.getKey(bundleMapping.saveString());
        }
        return null;
    }

    public static void registerBundleAwareKeybindResolver() {
        KeybindResolver.setKeyResolver(keyName -> () -> {
            var mapping = getByName(keyName);
            if (mapping != null) {
                var bundleKeyMessage = bundleAwareTranslatedKeyMessage(mapping);
                if (bundleKeyMessage != null) return bundleKeyMessage;
            }

            return KeyMapping.createNameSupplier(keyName).get();
        });
    }

    private record PendingRelease(KeyMapping mapping, int ticks) {
        private boolean ready() {
            return ticks <= 0;
        }

        private PendingRelease ticked() {
            return new PendingRelease(mapping, ticks - 1);
        }
    }
}

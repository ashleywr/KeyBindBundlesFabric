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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KeyMappingUtil {
    @Nullable
    public static KeyMapping getByName(String name) {
        return KeyMapping.ALL.get(name);
    }

    private static final Minecraft MC = Minecraft.getInstance();
    public static final List<KeyMapping> KEYS_TAKEN_OVER = new ArrayList<>();
    private static final Set<String> SUPPRESSED_PHYSICAL_KEYS = ConcurrentHashMap.newKeySet();

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

        ((KeyMappingExtension) mapping).takeOverForBundle();
        KEYS_TAKEN_OVER.add(mapping);
        mapping.setDown(true);
    }

    public static void click(KeyMapping map) {
        ((KeyMappingExtension) map).incrementClickCount();
    }

    public static void release(KeyMapping map) {
        map.setDown(false);

        ((KeyMappingExtension) map).restoreToOriginalKey();
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

    public static Component displayName(KeyMapping mapping) {
        if (mapping instanceof KeyBindBundleManager.RadialKeyMapping radial) {
            return radial.getDisplayName();
        }
        return Component.translatable(mapping.getName());
    }

    public static void registerBundleAwareKeybindResolver() {
        KeybindResolver.setKeyResolver(keyName -> () -> {
            var mapping = getByName(keyName);
            if (mapping != null && mapping.isUnbound()) {
                var bundleMapping = KeyBindBundleManager.findFirstBundleContaining(keyName);
                if (bundleMapping != null && !bundleMapping.isUnbound()) {
                    return bundleMapping.getTranslatedKeyMessage();
                }
            }

            return KeyMapping.createNameSupplier(keyName).get();
        });
    }
}

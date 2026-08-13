package com.matyrobbrt.keybindbundles;

import com.matyrobbrt.keybindbundles.ii.KeyMappingExtension;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KeyMappingUtil {
    @Nullable
    public static KeyMapping getByName(String name) {
        return KeyMapping.ALL.get(name);
    }

    private static final Minecraft MC = Minecraft.getInstance();
    public static final List<KeyMapping> KEYS_TAKEN_OVER = new ArrayList<>();

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

    public static Component displayName(KeyMapping mapping) {
        if (mapping instanceof KeyBindBundleManager.RadialKeyMapping radial) {
            return radial.getDisplayName();
        }
        return Component.translatable(mapping.getName());
    }
}

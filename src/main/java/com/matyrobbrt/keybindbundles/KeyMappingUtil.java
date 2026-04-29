package com.matyrobbrt.keybindbundles;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

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
     * This method emulates a press of the keymapping, firing the {@link InputEvent.Key key input event},
     * and handling special snowflakes like the fullscreen and screenshot buttons (see {@link KeyboardHandler#keyPress(long, int, int, int, int)})
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
                    message -> MC.execute(() -> {
                        MC.gui.getChat().addClientSystemMessage(message);
                        MC.getNarrator().saySystemQueued(message);
                    })
            );
            return;
        }

        mapping.takeOverForBundle();
        KEYS_TAKEN_OVER.add(mapping);
        mapping.setDown(true);

        ClientHooks.onKeyInput(
                new KeyEvent(
                        ModKeyBindBundles.BUNDLE_TRIGGER_KEY.getValue(), 0, 0
                ), GLFW.GLFW_PRESS
        );
    }

    public static void click(KeyMapping map) {
        map.incrementClickCount();
    }

    public static void release(KeyMapping map) {
        map.setDown(false);
        ClientHooks.onKeyInput(
                new KeyEvent(
                        ModKeyBindBundles.BUNDLE_TRIGGER_KEY.getValue(), 0, 0
                ), GLFW.GLFW_RELEASE
        );

        map.restoreToOriginalKey();
        KEYS_TAKEN_OVER.remove(map);
    }

    public static void restoreAll() {
        if (!KEYS_TAKEN_OVER.isEmpty()) {
            for (KeyMapping keyMapping : KEYS_TAKEN_OVER) {
                keyMapping.restoreToOriginalKey();
                if (keyMapping.isDown()) keyMapping.setDown(false);
            }
            KEYS_TAKEN_OVER.clear();
        }
    }

    public static boolean isShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}

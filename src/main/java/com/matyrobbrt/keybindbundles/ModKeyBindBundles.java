package com.matyrobbrt.keybindbundles;

import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.matyrobbrt.keybindbundles.util.SearchTreeManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindBundles implements ClientModInitializer {
    public static final String MOD_ID = "keybindbundles";
    public static final KeyMapping OPEN_RADIAL_MENU_MAPPING = new PriorityKeyMapping(
            "key.keybindbundles.open_radial_menu",
            GLFW.GLFW_KEY_LEFT_ALT,
            "category.keybindbundles"
    ) {
        @Override
        public int compareTo(KeyMapping map) {
            return map instanceof KeyBindBundleManager.RadialKeyMapping ? -1 : super.compareTo(map);
        }
    };

    public static final KeyMapping OPEN_SCREEN_MAPPING = new PriorityKeyMapping(
            "key.keybindbundles.open_screen",
            GLFW.GLFW_KEY_UNKNOWN,
            "category.keybindbundles"
    ) {
        @Override
        public void setDown(boolean value) {
            if (isDown() && !value && Minecraft.getInstance().screen == null) {
                super.setDown(false);
                Minecraft.getInstance().setScreen(new KeyBindsScreen(new Screen(Component.empty()) {
                    @Override
                    protected void init() {
                        Minecraft.getInstance().setScreen(null);
                    }
                }, Minecraft.getInstance().options));
            } else {
                super.setDown(value);
            }
        }

        @Override
        public int compareTo(KeyMapping map) {
            return map instanceof KeyBindBundleManager.RadialKeyMapping ? -1 : super.compareTo(map);
        }
    };

    // Random number chosen by fair dice roll. Pray mods get along with keys that don't exist
    public static final int SPECIAL_KEY_CODE = 22745;

    // A random key constant we use to simulate our presses when mimicking InputEvent.Key
    public static final InputConstants.Key BUNDLE_TRIGGER_KEY = InputConstants.getKey(SPECIAL_KEY_CODE, -1);

    @Override
    public void onInitializeClient() {
        KBClientConfig.load();

        KeyBindingHelper.registerKeyBinding(OPEN_RADIAL_MENU_MAPPING);
        KeyBindingHelper.registerKeyBinding(OPEN_SCREEN_MAPPING);
        HudRenderCallback.EVENT.register((graphics, deltaTracker) -> KeybindSelectionOverlay.INSTANCE.render(graphics, deltaTracker));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> SearchTreeManager.onPlayerJoin());
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> KeyBindBundleManager.load());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> KeyBindBundleManager.write());
    }
}

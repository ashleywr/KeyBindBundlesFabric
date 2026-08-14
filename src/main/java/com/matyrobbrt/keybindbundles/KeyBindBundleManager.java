package com.matyrobbrt.keybindbundles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.matyrobbrt.keybindbundles.ii.KeyMappingExtension;
import com.matyrobbrt.keybindbundles.render.KeybindSelectionOverlay;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class KeyBindBundleManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PATH = FabricLoader.getInstance().getGameDir().resolve("keybind_bundles.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<KeyBindBundle> keybinds;
    private static List<RadialKeyMapping> keyMappings;

    public static void load() {
        keybinds = new ArrayList<>();
        keyMappings = new ArrayList<>();

        try (var is = Files.newBufferedReader(PATH)) {
            var element = GSON.fromJson(is, JsonArray.class);
            if (element == null) {
                throw new JsonParseException("Bundle file is empty");
            }

            keybinds.addAll(KeyBindBundle.LIST_CODEC.decode(JsonOps.INSTANCE, element)
                    .getOrThrow().getFirst());

            var savedKeyMappings = readSavedKeyMappings();
            for (int i = 0; i < keybinds.size(); i++) {
                var mapping = keybinds.get(i).createMapping();
                applySavedKey(mapping, savedKeyMappings);
                keyMappings.add(mapping);
            }

            var options = Minecraft.getInstance().options;
            for (int i = 0; i < options.keyMappings.length; i++) {
                if (options.keyMappings[i] == ModKeyBindBundles.OPEN_SCREEN_MAPPING) {
                    options.keyMappings = ArrayUtils.insert(i + 1, options.keyMappings, keyMappings.toArray(KeyMapping[]::new));
                    KeyMapping.resetMapping();
                    break;
                }
            }
        } catch (NoSuchFileException ignore) {

        } catch (IOException ex) {
            LOGGER.error("Error reading file from {}: ", PATH, ex);
        } catch (JsonParseException | IllegalStateException ex) {
            LOGGER.error("Error parsing file from {}. Bundles will be left empty for this session: ", PATH, ex);
            keybinds.clear();
            keyMappings.clear();
        }
    }

    private static Map<String, InputConstants.Key> readSavedKeyMappings() {
        var optionsPath = FabricLoader.getInstance().getGameDir().resolve("options.txt");
        if (!Files.exists(optionsPath)) {
            return Map.of();
        }

        var keys = new HashMap<String, InputConstants.Key>();
        try (var lines = Files.lines(optionsPath)) {
            lines.forEach(line -> {
                if (!line.startsWith("key_")) {
                    return;
                }

                int separator = line.indexOf(':');
                if (separator <= "key_".length()) {
                    return;
                }

                var mappingName = line.substring("key_".length(), separator);
                var savedKey = line.substring(separator + 1);
                try {
                    keys.put(mappingName, InputConstants.getKey(savedKey));
                } catch (RuntimeException ex) {
                    LOGGER.warn("Ignoring invalid saved key '{}' for mapping '{}'.", savedKey, mappingName);
                }
            });
        } catch (IOException ex) {
            LOGGER.warn("Unable to read saved key mappings from {}: ", optionsPath, ex);
        }

        return keys;
    }

    private static void applySavedKey(KeyMapping mapping, Map<String, InputConstants.Key> savedKeyMappings) {
        var savedKey = savedKeyMappings.get(mapping.getName());
        if (savedKey != null) {
            mapping.setKey(savedKey);
        }
    }

    private static KeyMapping getLastKeyMapping() {
        return keyMappings.isEmpty() ? ModKeyBindBundles.OPEN_SCREEN_MAPPING : keyMappings.getLast();
    }

    public static KeyMapping add(KeyBindBundle bind) {
        keybinds.add(bind);

        var options = Minecraft.getInstance().options;
        var compareKeybind = getLastKeyMapping();
        var mapping = bind.createMapping();
        keyMappings.add(mapping);
        for (int i = 0; i < options.keyMappings.length; i++) {
            if (options.keyMappings[i] == compareKeybind) {
                options.keyMappings = ArrayUtils.insert(i + 1, options.keyMappings, mapping);
                break;
            }
        }

        write();
        return mapping;
    }

    public static void delete(KeyBindBundle bind) {
        var idx = keybinds.indexOf(bind);
        if (idx >= 0) {
            keybinds.remove(idx);
            var map = keyMappings.remove(idx);

            if (map != null) {
                var options = Minecraft.getInstance().options;

                options.keyMappings = ArrayUtils.removeElement(options.keyMappings, map);
                ((KeyMappingExtension) map).kbb$unregister();
            }

            write();
        }
    }

    public static void write() {
        if (keybinds == null) {
            return;
        }

        try {
            var out = KeyBindBundle.LIST_CODEC
                    .encodeStart(JsonOps.INSTANCE, keybinds)
                    .getOrThrow();
            Files.writeString(PATH, GSON.toJson(out));
        } catch (IOException ex) {
            LOGGER.error("Error writing to file {}: ", PATH, ex);
        }
    }

    public static List<RadialKeyMapping> getKeys() {
        return keyMappings;
    }

    @Nullable
    public static RadialKeyMapping findFirstBundleContaining(String keyName) {
        if (keyMappings == null) {
            return null;
        }

        for (RadialKeyMapping mapping : keyMappings) {
            if (mapping.bind.getEntries().stream().anyMatch(entry -> entry.key().equals(keyName))) {
                return mapping;
            }
        }
        return null;
    }

    public static class RadialKeyMapping extends PriorityKeyMapping {
        public final KeyBindBundle bind;
        private final Component name;
        public RadialKeyMapping(String name, int keyCode, String category, KeyBindBundle bind) {
            super(name, keyCode, category);
            this.bind = bind;
            this.name = Component.translatable("key.keybindbundles.bundle", Component.literal(bind.name).withStyle(ChatFormatting.GOLD));
        }

        public Component getDisplayName() {
            return name;
        }

        private KeyMapping currentlyPressing;

        @Override
        public void setDown(boolean value) {
            if (this.isDown()) {
                if (value) {
                    click();
                } else {
                    onRelease();
                    super.setDown(false);
                }
            } else if (!this.isDown() && value) {
                onClick();
                super.setDown(true);
            }
        }

        private void onClick() {
            KeyMappingUtil.suppressPhysicalKeyUntilRelease(this);

            if (!opensRadial()) {
                var entry = bind.getBookmarked();
                if (entry != null) {
                    var key = KeyMappingUtil.getByName(entry.key());
                    if (key != null) {
                        setAndPress(key);
                        return;
                    }
                }
            }

            if (!bind.getEntries().isEmpty()) {
                KeybindSelectionOverlay.INSTANCE.open(this);
                Minecraft.getInstance().mouseHandler.releaseMouse();
            }
        }

        public void setAndPress(KeyMapping mapping) {
            currentlyPressing = mapping;
            KeyMappingUtil.press(mapping);
            click();
        }

        private void click() {
            if (currentlyPressing != null) {
                KeyMappingUtil.click(currentlyPressing);
            }
        }

        private void onRelease() {
            if (currentlyPressing != null) {
                KeyMappingUtil.release(currentlyPressing);
                currentlyPressing = null;
            }

            if (KeybindSelectionOverlay.INSTANCE.getDisplayedMapping() == this) {
                KeybindSelectionOverlay.INSTANCE.close();

                var mouse = Minecraft.getInstance().mouseHandler;
                if (!mouse.isMouseGrabbed() && Minecraft.getInstance().screen == null) {
                    mouse.grabMouse();
                }
            }
        }

        private boolean opensRadial() {
            return ModKeyBindBundles.OPEN_RADIAL_MENU_MAPPING.isDown();
        }

        @Override
        public int compareTo(KeyMapping map) {
            if (map == ModKeyBindBundles.OPEN_SCREEN_MAPPING || map == ModKeyBindBundles.OPEN_RADIAL_MENU_MAPPING) return 1;
            return super.compareTo(map);
        }
    }
}

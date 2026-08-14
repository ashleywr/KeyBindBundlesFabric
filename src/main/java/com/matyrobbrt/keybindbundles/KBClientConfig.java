package com.matyrobbrt.keybindbundles;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class KBClientConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(ModKeyBindBundles.MOD_ID + "-client.properties");

    public static final BooleanValue CLIP_MOUSE_TO_MENU = new BooleanValue("clipMouseToMenu", false);
    public static final BooleanValue TRIGGER_KEYMAPPING_ON_RELEASE = new BooleanValue("triggerKeymappingOnRelease", false);
    public static final BooleanValue IGNORE_INVALID_KEY_CHECKS = new BooleanValue("ignoreInvalidKeyChecks", false);

    public static void load() {
        var properties = new Properties();
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                properties.load(reader);
            } catch (IOException ex) {
                LOGGER.error("Error reading config file {}: ", PATH, ex);
            }
        }

        CLIP_MOUSE_TO_MENU.read(properties);
        TRIGGER_KEYMAPPING_ON_RELEASE.read(properties);
        IGNORE_INVALID_KEY_CHECKS.read(properties);
        save();
    }

    public static void save() {
        var properties = new Properties();
        CLIP_MOUSE_TO_MENU.write(properties);
        TRIGGER_KEYMAPPING_ON_RELEASE.write(properties);
        IGNORE_INVALID_KEY_CHECKS.write(properties);

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                properties.store(writer, "KeyBind Bundles client config");
            }
        } catch (IOException ex) {
            LOGGER.error("Error writing config file {}: ", PATH, ex);
        }
    }

    public static class BooleanValue {
        private final String key;
        private final boolean defaultValue;
        private boolean value;

        private BooleanValue(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.value = defaultValue;
        }

        public boolean getAsBoolean() {
            return value;
        }

        public boolean getDefaultValue() {
            return defaultValue;
        }

        public void set(boolean value) {
            this.value = value;
        }

        private void read(Properties properties) {
            value = Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defaultValue)));
        }

        private void write(Properties properties) {
            properties.setProperty(key, Boolean.toString(value));
        }
    }
}

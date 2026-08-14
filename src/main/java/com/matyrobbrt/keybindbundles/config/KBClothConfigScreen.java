package com.matyrobbrt.keybindbundles.config;

import com.matyrobbrt.keybindbundles.KBClientConfig;
import com.matyrobbrt.keybindbundles.ModKeyBindBundles;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class KBClothConfigScreen {
    private KBClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title." + ModKeyBindBundles.MOD_ID + ".config"));
        builder.setSavingRunnable(KBClientConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category." + ModKeyBindBundles.MOD_ID + ".configuration"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("keybindbundles.configuration.clipMouseToMenu"),
                        KBClientConfig.CLIP_MOUSE_TO_MENU.getAsBoolean()
                )
                .setDefaultValue(KBClientConfig.CLIP_MOUSE_TO_MENU.getDefaultValue())
                .setSaveConsumer(KBClientConfig.CLIP_MOUSE_TO_MENU::set)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("keybindbundles.configuration.triggerKeymappingOnRelease"),
                        KBClientConfig.TRIGGER_KEYMAPPING_ON_RELEASE.getAsBoolean()
                )
                .setDefaultValue(KBClientConfig.TRIGGER_KEYMAPPING_ON_RELEASE.getDefaultValue())
                .setSaveConsumer(KBClientConfig.TRIGGER_KEYMAPPING_ON_RELEASE::set)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("keybindbundles.configuration.stickyBundleSelection"),
                        KBClientConfig.STICKY_BUNDLE_SELECTION.getAsBoolean()
                )
                .setDefaultValue(KBClientConfig.STICKY_BUNDLE_SELECTION.getDefaultValue())
                .setSaveConsumer(KBClientConfig.STICKY_BUNDLE_SELECTION::set)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("keybindbundles.configuration.ignoreInvalidKeyChecks"),
                        KBClientConfig.IGNORE_INVALID_KEY_CHECKS.getAsBoolean()
                )
                .setDefaultValue(KBClientConfig.IGNORE_INVALID_KEY_CHECKS.getDefaultValue())
                .setSaveConsumer(KBClientConfig.IGNORE_INVALID_KEY_CHECKS::set)
                .build());

        return builder.build();
    }
}

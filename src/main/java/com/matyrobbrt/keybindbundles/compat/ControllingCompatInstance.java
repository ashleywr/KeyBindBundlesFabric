package com.matyrobbrt.keybindbundles.compat;

import com.blamejared.controlling.api.entries.IKeyEntry;
import com.blamejared.controlling.api.event.ControllingEvents;
import com.blamejared.controlling.api.event.KeyEntryListenersEvent;
import com.blamejared.controlling.client.CustomList;
import com.blamejared.controlling.client.NewKeyBindsList;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;

import java.util.List;
import java.util.function.Predicate;

public class ControllingCompatInstance implements ControllingCompat {
    public ControllingCompatInstance() {
        ControllingEvents.KEY_ENTRY_LISTENERS_EVENT.register(this::addCustomListeners);
    }

    private List<GuiEventListener> addCustomListeners(KeyEntryListenersEvent event) {
        if (event.getEntry() instanceof OverrideListenersEntry entry) {
            if (entry.doOverrideListeners()) {
                event.getListeners().clear();
            }
            event.getListeners().addAll(entry.getAdditionalListeners());
        }
        return event.getListeners();
    }

    @Override
    public void addChildren(KeyBindsList list, int index, KeyBindsList.Entry entry) {
        if (list instanceof CustomList cl) {
            cl.getAllEntries().add(index, entry);
        }
    }

    @Override
    public boolean testKey(KeyBindsList.Entry entry, Predicate<KeyMapping> test) {
        return entry instanceof IKeyEntry ke ? test.test(ke.getKey()) : (entry instanceof KeyBindsList.KeyEntry kk && test.test(kk.key));
    }

    @Override
    public KeyBindsList.Entry createEntry(KeyBindsList list, KeyMapping mapping) {
        if (list instanceof NewKeyBindsList nl) {
            return nl.new KeyEntry(mapping, KeyMappingUtil.displayName(mapping));
        }
        return ControllingCompat.super.createEntry(list, mapping);
    }
}

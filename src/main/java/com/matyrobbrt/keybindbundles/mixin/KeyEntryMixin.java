package com.matyrobbrt.keybindbundles.mixin;

import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(KeyBindsList.KeyEntry.class)
public class KeyEntryMixin extends BaseKeyEntryMixin {
    @Shadow
    @Final
    private Button changeButton;

    @Shadow
    @Final
    @Mutable
    private Component name;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void handleCustom(KeyBindsList owner, KeyMapping key, Component name, CallbackInfo ci) {
        if (key instanceof KeyBindBundleManager.RadialKeyMapping) {
            var customName = KeyMappingUtil.displayName(key);
            this.name = customName;
            name = customName;
        }

        kbb$handleCustom(key, name);
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(
            GuiGraphics guiGraphics,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovering,
            float partialTick,
            CallbackInfo ci
    ) {
        if (selectButton != null) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.name, left, top + height / 2 - 9 / 2, -1);
            kbb$positionSelectButton(left, width, top);
            selectButton.render(guiGraphics, mouseX, mouseY, partialTick);
            ci.cancel();
        } else if (editButton != null) {
            editButton.setPosition(changeButton.getX() - 5 - editButton.getWidth(), top - 2);
            editButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Inject(at = @At("RETURN"), method = "children", cancellable = true)
    private void addCustomChildren(CallbackInfoReturnable<List<? extends GuiEventListener>> cir) {
        if (selectButton != null) {
            cir.setReturnValue(List.of(selectButton));
            return;
        }

        List<GuiEventListener> newList = new ArrayList<>(cir.getReturnValue());
        if (editButton != null) {
            newList.add(editButton);
        }
        cir.setReturnValue(newList);
    }

    @Inject(at = @At("RETURN"), method = "narratables", cancellable = true)
    private void addCustomNarratables(CallbackInfoReturnable<List<? extends NarratableEntry>> cir) {
        if (selectButton != null) {
            cir.setReturnValue(List.of(selectButton));
            return;
        }

        List<NarratableEntry> newList = new ArrayList<>(cir.getReturnValue());
        if (editButton != null) {
            newList.add(editButton);
        }
        cir.setReturnValue(newList);
    }
}

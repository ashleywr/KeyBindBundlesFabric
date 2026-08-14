package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.KBClientConfig;
import com.matyrobbrt.keybindbundles.KeyBindBundle;
import com.matyrobbrt.keybindbundles.KeyBindBundleManager;
import com.matyrobbrt.keybindbundles.KeyMappingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindSelectionScreen extends Screen {
    private final KeyBindBundleManager.RadialKeyMapping mapping;
    private final RadialMenuRenderer<KeyBindBundle.KeyEntry> renderer = new RadialMenuRenderer<>() {
        @Override
        public List<KeyBindBundle.KeyEntry> getEntries() {
            return mapping.bind.getEntries();
        }

        @Override
        public int getCurrentlySelected() {
            return mapping.bind.getBookmark();
        }

        @Override
        public Component getTitle(KeyBindBundle.KeyEntry entry) {
            return Component.literal(entry.title());
        }

        @Override
        public ItemStack getIcon(KeyBindBundle.KeyEntry entry) {
            return entry.icon();
        }
    };

    public KeybindSelectionScreen(KeyBindBundleManager.RadialKeyMapping mapping) {
        super(mapping.getDisplayName());
        this.mapping = mapping;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderer.render(guiGraphics, true);
        clipMouseToMenu();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int selectionIndex = renderer.getElementUnderMouse(false);
        if (selectionIndex < 0 || selectionIndex >= mapping.bind.getEntries().size()) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (selectionIndex == mapping.bind.getBookmark()) {
                mapping.bind.setBookmark(-1);
            } else {
                mapping.bind.setBookmark(selectionIndex);
            }
            KeyBindBundleManager.write();
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            trigger(selectionIndex);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void trigger(int selectionIndex) {
        var key = KeyMappingUtil.getByName(mapping.bind.getEntries().get(selectionIndex).key());
        Minecraft.getInstance().setScreen(null);
        if (key != null) {
            KeyMappingUtil.tap(key);
        }
    }

    private void clipMouseToMenu() {
        if (!KBClientConfig.CLIP_MOUSE_TO_MENU.getAsBoolean()) {
            return;
        }

        var mainWindow = Minecraft.getInstance().getWindow();
        int windowWidth = mainWindow.getScreenWidth();
        int windowHeight = mainWindow.getScreenHeight();

        double[] xPos = new double[1];
        double[] yPos = new double[1];
        GLFW.glfwGetCursorPos(mainWindow.getWindow(), xPos, yPos);

        double scaledX = xPos[0] - (windowWidth / 2.0f);
        double scaledY = yPos[0] - (windowHeight / 2.0f);

        double distance = Math.sqrt(scaledX * scaledX + scaledY * scaledY);
        double radius = RadialMenuRenderer.OUTER * ((double) windowWidth / mainWindow.getGuiScaledWidth()) * 1.1;

        if (distance > radius) {
            double fixedX = scaledX * radius / distance;
            double fixedY = scaledY * radius / distance;

            GLFW.glfwSetCursorPos(mainWindow.getWindow(), (int) (windowWidth / 2 + fixedX), (int) (windowHeight / 2 + fixedY));
        }
    }

    @Override
    public void onClose() {
        KeyMappingUtil.restoreAll();
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

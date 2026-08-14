package com.matyrobbrt.keybindbundles.render;

import com.matyrobbrt.keybindbundles.util.DelegatingConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AutoCompleteEditBox<T> extends EditBox {
    private static final int DEFAULT_MIN_SEARCH_LENGTH = 2;
    private static final long DEFAULT_DEBOUNCE_MILLIS = 150L;
    private static final int PICKER_SIZE = 16;
    private static final int PICKER_GAP = 3;
    private static final int PICKER_TOP_PADDING = 2;

    private final SearchTree<T> tree;
    private final Function<T, ResourceLocation> idGetter;
    private final int maxSuggestions;
    private final int minSearchLength;
    private final long debounceMillis;

    private final DelegatingConsumer<String> responders;
    private final AutoComplete autoComplete;
    public AutoCompleteEditBox(Font font, int x, int y, int width, int height, int itemHeight, int itemWidth, int maxSuggestions, Component message, SearchTree<T> tree, Function<T, ResourceLocation> idGetter) {
        this(font, x, y, width, height, itemHeight, itemWidth, maxSuggestions, message, tree, idGetter, DEFAULT_MIN_SEARCH_LENGTH, DEFAULT_DEBOUNCE_MILLIS);
    }

    public AutoCompleteEditBox(Font font, int x, int y, int width, int height, int itemHeight, int itemWidth, int maxSuggestions, Component message, SearchTree<T> tree, Function<T, ResourceLocation> idGetter, int minSearchLength, long debounceMillis) {
        super(font, x, y, width, height, message);
        this.tree = tree;
        this.idGetter = idGetter;
        this.maxSuggestions = maxSuggestions;
        this.minSearchLength = minSearchLength;
        this.debounceMillis = debounceMillis;
        setResponder(responders = new DelegatingConsumer<>());

        addResponder(autoComplete = new AutoComplete(x, y + 2 + height, width, itemHeight, itemWidth));

        setFormatter((search, cursor) -> {
            if (search.indexOf('@') >= 0) {
                var comp = Component.empty();
                var spl = search.split(" ");
                for (int i = 0; i < spl.length; i++) {
                    if (i != 0) comp = comp.append(Component.literal(" "));

                    if (spl[i].startsWith("@")) {
                        comp = comp.append(Component.literal(spl[i]).withStyle(ChatFormatting.GOLD));
                    } else {
                        comp = comp.append(Component.literal(spl[i]));
                    }
                }

                int trailingAmount = 0;
                int lastIndex = search.length() - 1;
                while (search.charAt(lastIndex--) == ' ') trailingAmount++;
                if (trailingAmount > 0) comp = comp.append(Component.literal(" ".repeat(trailingAmount)));

                return comp.getVisualOrderText();
            }
            return Component.literal(search).getVisualOrderText();
        });
    }

    public void addResponder(Consumer<String> res) {
        responders.add(res);
    }

    public abstract void renderItem(GuiGraphics graphics, int x, int y, T item);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setValue("");
            autoComplete.accept("");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        autoComplete.flushPendingSearch(true);

        if (key == GLFW.GLFW_KEY_ENTER && autoComplete().selectedIndex >= 0) {
            return autoComplete.chooseSelectedSuggestion();
        } else if (key == GLFW.GLFW_KEY_DOWN) {
            autoComplete.scrollDown();
            return true;
        } else if (key == GLFW.GLFW_KEY_UP) {
            autoComplete.scrollUp();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    public AutoComplete autoComplete() {
        return autoComplete;
    }

    public class AutoComplete extends AbstractWidget implements Consumer<String> {
        private List<T> currentSuggestions = List.of();
        private final Vector2d lastMousePosition = new Vector2d(0, 0);

        private final int itemHeight, itemWidth;

        private int selectedIndex, offset;

        private String prevSearch;
        private String pendingSearch;
        private long pendingSearchAtMillis;

        public AutoComplete(int x, int y, int width, int itemHeight, int itemWidth) {
            super(x, y, width, itemHeight * maxSuggestions + PICKER_TOP_PADDING + PICKER_SIZE, Component.empty());
            this.itemHeight = itemHeight;
            this.itemWidth = itemWidth;
        }

        @Override
        public void accept(String search) {
            if (search.isBlank() || isBelowMinimumSearchLength(search)) {
                clearSuggestions(search);
                return;
            }

            if (Objects.equals(search, prevSearch) || Objects.equals(search, pendingSearch)) return;

            pendingSearch = search;
            pendingSearchAtMillis = System.currentTimeMillis() + debounceMillis;
        }

        private boolean isBelowMinimumSearchLength(String search) {
            return search.trim().length() < minSearchLength;
        }

        private void flushPendingSearch(boolean force) {
            if (pendingSearch == null) {
                return;
            }

            if (!force && System.currentTimeMillis() < pendingSearchAtMillis) {
                return;
            }

            updateSuggestions(pendingSearch);
            pendingSearch = null;
        }

        private void clearSuggestions(String search) {
            pendingSearch = null;
            prevSearch = search;
            offset = 0;
            selectedIndex = -1;
            currentSuggestions = List.of();
        }

        private void updateSuggestions(String search) {
            prevSearch = search;
            offset = 0;
            selectedIndex = 0;

            var asRl = ResourceLocation.tryParse(search);

            String namespaceFilter;
            if (search.indexOf('@') >= 0) {
                var spl = new ArrayList<>(Arrays.asList(search.split(" ")));
                var element = spl.stream()
                        .filter(s -> s.startsWith("@"))
                        .findFirst()
                        .orElseThrow();

                spl.remove(element);
                namespaceFilter = element.substring(1).toLowerCase(Locale.ROOT);
                search = String.join(" ", spl);
            } else {
                namespaceFilter = "";
            }

            var items = tree.search(search.trim().toLowerCase(Locale.ROOT));

            var distinctSet = new HashSet<ResourceLocation>();
            var newItems = new ArrayList<T>(items.size());
            for (T item : items) {
                if (distinctSet.add(idGetter.apply(item))) {
                    newItems.add(item);
                }
            }
            items = newItems;

            if (asRl != null && items.stream().anyMatch(i -> idGetter.apply(i).equals(asRl))) {
                selectedIndex = -1;
                currentSuggestions = List.of();
            } else {
                if (namespaceFilter.isBlank()) {
                    currentSuggestions = items;
                } else {
                    currentSuggestions = items.stream().filter(i -> idGetter.apply(i).getNamespace().startsWith(namespaceFilter))
                            .toList();
                }

                if (currentSuggestions.isEmpty()) {
                    selectedIndex = -1;
                }
            }
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (AutoCompleteEditBox.this.isFocused()) {
                flushPendingSearch(false);
                updateHoveringState(mouseX, mouseY);

                for (int i = offset; i < offset + shownSuggestions(); i++) {
                    int minX = this.getX() + 2;
                    int minY = this.getY() + itemHeight * (i - offset);
                    int maxY = minY + itemHeight;
                    var item = currentSuggestions.get(i);
                    var hovered = i - offset == selectedIndex;
                    guiGraphics.fill(RenderType.guiOverlay(), this.getX(), minY, this.getX() + this.getWidth(), maxY, hovered ? -535752431 : -536870912);
                    renderItem(guiGraphics, minX, minY, item);
                    guiGraphics.drawString(Minecraft.getInstance().font, Component.literal(idGetter.apply(item).toString()), minX + itemWidth + 2, minY + (itemHeight - 9) / 2, hovered ? ChatFormatting.YELLOW.getColor() : -1);
                }

                renderPicker(guiGraphics, mouseX, mouseY);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        private void updateHoveringState(double x, double y) {
            if (!this.lastMousePosition.equals(x, y)) {
                if (this.isMouseOverSuggestions(x, y)) {
                    int minY = this.getY();

                    for (int i = 0; i < this.shownSuggestions(); ++i) {
                        int maxY = minY + itemHeight;
                        if (x >= this.getX() && x <= (this.getX() + this.getWidth()) && y >= minY && y < maxY) {
                            this.selectedIndex = i;
                        }

                        minY = maxY;
                    }
                }
                lastMousePosition.set(x, y);
            }
        }

        @Nullable
        private T getSuggestion(int index) {
            return index < 0 || index >= currentSuggestions.size() ? null : currentSuggestions.get(index);
        }

        private int shownSuggestions() {
            return Math.min(maxSuggestions, Math.max(currentSuggestions.size() - offset, 0));
        }

        private int selectedSuggestionIndex() {
            return selectedIndex < 0 ? -1 : offset + selectedIndex;
        }

        private void scrollUp() {
            moveSelection(selectedSuggestionIndex() <= 0 ? 0 : selectedSuggestionIndex() - 1);
        }

        private void scrollDown() {
            moveSelection(selectedSuggestionIndex() < 0 ? 0 : selectedSuggestionIndex() + 1);
        }

        private void moveSelection(int index) {
            if (currentSuggestions.isEmpty()) {
                offset = 0;
                selectedIndex = -1;
                return;
            }

            index = Mth.clamp(index, 0, currentSuggestions.size() - 1);
            if (index < offset) {
                offset = index;
            } else if (index >= offset + maxSuggestions) {
                offset = index - maxSuggestions + 1;
            }

            selectedIndex = index - offset;
        }

        @Override
        public boolean mouseScrolled(double xpos, double ypos, double xDelta, double yDelta) {
            if (!this.isMouseOverSuggestions(xpos, ypos)) {
                return false;
            } else {
                int selectedSuggestion = selectedSuggestionIndex();
                this.offset = (int) Mth.clamp((double)this.offset - yDelta, 0.0, Math.max(this.currentSuggestions.size() - maxSuggestions, 0));
                if (selectedSuggestion >= 0) {
                    selectedIndex = Mth.clamp(selectedSuggestion - offset, 0, Math.max(shownSuggestions() - 1, 0));
                }
                this.lastMousePosition.set(0.0);
                return true;
            }
        }

        @Override
        protected boolean clicked(double xpos, double ypos) {
            return super.clicked(xpos, ypos) && (isMouseOverSuggestions(xpos, ypos) || isMouseOverPicker(xpos, ypos));
        }

        @Override
        public boolean isMouseOver(double xpos, double ypos) {
            return super.isMouseOver(xpos, ypos) && (isMouseOverSuggestions(xpos, ypos) || isMouseOverPicker(xpos, ypos));
        }

        @Override
        public boolean mouseClicked(double mx, double my, int mb) {
            flushPendingSearch(true);

            if (super.mouseClicked(mx, my, mb)) {
                if (isMouseOverPicker(mx, my)) {
                    return clickPicker(mx, my);
                }

                updateHoveringState(mx, my);
                return chooseSelectedSuggestion();
            } else {
                return false;
            }
        }

        private boolean chooseSelectedSuggestion() {
            var item = getSuggestion(selectedSuggestionIndex());
            if (item == null) {
                return false;
            }

            var id = idGetter.apply(item).toString();
            setValue(id);
            clearSuggestions(id);
            return true;
        }

        private void renderPicker(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (currentSuggestions.isEmpty()) {
                return;
            }

            int startX = pickerStartX();
            int y = pickerY();
            renderPickerButton(guiGraphics, startX, y, mouseX, mouseY, true);
            renderSelectedIcon(guiGraphics, startX + PICKER_SIZE + PICKER_GAP, y, mouseX, mouseY);
            renderPickerButton(guiGraphics, startX + (PICKER_SIZE + PICKER_GAP) * 2, y, mouseX, mouseY, false);
        }

        private void renderPickerButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, boolean up) {
            boolean hovered = mouseX >= x && mouseX < x + PICKER_SIZE && mouseY >= y && mouseY < y + PICKER_SIZE;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + PICKER_SIZE, y + PICKER_SIZE, hovered ? -535752431 : -536870912);

            int color = hovered ? ChatFormatting.YELLOW.getColor() : -1;
            int centerX = x + PICKER_SIZE / 2;
            int startY = up ? y + 5 : y + 10;
            for (int row = 0; row < 4; row++) {
                int halfWidth = up ? row : 3 - row;
                int lineY = up ? startY + row : startY - row;
                guiGraphics.fill(RenderType.guiOverlay(), centerX - halfWidth, lineY, centerX + halfWidth + 1, lineY + 1, color);
            }
        }

        private void renderSelectedIcon(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX < x + PICKER_SIZE && mouseY >= y && mouseY < y + PICKER_SIZE;
            guiGraphics.fill(RenderType.guiOverlay(), x, y, x + PICKER_SIZE, y + PICKER_SIZE, hovered ? -535752431 : -536870912);

            var item = getSuggestion(selectedSuggestionIndex());
            if (item != null) {
                renderItem(guiGraphics, x, y, item);
            }
        }

        private boolean clickPicker(double mx, double my) {
            int startX = pickerStartX();
            int y = pickerY();
            if (mx >= startX && mx < startX + PICKER_SIZE && my >= y && my < y + PICKER_SIZE) {
                scrollUp();
                return true;
            }

            int iconX = startX + PICKER_SIZE + PICKER_GAP;
            if (mx >= iconX && mx < iconX + PICKER_SIZE && my >= y && my < y + PICKER_SIZE) {
                return chooseSelectedSuggestion();
            }

            int downX = startX + (PICKER_SIZE + PICKER_GAP) * 2;
            if (mx >= downX && mx < downX + PICKER_SIZE && my >= y && my < y + PICKER_SIZE) {
                scrollDown();
                return true;
            }

            return false;
        }

        private boolean isMouseOverSuggestions(double x, double y) {
            return super.isMouseOver(x, y) && y >= getY() && y < getY() + shownSuggestions() * itemHeight;
        }

        private boolean isMouseOverPicker(double x, double y) {
            return !currentSuggestions.isEmpty()
                    && x >= pickerStartX()
                    && x < pickerStartX() + PICKER_SIZE * 3 + PICKER_GAP * 2
                    && y >= pickerY()
                    && y < pickerY() + PICKER_SIZE;
        }

        private int pickerStartX() {
            return this.getX() + this.getWidth() - PICKER_SIZE * 3 - PICKER_GAP * 2;
        }

        private int pickerY() {
            return this.getY() + shownSuggestions() * itemHeight + PICKER_TOP_PADDING;
        }
    }
}

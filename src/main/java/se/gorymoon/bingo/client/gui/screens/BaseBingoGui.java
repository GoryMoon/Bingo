package se.gorymoon.bingo.client.gui.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.ItemRenderer;
import se.gorymoon.bingo.client.gui.BingoGui;
import se.gorymoon.bingo.client.gui.IBaseGui;
import se.gorymoon.bingo.client.gui.IBingoGui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseBingoGui extends Gui implements IGuiEventListener, IBaseGui, IBingoGui {

    private boolean isHidden = true;
    private int width;
    private int height;
    private int guiLeft;
    private int guiTop;
    private BingoGui gui;
    private List<TabGui> tabs = new ArrayList<>();

    Minecraft mc;
    ItemRenderer itemRender;
    FontRenderer fontRenderer;

    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {}

    public void setHidden(boolean hidden) {
        isHidden = hidden;
        if (isHidden) {
            tabs.forEach(tabGui -> tabGui.visible = false);
            hide();
        } else {
            tabs.forEach(tabGui -> tabGui.visible = true);
            show();
        }
    }

    public void show() {

    }

    public void hide() {

    }

    public boolean isHidden() {
        return isHidden;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public int getLeft() {
        return guiLeft;
    }

    @Override
    public int getTop() {
        return guiTop;
    }

    @Override
    public BingoGui getGui() {
        return gui;
    }

    public void setGui(BingoGui gui) {
        this.gui = gui;
    }

    public TabGui addTab(TabGui tab) {
        tabs.add(tab);
        return tab;
    }

    public void drawBackground(float partialTicks, int mouseX, int mouseY) {
        tabs.forEach(tabGui -> tabGui.render(mouseX, mouseY));
    }

    public void drawForeground(int mouseX, int mouseY) {
        tabs.forEach(tabGui -> drawHoveringText(tabGui.getTooltip(mouseX, mouseY), mouseX, mouseY));
    }

    public void refresh(Minecraft mc, int width, int height, int guiLeft, int guiTop) {
        this.mc = mc;
        this.itemRender = mc.getItemRenderer();
        this.fontRenderer = mc.fontRenderer;
        this.width = width;
        this.height = height;
        this.guiLeft = guiLeft;
        this.guiTop = guiTop;
        tabs.clear();
    }

    public void drawHoveringText(String text, int x, int y) {
        getGui().drawHoveringText(Arrays.asList(text.split("\n")), x - getLeft(), y - getTop());
    }

    public void drawHoveringText(List<String> text, int x, int y) {
        getGui().drawHoveringText(text, x - getLeft(), y - getTop());
    }

    public TabGui getBackTab() {
        return new TabGui(getLeft() + 31, getTop(), 224, 50, ImmutableList.of("Back to previous gui")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                getGui().navigateBack();
            }
        };
    }

    public void tick() {
        if (getGui().getFocused() != this) {
            getGui().focusOn(this);
        }
    }
}

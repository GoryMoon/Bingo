package se.gorymoon.bingo.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import se.gorymoon.bingo.client.gui.BingoGui;
import se.gorymoon.bingo.client.gui.IBaseGui;

import java.util.Collections;
import java.util.List;

public class TabGui extends Gui implements IGuiEventListener, IBaseGui {

    private int x;
    private int y;
    private int iconX;
    private int iconY;
    private List<String> tooltip;
    public boolean visible = true;

    public TabGui(int x, int y, int iconX, int iconY, List<String> tooltip) {
        this.x = x;
        this.y = y;
        this.iconX = iconX;
        this.iconY = iconY;
        this.tooltip = tooltip;
    }

    public void render(int mouseX, int mouseY) {
        if (!visible) return;
        GlStateManager.disableLighting();
        int texX = isPointInRegion(x, y, 24, 21, mouseX, mouseY) ? 39: 15;
        texture().bindTexture(BingoGui.TEXTURE);
        drawTexturedModalRect(x, y, texX, 200, 24, 21);
        drawTexturedModalRect(x + 4, y + 4, iconX, iconY, 16, 16);
        GlStateManager.enableLighting();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && visible && isPointInRegion(x, y, 24, 21, mouseX, mouseY)) {
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }

    public void onClick(double mouseX, double mouseY) {

    }

    public List<String> getTooltip(int mouseX, int mouseY) {
        return visible && isPointInRegion(x, y, 24, 21, mouseX, mouseY) ? tooltip : Collections.emptyList();
    }

}

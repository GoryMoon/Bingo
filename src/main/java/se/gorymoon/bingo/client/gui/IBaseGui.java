package se.gorymoon.bingo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

public interface IBaseGui {

    default  Minecraft mc() {
        return Minecraft.getInstance();
    }

    default TextureManager texture() {
        return mc().getTextureManager();
    }

    default ItemRenderer itemRender() {
        return mc().getItemRenderer();
    }

    default FontRenderer fontRenderer() {
        return mc().fontRenderer;
    }

    default boolean isSlotSelected(int x, int y, double mouseX, double mouseY) {
        return isPointInRegion(x, y, 18, 18, mouseX, mouseY);
    }

    default boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX < (double)(x + width) && mouseY >= (double)y && mouseY < (double)(y + height);
    }

    default boolean isPointInButton(GuiButton button, double mouseX, double mouseY) {
        return isPointInRegion(button.x, button.y, button.width, button.height, mouseX, mouseY);
    }
}

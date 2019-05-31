package se.gorymoon.bingo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

public class Patch9 extends Gui {

    public int x;
    public int y;
    private int width;
    private int height;

    private int texX;
    private int texY;

    private int size;

    public Patch9(int x, int y, int width, int height, int texX, int texY, int texSize) {
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
        this.texX = texX;
        this.texY = texY;
        size = texSize / 3;
    }

    public void setWidth(int width) {
        this.width = (width + 4) / 5 * 5;
    }

    public void setHeight(int height) {
        this.height = (height + 4) / 5 * 5;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void render() {
        GlStateManager.disableLighting();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bindTexture(BingoGui.TEXTURE);
        int h = height - size;
        int w = width - size;

        for (int xx = 0; xx < width; xx += size) {
            for (int yy = 0; yy < height; yy += size) {
                if (xx == 0 &&  yy == 0) {
                    drawTexturedModalRect(x + xx, y + yy, texX, texY, size, size);
                } else if (xx == w && yy == h) {
                    drawTexturedModalRect(x + xx, y + yy, texX + size * 2, texY + size * 2, size, size);
                } else if (xx == w && yy == 0) {
                    drawTexturedModalRect(x + xx, y + yy, texX + size * 2, texY, size, size);
                } else if (xx == 0 && yy == h) {
                    drawTexturedModalRect(x + xx, y + yy, texX, texY + size * 2, size, size);
                } else if (xx == 0) {
                    drawTexturedModalRect(x + xx, y + yy, texX, texY + size, size, size);
                } else if (xx == w) {
                    drawTexturedModalRect(x + xx, y + yy, texX + size * 2, texY + size, size, size);
                } else if (yy == 0) {
                    drawTexturedModalRect(x + xx, y + yy, texX + size, texY, size, size);
                } else if (yy == h) {
                    drawTexturedModalRect(x + xx, y + yy, texX + size, texY + size * 2, size, size);
                } else {
                    drawTexturedModalRect(x + xx, y + yy, texX + size, texY + size, size, size);
                }
            }
        }
        GlStateManager.enableLighting();
    }
}

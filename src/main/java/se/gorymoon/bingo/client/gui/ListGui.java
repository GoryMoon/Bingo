package se.gorymoon.bingo.client.gui;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public abstract class ListGui implements IGuiEventListener {

    private final List<IEntry> entries = new ArrayList<>();
    private final IBingoGui base;

    public final int x;
    public final int y;
    public int height = 130;
    public int width;

    private int slotHeight;
    private double scrollAmount;
    private boolean clickedScrollbar;
    protected boolean visible = true;


    public ListGui(IBingoGui base, int x, int y, int width, int slotHeight) {
        this.base = base;
        this.x = x;
        this.y = y;
        this.width = width;
        this.slotHeight = slotHeight;
    }

    public void addEntry(IEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(IEntry entry) {
        entries.remove(entry);
    }

    public void draw(double mouseX, double mouseY) {
        if (isVisible()) {
            this.clampAmountScrolled();
            int xx = x + 1;
            int yy = y - (int)this.scrollAmount;
            GlStateManager.disableLighting();
            Gui.drawRect(x, y, x + width, y + height, 0xFFDDDDDD);

            doGlScissor(x + 1, y + 1, width - 2, height - 2);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            for(int j = 0; j < entries.size(); ++j) {
                int k = yy + 1 + j * this.slotHeight;
                entries.get(j).drawEntry(xx, k, width - 2, slotHeight, mouseX, mouseY);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            base.getGui().texture().bindTexture(BingoGui.TEXTURE);
            base.getGui().drawTexturedModalRect(x + width, y, 242, 0, 7, 130);
            int j1 = this.getMaxScroll();
            int texY = j1 > 0 ? 0: 14;
            int k1 = (int)((float)((height - (y + 1)) * height / (float)this.getContentHeight()));
            k1 = MathHelper.clamp(k1, 16, height - 8);
            int l1 = (j1 > 0 ? ((int)this.scrollAmount * (height - k1) / j1) : 0) + (y + 1);
            if (l1 < y) {
                l1 = y;
            }
            base.getGui().drawTexturedModalRect(x + width + 1, l1, 249, texY, 5, 14);
            GlStateManager.enableLighting();
        }
    }

    public void drawHovering(double mouseX, double mouseY) {
        if(isVisible() && this.isMouseInList(mouseX, mouseY)) {
            int i = this.getEntryAt(mouseX, mouseY);
            if (i != -1) {
                entries.get(i).drawHovering(mouseX, mouseY);
            }
        }
    }

    public static void doGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        MainWindow mw = mc.mainWindow;
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;

        if (k == 0) {
            k = 1000;
        }

        while (scaleFactor < k &&  mw.getFramebufferWidth() / (scaleFactor + 1) >= 320 && mw.getFramebufferHeight() / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor(x * scaleFactor, mw.getFramebufferHeight() - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
    }

    public int getEntryAt(double x, double y) {
        int i = this.x + this.width / 2 - width / 2;
        int j = this.x + this.width / 2 + width / 2;
        int k = MathHelper.floor(y - (double)this.y) + (int)this.scrollAmount - 4;
        int l = k / this.slotHeight;
        return x < (double)width + this.x && x >= (double)i && x <= (double)j && l >= 0 && k >= 0 && l < entries.size() ? l : -1;
    }

    private void clampAmountScrolled() {
        this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0D, (double)this.getMaxScroll());
    }

    private int getMaxScroll() {
        return Math.max(0, this.getContentHeight() - height);
    }

    private int getContentHeight() {
        return entries.size() * this.slotHeight + 2;
    }

    private void checkScrollbarClick(double mouseX, int button) {
        this.clickedScrollbar = button == 0 && mouseX >= (double)x + width && mouseX < (double)x + width + 7;
    }
    protected boolean isMouseInList(double mouseX, double mouseY) {
        return mouseY >= (double)y && mouseY <= (double)y + height && mouseX >= (double)x && mouseX <= (double)x + width + 7;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkScrollbarClick(mouseX, button);
        if (isVisible() && this.isMouseInList(mouseX, mouseY)) {
            int i = this.getEntryAt(mouseX, mouseY);
            if (i != -1 && entries.get(i).mouseClicked(mouseX, mouseY, button)) {
                return true;
            } else {
                return clickedScrollbar;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        entries.forEach(teamEntry -> teamEntry.mouseReleased(mouseX, mouseY, button));
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isVisible() && button == 0 && this.clickedScrollbar) {
            if (mouseY < (double)y) {
                this.scrollAmount = 0.0D;
            } else if (mouseY > (double)y + height) {
                this.scrollAmount = (double)this.getMaxScroll();
            } else {
                double d0 = (double)this.getMaxScroll();
                if (d0 < 1.0D) {
                    d0 = 1.0D;
                }

                int i = (int)((float)height / (float)this.getContentHeight());
                i = MathHelper.clamp(i, 16, height - 8);
                double d1 = d0 / (double)(height - i);
                if (d1 < 1.0D) {
                    d1 = 1.0D;
                }

                scrollAmount += dragY * d1;
                clampAmountScrolled();
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        if (!this.isVisible()) {
            return false;
        } else {
            this.scrollAmount -= scroll * (double)this.slotHeight / 2.0D;
            return true;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public interface IEntry extends IGuiEventListener {

        void drawEntry(int x, int y, int width, int height, double mouseX, double mouseY);

        void drawHovering(double mouseX, double mouseY);
    }

}

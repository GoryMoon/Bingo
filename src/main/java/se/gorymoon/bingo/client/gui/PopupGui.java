package se.gorymoon.bingo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.utils.TriConsumer;

public class PopupGui extends Gui implements IBaseGui{

    private BoardGui boardGui;
    private boolean renderOnScreen = false;
    private boolean visibility = true;
    private boolean showing = false;
    private long animationTime = -1;

    private Patch9 clockArea;
    private Patch9 boardArea;

    private static final float SPEED = 400.F;

    public PopupGui() {
        boardGui = new BoardGui(4, 0, null);
        clockArea = new Patch9(0, 0, 30, 15, 0, 200, 15);
        boardArea = new Patch9(0, 0, 115, 115, 0, 200, 15);
    }

    public void setRenderOnScreen(boolean renderOnScreen) {
        this.renderOnScreen = renderOnScreen;
    }

    public void setRenderTooltip(TriConsumer<ItemStack, Integer, Integer> renderTooltip) {
        boardGui.setRenderTooltip(renderTooltip);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!BoardManager.INSTANCE.getBingoBoard().isPresent() || !TeamManager.INSTANCE.getTeamFromPlayer(se.gorymoon.bingo.utils.Util.getUUID(mc().player)).isPresent()) return;
        int boardTop = mc().mainWindow.getScaledHeight() / 2 - 53;
        String runTime = BoardManager.INSTANCE.getFormattedRunTime();
        FontRenderer fontRenderer = fontRenderer();

        int width = fontRenderer.getStringWidth(runTime);
        clockArea.y = boardTop - 17;
        clockArea.setWidth(width + 10);
        clockArea.render();
        GlStateManager.disableLighting();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int color = BoardManager.INSTANCE.isRunning() ? 0xFFFFFFFF: 0xFFAA00;
        drawCenteredString(fontRenderer, runTime, clockArea.getWidth() / 2, clockArea.y + 4, color);
        GlStateManager.enableLighting();

        GuiScreen currentScreen = Minecraft.getInstance().currentScreen;
        if ((renderOnScreen || (
                        currentScreen == null ||
                        currentScreen instanceof GuiIngameMenu ||
                        currentScreen instanceof GuiOptions ||
                        currentScreen instanceof ScreenChatOptions ||
                        currentScreen instanceof GuiCustomizeSkin ||
                        currentScreen instanceof GuiScreenOptionsSounds)
        )) {
            long i = Util.milliTime();
            if (visibility != showing && visibility) {
                showing = true;
                this.animationTime = i - (long) (int)(this.getVisibility(i) * SPEED);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(-boardArea.getWidth() * this.getVisibility(i), 1.0F, 1.0F);
            boardArea.y = boardTop - 4;
            boardArea.render();

            boardGui.y = boardTop;
            boardGui.render(mouseX, mouseY, partialTicks);

            GlStateManager.popMatrix();
            if (showing) {
                boardGui.renderTooltips(mouseX, mouseY);
            }
            if (visibility != showing && !visibility) {
                showing = false;
                this.animationTime = i - (long)((int)((1.0F - this.getVisibility(i)) * SPEED));
            }
        }
    }

    private float getVisibility(long milli) {
        float f = MathHelper.clamp((float)(milli - this.animationTime) / SPEED, 0.0F, 1.0F);
        f = f * f;
        return showing ? 1.0F - f : f;
    }

    public void toggleVisibility() {
        setVisibility(!this.visibility);
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}

package se.gorymoon.bingo.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BingoBoard;
import se.gorymoon.bingo.game.board.BingoItem;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.utils.TriConsumer;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class BoardGui extends Gui implements IBaseGui {

    public int x;
    public int y;
    private TriConsumer<ItemStack, Integer, Integer> renderTooltip;

    public BoardGui(int x, int y, @Nullable TriConsumer<ItemStack, Integer, Integer> renderTooltip) {
        this.x = x;
        this.y = y;
        this.renderTooltip = renderTooltip;
    }

    public void setRenderTooltip(TriConsumer<ItemStack, Integer, Integer> renderTooltip) {
        this.renderTooltip = renderTooltip;
    }

    public void renderTooltips(int mouseX, int mouseY) {
        if (renderTooltip != null && isPointInRegion(x, y, 106, 106, mouseX, mouseY)) {
            AtomicReference<NonNullList<IBingoItem>> atomicReference = new AtomicReference<>(NonNullList.withSize(BingoBoard.BOARD_TOTAL_SIZE, new BingoItem()));
            BoardManager.INSTANCE.getBingoBoard().ifPresent(board -> atomicReference.set(board.getItems()));

            NonNullList<IBingoItem> items = atomicReference.get();
            for (int i = 0; i < BingoBoard.BOARD_TOTAL_SIZE; i++) {
                IBingoItem item = items.get(i);
                if (item.getStack().isEmpty()) continue;
                int col = (i % BingoBoard.BOARD_SIZE);
                int row = (i / BingoBoard.BOARD_SIZE);
                int xx = x + col * 18 + 4 * col;
                int yy = y + row * 18 + 4 * row;
                if (isSlotSelected(xx, yy, mouseX, mouseY)) {
                    renderTooltip.accept(item.getStack(), mouseX, mouseY);
                }
            }
        }
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        AtomicReference<NonNullList<IBingoItem>> atomicReference = new AtomicReference<>(NonNullList.withSize(BingoBoard.BOARD_TOTAL_SIZE, new BingoItem()));
        BoardManager.INSTANCE.getBingoBoard().ifPresent(board -> atomicReference.set(board.getItems()));

        NonNullList<IBingoItem> items = atomicReference.get();

        for (int i = 0; i < BingoBoard.BOARD_TOTAL_SIZE; i++) {
            IBingoItem item = items.get(i);
            texture().bindTexture(BingoGui.TEXTURE);

            int col = (i % BingoBoard.BOARD_SIZE);
            int row = (i / BingoBoard.BOARD_SIZE);
            int xx = x + col * 18 + 4 * col;
            int yy = y + row * 18 + 4 * row;

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            if (item.isCompleted()) {
                NonNullList<IBingoTeam> teams = item.getCompletedBy();
                int slices = teams.size() > 2 ? 4: teams.size();
                int offset = (int) ((Util.milliTime() / 200L) % Math.max(1, Math.ceil((double) teams.size() / (double) slices) - 1));
                int size = teams.size() - 4 * offset;
                slices = size > 2 ? 4: size;

                for (int j = 0; j < slices; j++) {
                    int index = j + 4 * offset;
                    IBingoTeam team = index >= teams.size() ? null: teams.get(index);
                    if (team != null) {
                        int color = team.getColor().getColor();
                        float r = (float)(color >> 16 & 255) / 255.0F;
                        float g = (float)(color >> 8 & 255) / 255.0F;
                        float b = (float)(color & 255) / 255.0F;


                        GlStateManager.color4f(r, g, b, 1.0F);
                        drawTexturedModalRect(xx - 2, yy - 2, 207 + (slices > 2 ? (j == 0 || j == 2 ? 11 : 0) : 0), 221 + (slices > 1 ? (j == 0 || j == 1 ? 11: 0): 0), slices > 2 ? 11: 22, slices > 1 ? 11: 22);
                    }
                }
            }
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(xx, yy, 224, 0, 18, 18);

            RenderHelper.enableGUIStandardItemLighting();
            itemRender().renderItemAndEffectIntoGUI(item.getStack(), xx + 1, yy + 1);
            GlStateManager.enableLighting();
            RenderHelper.disableStandardItemLighting();

            if (isSlotSelected(xx, yy, (double) mouseX, (double) mouseY)) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepthTest();
                GlStateManager.colorMask(true, true, true, false);
                int slotColor = 0x80ffffff;
                this.drawGradientRect(xx, yy, xx + 18, yy + 18, slotColor, slotColor);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepthTest();
            }
            GlStateManager.popMatrix();
        }
    }
}

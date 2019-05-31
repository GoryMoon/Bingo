package se.gorymoon.bingo.client.gui.screens;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.text.TextFormatting;
import se.gorymoon.bingo.handlers.GuiHandler;
import se.gorymoon.bingo.utils.Util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class HelpGui extends BaseBingoGui {

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        listenerConsumer.accept(addTab(getBackTab()));
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        String title = TextFormatting.UNDERLINE + "Help";
        fontRenderer.drawString(title, (float)(getWidth() / 2 - fontRenderer.getStringWidth(title) / 2), 26, 0xFF000000);
        int width = 214;
        TextFormatting accent = TextFormatting.DARK_BLUE;
        TextFormatting secondary = TextFormatting.BLUE;
        StringBuilder builder = new StringBuilder();
        builder.append("You can toggle the board popup when in game by pressing ").append(accent).append("[").append(GuiHandler.showPopup.getLocalizedName()).append("]").append(TextFormatting.RESET).append(".");
        builder.append("\n\n");
        builder.append("You can open the " + Util.BINGO_TITLE + " menu at any time in-game by clicking ").append(accent).append("[").append(GuiHandler.showBingo.getLocalizedName()).append("]").append(TextFormatting.RESET).append(".");
        builder.append("\n\n");
        builder.append("You can join other people by requesting an invite by them, the invite can then be accessed on the ").append(accent).append("Main").append(secondary).append(" -> ").append(accent).append("Team").append(secondary).append(" -> ").append(accent).append("Invites").append(TextFormatting.RESET).append(" page.");
        builder.append("\n\n");
        builder.append("The game settings can only be changed by an ").append(accent).append("OP").append(TextFormatting.RESET).append(" while in multiplayer.");
        builder.append("\n\n");
        builder.append("The game can only be started by an ").append(accent).append("OP").append(TextFormatting.RESET).append(" while in multiplayer.");
        fontRenderer.drawSplitString(builder.toString(), 5, 40, width, 0xFF000000);
        super.drawForeground(mouseX, mouseY);
    }
}

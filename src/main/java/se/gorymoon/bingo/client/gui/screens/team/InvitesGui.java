package se.gorymoon.bingo.client.gui.screens.team;

import net.minecraft.client.MainWindow;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.text.TextFormatting;
import se.gorymoon.bingo.client.gui.screens.BaseBingoGui;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.utils.Util;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InvitesGui extends BaseBingoGui {

    private InvitesListGui invitesList;

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        UUID player = Util.getUUID(mc().player);
        invitesList = new InvitesListGui(this, player);
        listenerConsumer.accept(invitesList);
        listenerConsumer.accept(addTab(getBackTab()));
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        MouseHelper mouseHelper = mc().mouseHelper;
        MainWindow mw = mc().mainWindow;
        int mouseX = (int)(mouseHelper.getMouseX() * (double)mw.getScaledWidth() / (double)mw.getWidth());
        int mouseY = (int)(mouseHelper.getMouseY() * (double)mw.getScaledHeight() / (double)mw.getHeight());
        return isInList(mouseX, mouseY) && invitesList.mouseScrolled(scroll);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return isInList(mouseX, mouseY) && invitesList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return invitesList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isInList(double mouseX, double mouseY) {
        return isPointInRegion(invitesList.x, invitesList.y, invitesList.width + 7, invitesList.height, mouseX, mouseY);
    }

    @Override
    public void drawBackground(float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(partialTicks, mouseX, mouseY);

        invitesList.draw(mouseX, mouseY);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        FontRenderer fontRenderer = fontRenderer();
        Util.drawCenteredString(fontRenderer, TextFormatting.UNDERLINE + "Invites", getWidth() / 2, 26, 0xFF000000);
        fontRenderer.drawSplitString("Invites show up below if you have any, you can either accept or deny them.", 4, 40, 214, 0xFF000000);
        if (TeamManager.INSTANCE.getPartyInvites(getGui().playerID).size() <= 0) {
            Util.drawCenteredString(fontRenderer, TextFormatting.DARK_GRAY + "You have no invites", getWidth() / 2, 100, 0xFF000000);
        }

        invitesList.drawHovering(mouseX, mouseY);

        super.drawForeground(mouseX, mouseY);
    }
}

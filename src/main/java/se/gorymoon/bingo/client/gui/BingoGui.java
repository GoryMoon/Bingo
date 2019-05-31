package se.gorymoon.bingo.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.client.gui.screens.*;
import se.gorymoon.bingo.client.gui.screens.team.InvitesGui;
import se.gorymoon.bingo.client.gui.screens.team.TeamGui;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.events.TeamSyncEvent;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.handlers.GuiHandler;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.utils.Util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

public class BingoGui extends GuiScreen implements IBaseGui {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Bingo.MOD_ID, "textures/gui/bingo.png");

    /** The X size of the inventory window in pixels. */
    private int xSize;
    /** The Y size of the inventory window in pixels. */
    private int ySize;
    /** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
    private int guiLeft;
    /** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
    private int guiTop;

    private String tabToOpen;
    private StartGui startGui = new StartGui();
    private BaseBingoGui currentScreen;
    private int buttonId = 0;
    public UUID playerID;
    private Patch9 clockArea;
    private Deque<BaseBingoGui> navigationDeque = new ArrayDeque<>();
    private BaseBingoGui goToGui;

    public BingoGui(String tab) {
        tabToOpen = tab;
        this.xSize = 224;
        this.ySize = 221;
    }

    public static void open(String tab) {
        Minecraft.getInstance().displayGuiScreen(new BingoGui(tab));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void setCurrentScreen(BaseBingoGui screen) {
        setCurrentScreen(screen, true);
    }

    public void setCurrentScreen(BaseBingoGui screen, boolean saveNavigation) {
        if (currentScreen != null) {
            currentScreen.setHidden(true);
            children.clear();
            buttons.clear();
            if (saveNavigation) {
                navigationDeque.push(currentScreen);
            }
        }

        currentScreen = screen;
        currentScreen.refresh(this.mc, xSize, ySize, guiLeft, guiTop);
        currentScreen.setGui(this);
        currentScreen.initGui(this::addButton, children::add, this::getButtonNextId);
        currentScreen.setHidden(false);
        children.add(currentScreen);
    }

    public void navigateBack() {
        if (navigationDeque.peek() != null) {
            setCurrentScreen(navigationDeque.pop(), false);
        }
    }

    public void goToGui(String name) {
        goToGui = getGuiFromName(name);
    }

    private int getButtonNextId() {
        return buttonId++;
    }

    @Override
    protected void initGui() {
        super.initGui();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        clockArea = new Patch9(getGuiLeft() + (xSize / 2) - 15, getGuiTop() + 6, 30, 15, 0, 200, 15);

        playerID = Util.getUUID(mc.player);
        checkTeamStatus(false);
        NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardManager.INSTANCE.getSyncMessage());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (currentScreen != null) {
            currentScreen.setHidden(true);
        }
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        super.render(mouseX, mouseY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)i, (float)j, 0.0F);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
        RenderHelper.enableStandardItemLighting();
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j + 21, 0, 0, this.xSize, this.ySize - 21);

        String runTime = BoardManager.INSTANCE.getFormattedRunTime();
        int width = fontRenderer.getStringWidth(runTime);
        clockArea.setWidth(width + 10);
        clockArea.x = getGuiLeft() + xSize / 2 - clockArea.getWidth() / 2;
        clockArea.render();


        currentScreen.drawBackground(partialTicks, mouseX, mouseY);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String runTime = BoardManager.INSTANCE.getFormattedRunTime();
        GlStateManager.disableLighting();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int color = BoardManager.INSTANCE.isRunning() ? 0xFFFFFFFF: 0xFFAA00;
        drawCenteredString(fontRenderer, runTime, clockArea.x - getGuiLeft() + clockArea.getWidth() / 2, clockArea.y - getGuiTop() + 4, color);

        currentScreen.drawForeground(mouseX, mouseY);
        GlStateManager.enableLighting();
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void tick() {
        super.tick();
        if (currentScreen != null) {
            currentScreen.tick();
        }
        if (goToGui != null) {
            setCurrentScreen(goToGui, false);
            goToGui = null;
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (GuiHandler.showBingo.matchesKey(key, scanCode) && !(getFocused() instanceof GuiTextField)) {
            close();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private void checkTeamStatus(boolean dataUpdated) {
        Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeamFromPlayer(playerID);
        if (currentScreen == null || dataUpdated) {
            if (optional.isPresent() && (currentScreen == null || currentScreen == startGui)) {
                setCurrentScreen(getGuiFromName(tabToOpen), false);
                tabToOpen = "";
                return;
            } else if (!optional.isPresent()) {
                setCurrentScreen(startGui, false);
                return;
            }
        }
        setCurrentScreen(currentScreen, false);
    }

    private BaseBingoGui getGuiFromName(String name) {
        navigationDeque.clear();
        switch (name) {
            case "invites":
                navigationDeque.push(new DashboardGui());
                navigationDeque.push(new TeamGui());
                return new InvitesGui();
            case "team":
                navigationDeque.push(new DashboardGui());
                return new TeamGui();
            case "settings":
                navigationDeque.push(new DashboardGui());
                return new SettingsGui();
            case "help":
                navigationDeque.push(new DashboardGui());
                return new HelpGui();
            default:
                return new DashboardGui();
        }
    }

    @SubscribeEvent
    public void onDataUpdated(TeamSyncEvent event) {
        if (event.getTeam().getStatus(playerID).isPresent()) {
            checkTeamStatus(true);
        }
    }

    public int getGuiLeft() { return guiLeft; }
    public int getGuiTop() { return guiTop; }
    public int getXSize() { return xSize; }
    public int getYSize() { return ySize; }
}

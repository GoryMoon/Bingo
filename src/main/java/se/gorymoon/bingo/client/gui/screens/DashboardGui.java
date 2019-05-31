package se.gorymoon.bingo.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.client.gui.BoardGui;
import se.gorymoon.bingo.client.gui.screens.team.TeamGui;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.events.BoardSyncEvent;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.BoardActionMessage;
import se.gorymoon.bingo.network.messages.TeamActionMessage;
import se.gorymoon.bingo.utils.PlayerCache;
import se.gorymoon.bingo.utils.Util;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DashboardGui extends BaseBingoGui {

    private BoardGui boardGui;
    private int boardY;
    private int generateButtonY;
    private int generateButtonX;
    private int startButtonX;
    private int startButtonY;

    private static final String[] BINGO = {Util.BINGO_TITLE, "§cB§bI§aN§eG§dO", "§dB§cI§bN§aG§eO", "§eB§dI§cN§bG§aO", "§aB§eI§dN§cG§bO"};
    private GuiButton startButton;
    private GuiButton generateButton;
    private GuiButtonExt stopButton;
    private GuiButtonExt teleportToTeam;

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        generateButtonX = getLeft() + (getWidth() / 2) - 65;
        generateButtonY = getTop() + (getHeight() / 2) + 60;
        generateButton = new GuiButtonExt(nextId.get(), generateButtonX, generateButtonY, 130, 20, "Generate New Board") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardActionMessage.generate());
            }
        };
        generateButton.visible = !BoardManager.INSTANCE.isRunning() && PlayerCache.INSTANCE.isOp(getGui().playerID);
        startButtonX = getLeft() + (getWidth() / 2) - 35;
        startButtonY = getTop() + (getHeight() / 2) + 63 + 21;
        startButton = new GuiButtonExt(nextId.get(), startButtonX, startButtonY, 70, 20, "Start Game") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardActionMessage.start());
                getGui().close();
            }
        };
        startButton.visible = generateButton.visible;
        startButton.enabled = BoardManager.INSTANCE.canStart() && !GameHandler.INSTANCE.isWaiting();

        stopButton = new GuiButtonExt(nextId.get(), startButtonX, startButtonY, 70, 20, "Stop Game") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardActionMessage.stop());
                stopButton.visible = false;
                teleportToTeam.visible = false;
                startButton.visible = generateButton.visible = PlayerCache.INSTANCE.isOp(getGui().playerID);
            }
        };
        stopButton.visible = BoardManager.INSTANCE.isRunning() && PlayerCache.INSTANCE.isOp(getGui().playerID);

        teleportToTeam = new GuiButtonExt(nextId.get(), generateButtonX, generateButtonY, 130, 20, "Teleport to Team") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardActionMessage.teleport());
                getGui().close();
            }
        };
        teleportToTeam.visible = BoardManager.INSTANCE.isRunning();

        TabGui tabLeave = new TabGui(getLeft() + 7, getTop(), 224, 66, ImmutableList.of("Leave game", TextFormatting.DARK_GRAY + "When leaving the game you also leave your team")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                Optional<IBingoTeam> teamOptional = TeamManager.INSTANCE.getTeamFromPlayer(getGui().playerID);
                teamOptional.ifPresent(team -> {
                    NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.kick(team.getTeamId(), getGui().playerID));
                });
            }
        };
        TabGui tabSettings = new TabGui(getLeft() + getWidth() - 31, getTop(), 224, 18, ImmutableList.of("Board Settings", TextFormatting.DARK_GRAY.toString() + TextFormatting.STRIKETHROUGH + "Modify item whitelist," + TextFormatting.RESET + TextFormatting.DARK_GRAY + " seed, game mode and game effect")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                getGui().setCurrentScreen(new SettingsGui());
            }
        };
        tabSettings.visible = generateButton.visible;
        TabGui tabTeam = new TabGui(getLeft() + getWidth() - 55, getTop(), 224, 34, ImmutableList.of("Team", TextFormatting.DARK_GRAY + "View Invites", TextFormatting.DARK_GRAY + "Rename/Change color of team", TextFormatting.DARK_GRAY + "Invite/Kick users", TextFormatting.DARK_GRAY + "Leave the team")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                getGui().setCurrentScreen(new TeamGui());
            }
        };
        TabGui tabHelp = new TabGui(getLeft() + getWidth() - 79, getTop(), 224, 82, ImmutableList.of("Help", TextFormatting.DARK_GRAY + "Info on how the bingo works")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                getGui().setCurrentScreen(new HelpGui());
            }
        };
        boardY = getTop() + (getHeight() / 2) - 53;
        boardGui = new BoardGui(getLeft() + (getWidth() / 2) - 53, boardY, (stack, x, y) -> Util.renderToolTip(stack, x - getLeft(), y - getTop(), getGui()));
        buttonConsumer.accept(generateButton);
        buttonConsumer.accept(startButton);
        buttonConsumer.accept(stopButton);
        buttonConsumer.accept(teleportToTeam);
        listenerConsumer.accept(addTab(tabLeave));
        listenerConsumer.accept(addTab(tabHelp));
        listenerConsumer.accept(addTab(tabTeam));
        listenerConsumer.accept(addTab(tabSettings));
    }

    @Override
    public void show() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void hide() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.scaled(1.5D, 1.5D, 1.5D);
        GlStateManager.translated(-37.0D, -(fontRenderer.FONT_HEIGHT * 2) - 15, 1.0D);
        drawCenteredString(fontRenderer, BINGO[(int) (net.minecraft.util.Util.milliTime() / 200L % 5)], (getWidth() / 2), boardY - getTop(), 0xFFFFFFFF);
        GlStateManager.popMatrix();

        if (generateButton.visible && isPointInButton(generateButton, mouseX, mouseY)) {
            drawHoveringText(ImmutableList.of("Generate a new board", TextFormatting.DARK_GRAY + "Resets any previous board"), mouseX, mouseY);
        }
        if (startButton.visible && isPointInButton(startButton, mouseX, mouseY)) {
            ArrayList<String> strings = Lists.newArrayList("Starts Game", TextFormatting.DARK_GRAY + "Teleports players and start timer");
            if (!startButton.enabled) {
                strings.add(TextFormatting.RED + "You need to generate a new board before you can start the game");
            }
            drawHoveringText(strings, mouseX, mouseY);
        }

        if (teleportToTeam.visible && isPointInButton(teleportToTeam, mouseX, mouseY)) {
            drawHoveringText(ImmutableList.of("Teleports to team", TextFormatting.DARK_GRAY + "Teleports you to your team or your team's starting position"), mouseX, mouseY);
        }
        if (stopButton.visible && isPointInButton(stopButton, mouseX, mouseY)) {
            drawHoveringText(ImmutableList.of("Stops Game", TextFormatting.DARK_GRAY + "Teleports players back and stop timer"), mouseX, mouseY);
        }

        boardGui.renderTooltips(mouseX, mouseY);
        super.drawForeground(mouseX, mouseY);
    }

    @Override
    public void drawBackground(float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(partialTicks, mouseX, mouseY);
        boardGui.render(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent
    public void onBoardSync(BoardSyncEvent event) {
        startButton.enabled = BoardManager.INSTANCE.canStart();
    }

}

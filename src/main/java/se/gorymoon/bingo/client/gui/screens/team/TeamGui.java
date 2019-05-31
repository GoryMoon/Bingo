package se.gorymoon.bingo.client.gui.screens.team;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.glfw.GLFW;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.client.gui.screens.BaseBingoGui;
import se.gorymoon.bingo.client.gui.screens.TabGui;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamActionMessage;
import se.gorymoon.bingo.utils.PlayerCache;
import se.gorymoon.bingo.utils.Util;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TeamGui extends BaseBingoGui {

    private GuiTextField teamName;
    private IBingoTeam team;
    private GuiButtonExt saveButton;
    private TextFormatting teamColor = TextFormatting.WHITE;
    private int colorIndex = 0;
    private TeamListGui teamList;
    private IBingoTeam.MembershipStatus playerStatus;
    private GuiTextField inviteField;
    private GuiButtonExt inviteButton;

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        UUID player = Util.getUUID(mc().player);
        Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeamFromPlayer(player);
        if (!optional.isPresent()) {
            getGui().navigateBack();
            return;
        }
        team = optional.get();
        playerStatus = team.getStatus(player).get();
        teamColor = team.getColor();
        colorIndex = teamColor.getColorIndex();

        List<String> leaveTooltip = Lists.newArrayList("Leave Team", TextFormatting.DARK_GRAY + "You will be added to your own team when you leave this");
        if (playerStatus == IBingoTeam.MembershipStatus.OWNER) {
            leaveTooltip.add(TextFormatting.DARK_GRAY + "Ownership of team will be given to another member if present");
        }
        TabGui tabLeave = new TabGui(getLeft() + 7, getTop(), 224, 66, leaveTooltip) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                Optional<IBingoTeam> teamOptional = TeamManager.INSTANCE.getTeamFromPlayer(getGui().playerID);
                teamOptional.ifPresent(team -> {
                    NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.kick(team.getTeamId(), getGui().playerID));
                    NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.add(mc().player.getGameProfile().getName()));
                    getGui().navigateBack();
                });
            }
        };

        saveButton = new GuiButtonExt(nextId.get(), getLeft() + 5, getTop() + 48, 100, 15, "Save Settings") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NBTTagCompound nbt = team.serializeNBT();
                nbt.putString("Name", teamName.getText());
                nbt.putInt("Color", teamColor.getColorIndex());
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.update(team.getTeamId(), nbt));
            }
        };
        saveButton.enabled = false;
        saveButton.visible = playerStatus == IBingoTeam.MembershipStatus.OWNER;

        teamName = new GuiTextField(nextId.get(), fontRenderer(), getLeft() + 5, getTop() + 26, 150, 20) {
            @Override
            public boolean canFocus() {
                return playerStatus == IBingoTeam.MembershipStatus.OWNER;
            }
        };
        teamName.setText(team.getTeamName());
        teamName.setTextFormatter((s, integer) -> teamColor + s);
        teamName.setTextAcceptHandler((integer, s) -> updateSaveButton());
        teamName.setEnabled(playerStatus == IBingoTeam.MembershipStatus.OWNER);

        inviteButton = new GuiButtonExt(nextId.get(), getLeft() + 157, getTop() + 65, 63, 20, "Invite") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                UUID uuid = PlayerCache.INSTANCE.getUUID(inviteField.getText());
                if (uuid != null) {
                    NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.invite(team.getTeamId(), uuid));
                    inviteField.setText("");
                }
            }
        };
        inviteButton.enabled = false;
        inviteButton.visible = playerStatus == IBingoTeam.MembershipStatus.OWNER;

        inviteField = new GuiTextField(nextId.get(), fontRenderer(), getLeft() + 5, getTop() + 65, 150, 20);
        inviteField.setTextAcceptHandler((integer, s) -> inviteButton.enabled = !s.isEmpty());
        inviteField.setVisible(playerStatus == IBingoTeam.MembershipStatus.OWNER);

        TabGui tabInvites = new TabGui(getLeft() + getWidth() - 31, getTop(), 224, 98, ImmutableList.of("View Team Invites")) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                getGui().setCurrentScreen(new InvitesGui());
            }
        };

        teamList = new TeamListGui(this, player, playerStatus, team);

        buttonConsumer.accept(saveButton);
        buttonConsumer.accept(inviteButton);
        listenerConsumer.accept(teamName);
        listenerConsumer.accept(inviteField);
        listenerConsumer.accept(teamList);
        listenerConsumer.accept(addTab(tabLeave));
        listenerConsumer.accept(addTab(tabInvites));
        listenerConsumer.accept(addTab(getBackTab()));
    }

    @Override
    public void show() {}

    @Override
    public void hide() {}

    @Override
    public void tick() {
        if (getGui().getFocused() != this && !teamName.isFocused() && !inviteField.isFocused()) {
            getGui().focusOn(this);
        }
        teamName.tick();
        inviteField.tick();
    }

    @Override
    public boolean charTyped(char key, int modifiers) {
        if (teamName.isFocused()) {
            teamName.charTyped(key, modifiers);
            return true;
        }
        if (inviteField.isFocused()) {
            inviteField.charTyped(key, modifiers);
            return true;
        }
        return super.charTyped(key, modifiers);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (teamName.isFocused()) {
            teamName.keyPressed(key, scanCode, modifiers);
            return true;
        }
        if (inviteField.isFocused()) {
            inviteField.keyPressed(key, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInColorArea(mouseX, mouseY) && playerStatus == IBingoTeam.MembershipStatus.OWNER) {
            TextFormatting col;
            while ((col = TextFormatting.fromColorIndex(button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? ++colorIndex: --colorIndex)) == null || !col.isColor()) {
                if (colorIndex == -1) {
                    colorIndex = 16;
                    continue;
                }
                if (col == null) {
                    colorIndex = 0;
                }
            }
            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            teamColor = col;
            updateSaveButton();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double scroll) {
        MouseHelper mouseHelper = mc().mouseHelper;
        MainWindow mw = mc().mainWindow;
        int mouseX = (int)(mouseHelper.getMouseX() * (double)mw.getScaledWidth() / (double)mw.getWidth());
        int mouseY = (int)(mouseHelper.getMouseY() * (double)mw.getScaledHeight() / (double)mw.getHeight());
        return isInList(mouseX, mouseY) && teamList.mouseScrolled(scroll);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return isInList(mouseX, mouseY) && teamList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return teamList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isInList(double mouseX, double mouseY) {
        return isPointInRegion(teamList.x, teamList.y, teamList.width + 7, teamList.height, mouseX, mouseY);
    }

    @Override
    public void drawBackground(float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(partialTicks, mouseX, mouseY);

        GlStateManager.disableLighting();
        teamName.drawTextField(mouseX, mouseY, partialTicks);
        inviteField.drawTextField(mouseX, mouseY, partialTicks);

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int color = isInColorArea(mouseX, mouseY) ? 0xFFDDDDDD: 0xFFAAAAAA;
        drawRect(getLeft() + 157, getTop() + 25, getLeft() + 220, getTop() + 47, color);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawRect(getLeft() + 158, getTop() + 26, getLeft() + 219, getTop() + 46, 0xFF000000 | teamColor.getColor());
        GlStateManager.enableLighting();

        teamList.draw(mouseX, mouseY);
    }

    private void updateSaveButton() {
        saveButton.enabled = !teamName.getText().equals(team.getTeamName()) || teamColor != team.getColor();
    }

    private boolean isInColorArea(double mouseX, double mouseY) {
        return isPointInRegion(getLeft() + 157, getTop() + 25, 63, 22, mouseX, mouseY);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        if (isInColorArea(mouseX, mouseY)) {
            drawHoveringText("Team color: " + teamColor + WordUtils.capitalize(teamColor.getFriendlyName().replaceAll("_", " ")), mouseX, mouseY);
        }
        if (saveButton.enabled && isPointInRegion(getLeft() + 5, getTop() + 26, 150, 20, mouseX, mouseY)) {
            drawHoveringText("Save team settings", mouseX, mouseY);
        }
        if (isPointInRegion(teamName.x, teamName.y, teamName.width, teamName.height, mouseX, mouseY)) {
            drawHoveringText("Team name", mouseX, mouseY);
        }
        if (isPointInRegion(inviteField.x, inviteField.y, inviteField.width, inviteField.height, mouseX, mouseY)) {
            drawHoveringText("Username to invite", mouseX, mouseY);
        }

        teamList.drawHovering(mouseX, mouseY);

        super.drawForeground(mouseX, mouseY);
    }
}

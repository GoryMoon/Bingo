package se.gorymoon.bingo.client.gui.screens.team;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.client.gui.BingoGui;
import se.gorymoon.bingo.client.gui.IBingoGui;
import se.gorymoon.bingo.client.gui.ListGui;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamActionMessage;
import se.gorymoon.bingo.utils.PlayerCache;

import java.util.List;
import java.util.UUID;

public class TeamListGui extends ListGui {

    private final UUID player;
    private final IBingoTeam.MembershipStatus playerStatus;
    private final IBingoTeam team;

    public TeamListGui(IBingoGui base, UUID player, IBingoTeam.MembershipStatus playerStatus, IBingoTeam team) {
        super(base, base.getLeft() + 4, base.getTop() + 87, 209, 23);
        this.player = player;
        this.playerStatus = playerStatus;
        this.team = team;

        List<UUID> members = team.getMembers();
        for (int i = 0, membersSize = members.size(); i < membersSize; i++) {
            UUID uuid = members.get(i);
            addEntry(new TeamEntry(this, base, uuid, team.getColor()));
        }
    }

    public class TeamEntry implements IEntry {

        private final TeamListGui listGui;
        private final IBingoGui base;
        private final UUID uuid;
        private final TextFormatting color;
        private int x;
        private int y;

        public TeamEntry(TeamListGui listGui, IBingoGui base, UUID uuid, TextFormatting color) {
            this.listGui = listGui;
            this.base = base;
            this.uuid = uuid;
            this.color = color;
        }

        public void drawEntry(int x, int y, int width, int height, double mouseX, double mouseY) {
            this.x = x;
            this.y = y;

            base.getGui().texture().bindTexture(BingoGui.TEXTURE);
            base.getGui().drawTexturedModalRect(x, y, 0, 221, width, height);
            int nameWidth = 201;
            if (playerStatus == IBingoTeam.MembershipStatus.OWNER || player.equals(uuid)) {
                nameWidth = 181;
                int texX = listGui.isMouseInList(mouseX, mouseY) && base.getGui().isPointInRegion(x + 186, y + 2, 19, 19, mouseX, mouseY) ? 82: 63;
                base.getGui().drawTexturedModalRect(x + 186, y + 2, texX, 200, 19, 19);
                base.getGui().drawTexturedModalRect(x + 188, y + 4, 131, 200, 15, 15);
            }

            FontRenderer fontRenderer = base.getGui().fontRenderer();
            fontRenderer.drawString(fontRenderer.trimStringToWidth(PlayerCache.INSTANCE.getName(uuid), nameWidth), x + 4, y + 1 + (height / 2) - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
        }

        @Override
        public void drawHovering(double mouseX, double mouseY) {
            if (inButtonRegion(mouseX, mouseY) && (playerStatus == IBingoTeam.MembershipStatus.OWNER || player.equals(uuid))) {
                List<String> messages = player.equals(uuid) ? Lists.newArrayList("Leave the team", TextFormatting.DARK_GRAY + "You will be added to your own team when you leave this"): ImmutableList.of("Kick " + color + "[" + PlayerCache.INSTANCE.getName(uuid) + "]" + TextFormatting.RESET + " from team");
                if (player.equals(uuid) && playerStatus == IBingoTeam.MembershipStatus.OWNER) {
                    messages.add(TextFormatting.DARK_GRAY + "Ownership of team will be given to another member if present");
                }
                base.getGui().drawHoveringText(messages, (int)mouseX - base.getLeft(), (int)mouseY - base.getTop());
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (inButtonRegion(mouseX, mouseY)) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.kick(team.getTeamId(), uuid));
                listGui.removeEntry(this);
            }
            return false;
        }

        private boolean inButtonRegion(double mouseX, double mouseY) {
            return base.getGui().isPointInRegion(x + 186, y + 2, 19, 19, mouseX, mouseY);
        }
    }

}

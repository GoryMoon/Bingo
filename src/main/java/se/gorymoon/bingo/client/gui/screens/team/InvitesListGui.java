package se.gorymoon.bingo.client.gui.screens.team;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.client.gui.BingoGui;
import se.gorymoon.bingo.client.gui.IBingoGui;
import se.gorymoon.bingo.client.gui.ListGui;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamActionMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvitesListGui extends ListGui {

    public InvitesListGui(IBingoGui base, UUID player) {
        super(base, base.getLeft() + 4, base.getTop() + 87, 209, base.getGui().fontRenderer().FONT_HEIGHT * 3);

        List<UUID> teams = TeamManager.INSTANCE.getPartyInvites(player);
        for (int i = 0, membersSize = teams.size(); i < membersSize; i++) {
            Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeam(teams.get(i));
            if (optional.isPresent()) {
                addEntry(new InviteEntry(this, base, optional.get()));
            }
        }
    }

    public class InviteEntry implements IEntry {

        private final InvitesListGui listGui;
        private final IBingoGui base;
        private final IBingoTeam team;

        public InviteEntry(InvitesListGui listGui, IBingoGui base, IBingoTeam team) {
            this.listGui = listGui;
            this.base = base;
            this.team = team;
        }

        public void drawEntry(int x, int y, int width, int height, double mouseX, double mouseY) {
            base.getGui().texture().bindTexture(BingoGui.TEXTURE);
            base.getGui().drawTexturedModalRect(x, y, 0, 221, width, height);
            int nameWidth = 161;
            int texX = listGui.isMouseInList(mouseX, mouseY) && base.getGui().isPointInRegion(x + 186, y + 2, 19, 19, mouseX, mouseY) ? 82: 63;
            base.getGui().drawTexturedModalRect(x + 186, y + 2, texX, 200, 19, 19);
            base.getGui().drawTexturedModalRect(x + 188, y + 4, 101, 200, 15, 15);

            texX = listGui.isMouseInList(mouseX, mouseY) && base.getGui().isPointInRegion(x + 166, y + 2, 19, 19, mouseX, mouseY) ? 82: 63;
            base.getGui().drawTexturedModalRect(x + 166, y + 2, texX, 200, 19, 19);
            base.getGui().drawTexturedModalRect(x + 168, y + 4, 116, 200, 15, 15);

            FontRenderer fontRenderer = base.getGui().fontRenderer();
            fontRenderer.drawString(fontRenderer.trimStringToWidth(team.getColor() + team.getTeamName(), nameWidth), x + 4, y - 1 + (height / 2) - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFFFF);
        }

        @Override
        public void drawHovering(double mouseX, double mouseY) {
            if (inButtonRegion(166, mouseX, mouseY)) {
                base.getGui().drawHoveringText(ImmutableList.of("Accept invite", TextFormatting.DARK_GRAY + "You will leave your team when accepting"), (int)mouseX - base.getLeft(), (int)mouseY - base.getTop());
            }
            if (inButtonRegion(186, mouseX, mouseY)) {
                base.getGui().drawHoveringText("Deny Invite", (int)mouseX - base.getLeft(), (int)mouseY - base.getTop());
            }
        }

        private boolean inButtonRegion(int xx, double mouseX, double mouseY) {
            return base.getGui().isPointInRegion(x + xx, y + 2, 19, 19, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (inButtonRegion(166, mouseX, mouseY)) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.join(team.getTeamId()));
                base.getGui().goToGui("team");
            }
            if (inButtonRegion(186, mouseX, mouseY)) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.deny(team.getTeamId()));
                listGui.removeEntry(this);
            }
            return false;
        }
    }

}

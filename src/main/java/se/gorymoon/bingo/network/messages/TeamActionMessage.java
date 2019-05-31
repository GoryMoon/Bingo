package se.gorymoon.bingo.network.messages;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.api.IBingoTeam.MembershipStatus;
import se.gorymoon.bingo.game.team.BingoTeam;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.utils.Util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class TeamActionMessage {

    private final Action action;
    private final UUID teamId;
    private final NBTTagCompound data;

    public TeamActionMessage(Action action, UUID teamId, NBTTagCompound data) {
        this.action = action;
        this.teamId = teamId;
        this.data = data;
    }

    public static TeamActionMessage add(String name) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putString("Name", name);
        return new TeamActionMessage(Action.ADD, Util.DUMMY_UUID, nbt);
    }

    public static TeamActionMessage kick(UUID team, UUID target) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putUniqueId("Target", target);
        return new TeamActionMessage(Action.KICK, team, nbt);
    }

    public static TeamActionMessage update(UUID team, NBTTagCompound nbt) {
        return new TeamActionMessage(Action.EDIT, team, nbt);
    }

    public static TeamActionMessage invite(UUID team, UUID target) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putUniqueId("Target", target);
        return new TeamActionMessage(Action.INVITE, team, nbt);
    }

    public static TeamActionMessage join(UUID team) {
        return new TeamActionMessage(Action.JOIN, team, new NBTTagCompound());
    }

    public static TeamActionMessage deny(UUID team) {
        return new TeamActionMessage(Action.DENY, team, new NBTTagCompound());
    }

    public static TeamActionMessage decode(PacketBuffer buf) {
        return new TeamActionMessage(Action.getFromId(buf.readInt()), buf.readUniqueId(), buf.readCompoundTag());
    }

    public static void encode(TeamActionMessage message, PacketBuffer buf) {
        buf.writeInt(message.action.id);
        buf.writeUniqueId(message.teamId);
        buf.writeCompoundTag(message.data);
    }

    public static void handle(TeamActionMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            EntityPlayerMP player = ctx.get().getSender();
            NBTTagCompound data = message.data;

            UUID senderID = Util.getUUID(player);
            UUID targetID = null;
            MembershipStatus status = MembershipStatus.NONE;
            Optional<IBingoTeam> teamOptional;

            if (message.action == Action.JOIN || message.action == Action.DENY) {
                teamOptional = TeamManager.INSTANCE.getTeam(message.teamId);
            } else {
                teamOptional = TeamManager.INSTANCE.getTeamFromPlayer(senderID);
            }
            if (teamOptional.isPresent()) {
                IBingoTeam team = teamOptional.get();
                status = team.getStatus(senderID).orElse(MembershipStatus.NONE);
            }

            if (data.hasUniqueId("Target")) {
                targetID = data.getUniqueId("Target");
            }

            if (message.action == Action.ADD && !teamOptional.isPresent()) {
                String name = data.getString("Name");
                BingoTeam team = new BingoTeam();
                team.setName(name);
                team.inviteMember(senderID);
                TeamManager.INSTANCE.add(team);
                TeamManager.INSTANCE.sendSync();
            } else if (message.action == Action.REMOVE && teamOptional.isPresent() && status == MembershipStatus.OWNER) {
                TeamManager.INSTANCE.remove(teamOptional.get());
                TeamManager.INSTANCE.sendSync();
            } else if (message.action == Action.EDIT && teamOptional.isPresent() && status == MembershipStatus.OWNER) {
                IBingoTeam team = teamOptional.get();
                team.deserializeNBT(data);
                team.sendSync();
            } else if (message.action == Action.KICK && teamOptional.isPresent() && targetID != null && (status == MembershipStatus.OWNER || targetID.equals(senderID))) {
                IBingoTeam team = teamOptional.get();
                team.removePlayer(targetID);
                GameHandler.INSTANCE.teleportToSpawn(targetID);
                if (!targetID.equals(senderID)) {
                    GameHandler.INSTANCE.sendMessageTo(targetID, GameHandler.getMessage("You have been kicked from " + team.getColor() + "[" + team.getTeamName() + "]"));
                }
            } else if (message.action == Action.JOIN && teamOptional.isPresent() && status == MembershipStatus.INVITED) {
                Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeamFromPlayer(senderID);
                optional.ifPresent(team -> team.removePlayer(senderID));

                teamOptional.get().updatePlayer(senderID, MembershipStatus.MEMBER);
            } else if (message.action == Action.DENY && teamOptional.isPresent() && status == MembershipStatus.INVITED) {
                teamOptional.get().removePlayer(senderID);
            } else if (message.action == Action.INVITE && teamOptional.isPresent() && targetID != null && status == MembershipStatus.OWNER) {
                IBingoTeam team = teamOptional.get();
                team.inviteMember(targetID);
                ITextComponent textComponent = GameHandler.getMessage("You have been invited to join " + team.getColor() + "[" + team.getTeamName() + "] ");
                Style style = new Style();
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bingo gui invites"));
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to show your invites")));
                style.setColor(TextFormatting.GOLD);
                textComponent.appendSibling(new TextComponentString("[Open]").setStyle(style));
                GameHandler.INSTANCE.sendMessageTo(targetID,  textComponent);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Action {
        ADD(0),
        REMOVE(1),
        EDIT(2),
        KICK(3),
        JOIN(4),
        DENY(5),
        INVITE(6);

        private static final Action[] INDEX = Arrays.stream(values()).sorted(Comparator.comparing(Action::getId)).toArray(Action[]::new);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        private int getId() {
            return id;
        }

        public static Action getFromId(int id) {
            return INDEX[Math.abs(id % INDEX.length)];
        }
    }
}

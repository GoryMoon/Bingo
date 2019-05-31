package se.gorymoon.bingo.network.messages;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.events.TeamSyncEvent;
import se.gorymoon.bingo.game.team.BingoTeam;
import se.gorymoon.bingo.game.team.TeamManager;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class TeamSyncMessage {

    private UUID teamUUID;
    private NBTTagCompound payload;

    public TeamSyncMessage(UUID teamUUID, NBTTagCompound payload) {
        this.teamUUID = teamUUID;
        this.payload = payload;
    }

    public static TeamSyncMessage decode(PacketBuffer buf) {
        return new TeamSyncMessage(buf.readUniqueId(), buf.readCompoundTag());
    }

    public static void encode(TeamSyncMessage message, PacketBuffer buf) {
        buf.writeUniqueId(message.teamUUID);
        buf.writeCompoundTag(message.payload);
    }

    public static void handle(TeamSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeam(message.teamUUID);
            IBingoTeam team = optional.orElseGet(() -> TeamManager.INSTANCE.add(message.teamUUID, new BingoTeam()));
            team.deserializeNBT(message.payload);
            MinecraftForge.EVENT_BUS.post(new TeamSyncEvent(team));
        });
        ctx.get().setPacketHandled(true);
    }

}

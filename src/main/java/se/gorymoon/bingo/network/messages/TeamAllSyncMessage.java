package se.gorymoon.bingo.network.messages;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.game.events.TeamSyncEvent;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.network.NetworkManager;

import java.util.function.Supplier;

public class TeamAllSyncMessage {

    private NBTTagCompound payload;

    public TeamAllSyncMessage(NBTTagCompound payload) {
        this.payload = payload;
    }

    public static TeamAllSyncMessage decode(PacketBuffer buf) {
        return new TeamAllSyncMessage(buf.readCompoundTag());
    }

    public static void encode(TeamAllSyncMessage message, PacketBuffer buf) {
        buf.writeCompoundTag(message.payload);
    }

    public static void handle(TeamAllSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                // Request
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), TeamManager.INSTANCE.getSyncMessage());
            } else {
                TeamManager.INSTANCE.deserializeNBT(message.payload.getList("Data", Constants.NBT.TAG_COMPOUND));
                MinecraftForge.EVENT_BUS.post(new TeamSyncEvent(null));
            }
        });
        ctx.get().setPacketHandled(true);
    }

}

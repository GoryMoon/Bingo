package se.gorymoon.bingo.network.messages;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.events.BoardSyncEvent;
import se.gorymoon.bingo.network.NetworkManager;

import java.util.function.Supplier;

public class BoardSyncMessage {

    private final NBTTagCompound payload;

    public BoardSyncMessage(NBTTagCompound payload) {
        this.payload = payload;
    }

    public BoardSyncMessage() {
        this.payload = new NBTTagCompound();
    }

    public static BoardSyncMessage decode(PacketBuffer buf) {
        return new BoardSyncMessage(buf.readCompoundTag());
    }

    public static void encode(BoardSyncMessage message, PacketBuffer buf) {
        buf.writeCompoundTag(message.payload);
    }

    public static void handle(BoardSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                // Request
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), BoardManager.INSTANCE.getSyncMessage());
            } else {
                BoardManager.INSTANCE.deserializeNBT(message.payload);
                MinecraftForge.EVENT_BUS.post(new BoardSyncEvent());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

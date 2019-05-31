package se.gorymoon.bingo.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.game.board.BoardManager;

import java.util.function.Supplier;

public class BoardTimeSyncMessage {

    private final boolean running;
    private final long runTime;

    public BoardTimeSyncMessage(boolean running, long runTime) {
        this.running = running;
        this.runTime = runTime;
    }

    public static BoardTimeSyncMessage decode(PacketBuffer buf) {
        return new BoardTimeSyncMessage(buf.readBoolean(), buf.readLong());
    }

    public static void encode(BoardTimeSyncMessage message, PacketBuffer buf) {
        buf.writeBoolean(message.running);
        buf.writeLong(message.runTime);
    }

    public static void handle(BoardTimeSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            BoardManager.INSTANCE.setRunning(message.running);
            BoardManager.INSTANCE.setRunTime(message.runTime);
        });
        ctx.get().setPacketHandled(true);
    }
}

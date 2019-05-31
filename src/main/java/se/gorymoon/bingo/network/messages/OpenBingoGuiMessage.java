package se.gorymoon.bingo.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.client.gui.BingoGui;

import java.util.function.Supplier;

public class OpenBingoGuiMessage {

    private final String tab;

    public OpenBingoGuiMessage() {
        this("");
    }
    public OpenBingoGuiMessage(String tab) {
        this.tab = tab;
    }

    public static OpenBingoGuiMessage decode(PacketBuffer buf) {
        return new OpenBingoGuiMessage(buf.readString(10));
    }

    public static void encode(OpenBingoGuiMessage message, PacketBuffer buf) {
        buf.writeString(message.tab, 20);
    }

    public static void handle(OpenBingoGuiMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> BingoGui.open(message.tab));
        ctx.get().setPacketHandled(true);
    }

}

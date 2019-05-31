package se.gorymoon.bingo.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.api.BingoApi;
import se.gorymoon.bingo.api.IWorldStorage;

import java.util.function.Supplier;

public class SyncWorldStorageMessage {

    private NBTTagCompound storageNBT;

    public SyncWorldStorageMessage(IWorldStorage storage) {
        this.storageNBT = storage.serializeNBT();
    }

    public SyncWorldStorageMessage(NBTTagCompound storageNBT) {
        this.storageNBT = storageNBT;
    }

    public static SyncWorldStorageMessage decode(PacketBuffer buf) {
        return new SyncWorldStorageMessage(buf.readCompoundTag());
    }

    public static void encode(SyncWorldStorageMessage message, PacketBuffer buf) {
        buf.writeCompoundTag(message.storageNBT);
    }

    public static void handle(SyncWorldStorageMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().world.getCapability(BingoApi.WORLD_STORAGE_CAPABILITY).ifPresent(worldStorage -> {
                worldStorage.deserializeNBT(message.storageNBT);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}

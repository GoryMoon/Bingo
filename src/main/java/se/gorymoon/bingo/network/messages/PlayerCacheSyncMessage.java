package se.gorymoon.bingo.network.messages;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;
import se.gorymoon.bingo.utils.PlayerCache;

import java.util.function.Supplier;

public class PlayerCacheSyncMessage {

    private NBTTagCompound payload;

    public PlayerCacheSyncMessage(NBTTagList list) {
        this.payload = new NBTTagCompound();
        this.payload.put("Data", list);
    }

    public PlayerCacheSyncMessage(NBTTagCompound payload) {
        this.payload = payload;
    }

    public static PlayerCacheSyncMessage decode(PacketBuffer buf) {
        return new PlayerCacheSyncMessage(buf.readCompoundTag());
    }

    public static void encode(PlayerCacheSyncMessage message, PacketBuffer buf) {
        buf.writeCompoundTag(message.payload);
    }

    public static void handle(PlayerCacheSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerCache.INSTANCE.deserializeNBT(message.payload.getList("Data", Constants.NBT.TAG_COMPOUND));
        });
        ctx.get().setPacketHandled(true);
    }

}

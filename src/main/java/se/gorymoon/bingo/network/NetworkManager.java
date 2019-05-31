package se.gorymoon.bingo.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.network.messages.*;

import java.util.Objects;

public class NetworkManager {

    private static ResourceLocation id = new ResourceLocation(Bingo.MOD_ID, "net");

    public static SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(id)
            .clientAcceptedVersions(s -> Objects.equals(s, "1"))
            .serverAcceptedVersions(s -> Objects.equals(s, "1"))
            .networkProtocolVersion(() -> "1")
            .simpleChannel();

    public static void register() {
        INSTANCE.registerMessage(0, OpenBingoGuiMessage.class, OpenBingoGuiMessage::encode, OpenBingoGuiMessage::decode, OpenBingoGuiMessage::handle);
        INSTANCE.registerMessage(1, SyncWorldStorageMessage.class, SyncWorldStorageMessage::encode, SyncWorldStorageMessage::decode, SyncWorldStorageMessage::handle);
        INSTANCE.registerMessage(2, PlayerCacheSyncMessage.class, PlayerCacheSyncMessage::encode, PlayerCacheSyncMessage::decode, PlayerCacheSyncMessage::handle);

        INSTANCE.registerMessage(3, BoardActionMessage.class, BoardActionMessage::encode, BoardActionMessage::decode, BoardActionMessage::handle);
        INSTANCE.registerMessage(4, BoardSyncMessage.class, BoardSyncMessage::encode, BoardSyncMessage::decode, BoardSyncMessage::handle);
        INSTANCE.registerMessage(5, BoardTimeSyncMessage.class, BoardTimeSyncMessage::encode, BoardTimeSyncMessage::decode, BoardTimeSyncMessage::handle);

        INSTANCE.registerMessage(6, TeamActionMessage.class, TeamActionMessage::encode, TeamActionMessage::decode, TeamActionMessage::handle);
        INSTANCE.registerMessage(7, TeamSyncMessage.class, TeamSyncMessage::encode, TeamSyncMessage::decode, TeamSyncMessage::handle);
        INSTANCE.registerMessage(8, TeamAllSyncMessage.class, TeamAllSyncMessage::encode, TeamAllSyncMessage::decode, TeamAllSyncMessage::handle);
    }

}

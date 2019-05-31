package se.gorymoon.bingo.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.PlayerCacheSyncMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache implements INBTSerializable<NBTTagList> {

    public static final PlayerCache INSTANCE = new PlayerCache();

    private final Map<UUID, NBTTagCompound> cache = new ConcurrentHashMap<>();

    public void setName(UUID uuid, String name) {
        if (uuid == null || name == null) return;

        synchronized (cache) {
            NBTTagCompound tag = cache.get(uuid);

            if (tag == null) {
                tag = new NBTTagCompound();
                tag.putBoolean("IsOp", false);
                cache.put(uuid, tag);
            }

            tag.putString("Name", name);
        }
    }

    public String getName(UUID uuid) {
        if (uuid == null) return null;

        synchronized (cache) {
            if (!cache.containsKey(uuid)) {
                return uuid.toString();
            } else {
                return cache.get(uuid).getString("Name");
            }
        }
    }

    public UUID getUUID(String name) {
        if (name == null || name.isEmpty()) return null;

        synchronized (cache) {
            for (Map.Entry<UUID, NBTTagCompound> entry: cache.entrySet()) {
                if (entry.getValue().getString("Name").equalsIgnoreCase(name)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public boolean isOp(UUID uuid) {
        if (uuid == null) return false;

        synchronized (cache) {
            if (!cache.containsKey(uuid)) {
                return false;
            } else {
                return cache.get(uuid).getBoolean("IsOp");
            }
        }
    }

    public void updateNames(MinecraftServer server) {
        for (EntityPlayerMP player: server.getPlayerList().getPlayers()) {
            GameProfile profile = player == null ? null : player.getGameProfile();

            if (profile != null) {
                synchronized (cache) {
                    UUID oldId = getUUID(profile.getName());
                    while (oldId != null) {
                        cache.remove(oldId);
                        oldId = getUUID(profile.getName());
                    }

                    NBTTagCompound tag = new NBTTagCompound();
                    tag.putString("Name", profile.getName());
                    tag.putBoolean("IsOp", server.getPlayerList().canSendCommands(profile) || server.isSinglePlayer());
                    cache.put(profile.getId(), tag);
                }
            }
        }
        sendSync();
    }

    public int size() {
        synchronized (cache) {
            return cache.size();
        }
    }

    public void sendSync() {
        NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), new PlayerCacheSyncMessage(serializeNBT()));
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList nbt = new NBTTagList();
        synchronized (cache) {
            for (Map.Entry<UUID, NBTTagCompound> entry: cache.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.putUniqueId("Uuid", entry.getKey());
                tag.putString("Name", entry.getValue().getString("Name"));
                tag.putBoolean("IsOp", entry.getValue().getBoolean("IsOp"));

                nbt.add(tag);
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        synchronized (cache) {
            cache.clear();
            for (int i = 0; i < nbt.size(); i++) {
                NBTTagCompound tag = nbt.getCompound(i);
                UUID uuid = tag.getUniqueId("Uuid");

                NBTTagCompound newTag = new NBTTagCompound();
                newTag.putString("Name", tag.getString("Name"));
                newTag.putBoolean("IsOp", tag.getBoolean("IsOp"));
                cache.put(uuid, newTag);
            }
        }
    }

    public void reset() {
        synchronized (cache) {
            cache.clear();
        }
    }
}

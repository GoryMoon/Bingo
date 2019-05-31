package se.gorymoon.bingo.game.team;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamSyncMessage;

import java.util.*;

public class BingoTeam implements IBingoTeam {

    private static final Logger LOGGER = LogManager.getLogger();

    private UUID uuid = UUID.randomUUID();
    private String name = "";
    private TextFormatting color = TextFormatting.RED;

    private Map<UUID, MembershipStatus> members = new HashMap<>();
    private List<UUID> cache = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getTeamId() {
        return uuid;
    }

    @Override
    public String getTeamName() {
        return name;
    }

    @Override
    public TextFormatting getColor() {
        return color;
    }

    @Override
    public void inviteMember(UUID uuid) {
        //if (members.containsKey(uuid)) return;

        if (members.size() == 0) {
            members.put(uuid, MembershipStatus.OWNER);
            BoardManager.INSTANCE.sendSync();
        } else {
            members.put(uuid, MembershipStatus.INVITED);
        }

        updateCache();
        sendSync();
    }

    @Override
    public void removePlayer(UUID uuid) {
        if (!members.containsKey(uuid)) return;

        MembershipStatus status = members.get(uuid);

        if (members.remove(uuid) == null) {
            LOGGER.error("Couldn't remove player {} from team {}", uuid, getTeamName());
            return;
        }

        if (members.size() <= 0) {
            TeamManager.INSTANCE.remove(this);
            TeamManager.INSTANCE.sendSync();
        } else if (status == MembershipStatus.OWNER) {
            migrate();
        }
        updateCache();
        sendSync();
    }

    @Override
    public void updatePlayer(UUID uuid, MembershipStatus status) {
        if (!members.containsKey(uuid)) return;

        MembershipStatus prev = members.get(uuid);
        if (prev == status) return;

        members.put(uuid, status);

        if (status == MembershipStatus.OWNER) {
            for (Map.Entry<UUID, MembershipStatus> entry: members.entrySet()) {
                if (entry.getKey() != uuid && entry.getValue() == MembershipStatus.OWNER) {
                    members.put(entry.getKey(), MembershipStatus.MEMBER);
                }
            }
        } else if (prev == MembershipStatus.OWNER) {
            Optional<UUID> next = members.keySet().stream().findFirst();
            if (next.isPresent()) {
                members.put(next.get(), MembershipStatus.OWNER);
            } else {
                members.put(uuid, status);
            }
        }
        BoardManager.INSTANCE.sendSync();
        updateCache();
        sendSync();
    }

    @Override
    public Optional<MembershipStatus> getStatus(UUID uuid) {
        return Optional.ofNullable(members.get(uuid));
    }

    @Override
    public List<UUID> getMembers() {
        return cache;
    }

    private void updateCache() {
        cache.clear();
        members.entrySet().stream().filter(entry -> entry.getValue() != MembershipStatus.INVITED).forEach(entry -> cache.add(entry.getKey()));
    }

    private void migrate() {
        for (MembershipStatus status: members.values()) {
            if (status == MembershipStatus.OWNER) {
                return;
            }
        }

        Optional<UUID> next = members.keySet().stream().findFirst();
        if (next.isPresent()) {
            members.put(next.get(), MembershipStatus.OWNER);
        } else {
            LOGGER.error("Failed to migrate owner, should not happen.");
        }
    }

    @Override
    public void sendSync() {
        NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), new TeamSyncMessage(getTeamId(), serializeNBT()));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putUniqueId("Id", uuid);
        nbt.putString("Name", name);
        nbt.putInt("Color", color.getColorIndex());

        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<UUID, MembershipStatus> entry: members.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.putUniqueId("Uuid", entry.getKey());
            compound.putInt("Status", entry.getValue().getId());
            tagList.add(compound);
        }
        nbt.put("Members", tagList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        uuid = nbt.getUniqueId("Id");
        name = nbt.getString("Name");
        color = TextFormatting.fromColorIndex(nbt.getInt("Color"));

        members.clear();
        NBTTagList nbtList = nbt.getList("Members", Constants.NBT.TAG_COMPOUND);
        for (INBTBase base: nbtList) {
            NBTTagCompound compound = (NBTTagCompound) base;
            if (compound.hasUniqueId("Uuid") && compound.contains("Status", Constants.NBT.TAG_INT)) {
                UUID id = compound.getUniqueId("Uuid");
                MembershipStatus status = MembershipStatus.getFromId(compound.getInt("Status"));
                members.put(id, status);
            }
        }
        updateCache();
    }

    @Override
    public String toString() {
        return "BingoTeam{" + "uuid=" + uuid + ", name='" + name + '\'' + ", members=" + members + ", cache=" + cache + '}';
    }
}

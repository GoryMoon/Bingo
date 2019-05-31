package se.gorymoon.bingo.game.team;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.api.IBingoTeamManager;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamAllSyncMessage;

import java.util.*;
import java.util.stream.Collectors;

public class TeamManager implements IBingoTeamManager {

    public static final TeamManager INSTANCE = new TeamManager();

    private final Map<UUID, IBingoTeam> teams = Collections.synchronizedMap(new HashMap<>());
    private final HashMap<UUID, UUID> playerCache = new HashMap<>();

    @Override
    public Optional<IBingoTeam> getTeam(UUID uuid) {
        return Optional.ofNullable(teams.get(uuid));
    }

    @Override
    public Optional<IBingoTeam> getTeamFromPlayer(UUID uuid) {
        synchronized (playerCache) {
            UUID cachedId = playerCache.get(uuid);
            IBingoTeam cachedTeam = cachedId == null ? null: teams.get(cachedId);
            if (cachedId != null && cachedTeam == null) {
                playerCache.remove(uuid); // Removed team
            } else if (cachedTeam != null) {
                Optional<IBingoTeam.MembershipStatus> status = cachedTeam.getStatus(uuid);
                if (status.isPresent() && status.get() != IBingoTeam.MembershipStatus.INVITED) return Optional.of(cachedTeam);
                playerCache.remove(uuid); // Isn't member anymore
            }
        }

        synchronized (teams) {
            for (Map.Entry<UUID, IBingoTeam> entry : teams.entrySet()) {
                Optional<IBingoTeam.MembershipStatus> status = entry.getValue().getStatus(uuid);
                if (status.isPresent() && status.get() != IBingoTeam.MembershipStatus.INVITED) {
                    playerCache.put(uuid, entry.getKey());
                    return Optional.of(entry.getValue());
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<UUID> getPartyInvites(UUID uuid) {
        ArrayList<UUID> invites;
        synchronized (teams) {
            invites = teams.entrySet().stream()
                    .filter(entry -> entry.getValue().getStatus(uuid).orElse(IBingoTeam.MembershipStatus.NONE) == IBingoTeam.MembershipStatus.INVITED)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return invites;
    }

    public IBingoTeam add(IBingoTeam team) {
        return add(team.getTeamId(), team);
    }

    public IBingoTeam add(UUID teamUUID, IBingoTeam team) {
        teams.put(teamUUID, team);
        return team;
    }

    public boolean remove(UUID uuid) {
        synchronized (teams) {
            return teams.remove(uuid) != null;
        }
    }

    public boolean remove(IBingoTeam team) {
        synchronized (teams) {
            return teams.values().removeIf(entry -> entry == team);
        }
    }

    public int size() {
        return teams.size();
    }

    public List<IBingoTeam> getTeams() {
        return ImmutableList.copyOf(teams.values());
    }

    @Override
    public void sendSync() {
        NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), getSyncMessage());
    }

    public TeamAllSyncMessage getSyncMessage() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.put("Data", serializeNBT());
        return new TeamAllSyncMessage(nbt);
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList nbt = new NBTTagList();
        synchronized (teams) {
            for (IBingoTeam team: teams.values()) {
                nbt.add(team.serializeNBT());
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        clear();
        for (INBTBase base: nbt) {
            if (base instanceof NBTTagCompound) {
                BingoTeam team = new BingoTeam();
                team.deserializeNBT((NBTTagCompound) base);
                if (team.getMembers().size() > 0) {
                    teams.put(team.getTeamId(), team);
                }
            }
        }
    }

    public void clear() {
        synchronized (teams) {
            teams.clear();
        }
        synchronized (playerCache) {
            playerCache.clear();
        }
    }
}

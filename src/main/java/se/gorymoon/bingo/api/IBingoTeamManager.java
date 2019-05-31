package se.gorymoon.bingo.api;

import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBingoTeamManager extends INBTSerializable<NBTTagList> {

    Optional<IBingoTeam> getTeam(UUID uuid);

    Optional<IBingoTeam> getTeamFromPlayer(UUID uuid);

    List<UUID> getPartyInvites(UUID uuid);

    void sendSync();
}

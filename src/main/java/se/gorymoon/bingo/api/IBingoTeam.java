package se.gorymoon.bingo.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public interface IBingoTeam extends INBTSerializable<NBTTagCompound> {

    UUID getTeamId();

    String getTeamName();

    TextFormatting getColor();

    void inviteMember(UUID uuid);

    void removePlayer(UUID uuid);

    void updatePlayer(UUID uuid, MembershipStatus status);

    Optional<MembershipStatus> getStatus(UUID uuid);

    List<UUID> getMembers();

    void sendSync();

    String toString();

    enum MembershipStatus {
        NONE(0),
        OWNER(1),
        MEMBER(2),
        INVITED(3);

        private static final MembershipStatus[] INDEX = Arrays.stream(values()).sorted(Comparator.comparing(MembershipStatus::getId)).toArray(MembershipStatus[]::new);
        private final int id;

        MembershipStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static MembershipStatus getFromId(int id) {
            return INDEX[Math.abs(id % INDEX.length)];
        }
    }
}

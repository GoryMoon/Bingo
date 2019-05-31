package se.gorymoon.bingo.game.events;

import net.minecraftforge.eventbus.api.Event;
import se.gorymoon.bingo.api.IBingoTeam;

public class TeamSyncEvent extends Event {

    private final IBingoTeam team;

    public TeamSyncEvent(IBingoTeam team) {
        this.team = team;
    }

    public IBingoTeam getTeam() {
        return team;
    }
}

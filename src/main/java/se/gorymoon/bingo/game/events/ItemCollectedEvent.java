package se.gorymoon.bingo.game.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import se.gorymoon.bingo.api.IBingoTeam;

public class ItemCollectedEvent extends Event {

    private final ItemStack stack;
    private final IBingoTeam team;
    private final EntityPlayerMP player;

    public ItemCollectedEvent(ItemStack stack, IBingoTeam team, EntityPlayerMP player) {
        this.stack = stack;
        this.team = team;
        this.player = player;
    }

    public IBingoTeam getTeam() {
        return team;
    }

    public ItemStack getStack() {
        return stack;
    }

    public EntityPlayerMP getPlayer() {
        return player;
    }
}

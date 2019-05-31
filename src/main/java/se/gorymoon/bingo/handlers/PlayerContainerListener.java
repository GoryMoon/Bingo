package se.gorymoon.bingo.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.events.ItemCollectedEvent;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.utils.Util;

import java.util.HashMap;
import java.util.UUID;

public class PlayerContainerListener implements IContainerListener {

    private static final HashMap<UUID, PlayerContainerListener> LISTENER_MAP = new HashMap<>();
    private EntityPlayerMP player;

    public PlayerContainerListener(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        if (!BoardManager.INSTANCE.isRunning()) return;
        BoardManager.INSTANCE.getBingoBoard().ifPresent(bingoBoard -> {
            TeamManager.INSTANCE.getTeamFromPlayer(Util.getUUID(player)).ifPresent(team -> {
                if (bingoBoard.completeItem(stack, team)) {
                    MinecraftForge.EVENT_BUS.post(new ItemCollectedEvent(stack, team, player));
                }
            });
        });
    }

    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}

    @Override
    public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}

    public static void updateListener(EntityPlayerMP player) {
        UUID uuid = Util.getUUID(player);
        PlayerContainerListener listener = LISTENER_MAP.get(uuid);
        if (listener != null) {
            listener.player = player;
        } else {
            listener = new PlayerContainerListener(player);
            LISTENER_MAP.put(uuid, listener);
        }

        try {
            player.inventoryContainer.addListener(listener);
        } catch (Exception ignored) {}
    }
}

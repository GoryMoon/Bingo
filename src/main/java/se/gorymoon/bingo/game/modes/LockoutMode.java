package se.gorymoon.bingo.game.modes;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.item.ItemStack;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BingoBoard;
import se.gorymoon.bingo.game.team.TeamManager;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class LockoutMode extends NormalMode {

    LockoutMode(String description) {
        super("lockout", description);
    }

    @Override
    public String getName() {
        return "Lockout";
    }

    @Override
    public boolean handleItem(ItemStack stack, IBingoTeam team, IBingoBoard board) {
        boolean completed = false;
        for (IBingoItem item : board.getItems()) {
            if (!item.isCompleted() && checkItem(stack, item.getStack())) {
                item.complete(team);
                completed = true;
            }
        }
        return completed;
    }

    @Override
    public Optional<IBingoTeam> handleCompletion(IBingoBoard board) {
        Map<IBingoTeam, Integer> map = new Object2IntArrayMap<>();
        for (IBingoItem item : board.getItems()) {
            if (item.getCompletedBy().size() > 0) {
                IBingoTeam team = item.getCompletedBy().get(0);
                if (map.computeIfPresent(team, (t, i) -> i + 1) == null) {
                    map.put(team, 1);
                }
            }
        }

        double teams = TeamManager.INSTANCE.size();
        int majority = (int) Math.ceil(((double) BingoBoard.BOARD_TOTAL_SIZE) / (teams <= 0 ? 1.0D : teams));

        Optional<Map.Entry<IBingoTeam, Integer>> entry = map.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
        if (entry.isPresent() && entry.get().getValue() >= majority) {
            return Optional.of(entry.get().getKey());
        }
        return super.handleCompletion(board);
    }
}

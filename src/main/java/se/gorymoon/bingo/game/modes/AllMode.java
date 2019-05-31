package se.gorymoon.bingo.game.modes;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BingoBoard;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class AllMode extends NormalMode {

    public AllMode(String description) {
        super("all", description);
    }

    @Override
    public String getName() {
        return "All";
    }

    @Override
    public Optional<IBingoTeam> handleCompletion(IBingoBoard board) {
        Map<IBingoTeam, Integer> map = new Object2IntArrayMap<>();
        for (IBingoItem item : board.getItems()) {
            if (!item.isCompleted())
                return Optional.empty();
            for (IBingoTeam team : item.getCompletedBy()) {
                if (map.computeIfPresent(team, (t, i) -> i + 1) == null) {
                    map.put(team, 1);
                }
            }
        }
        Optional<Map.Entry<IBingoTeam, Integer>> entry = map.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
        if (entry.isPresent() && entry.get().getValue() == BingoBoard.BOARD_TOTAL_SIZE) {
            return Optional.of(entry.get().getKey());
        }
        return Optional.empty();
    }
}

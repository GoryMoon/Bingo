package se.gorymoon.bingo.game.board;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BoardNormalMode {

    private Map<UUID, Table<Integer, Integer, Integer>> boardTable = new HashMap<>();

    public void update(IBingoBoard board) {
        for (int row = 0; row < BingoBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < BingoBoard.BOARD_SIZE; col++) {
                Optional<IBingoItem> optional = board.getItem(row, col);
                if (optional.isPresent()) {
                    IBingoItem item = optional.get();

                    if (row == 0 && !item.isCompleted()) continue;
                    if (col == 0 && !item.isCompleted()) continue;

                    for (IBingoTeam team: item.getCompletedBy()) {
                        Table<Integer, Integer, Integer> table = boardTable.computeIfAbsent(team.getTeamId(), team1 -> {
                            TreeBasedTable<Integer, Integer, Integer> t = TreeBasedTable.create();
                            int size = BingoBoard.BOARD_SIZE;
                            for (int i = 0; i < BingoBoard.BOARD_TOTAL_SIZE; i++) {
                                t.put(i / size, i % size, 0);
                            }
                            return t;
                        });
                        table.put(row, col, 1);
                    }
                }
            }
        }
    }

    public Optional<UUID> getBingo() {
        for (Map.Entry<UUID, Table<Integer, Integer, Integer>> entry: boardTable.entrySet()) {
            Table<Integer, Integer, Integer> table = entry.getValue();
            if (table != null) {
                int dUp = 0;
                int dDown = 0;
                for (int i = 0; i < BingoBoard.BOARD_SIZE; i++) {
                    int sum = table.row(i).values().stream().mapToInt(Integer::intValue).sum();
                    if (sum >= BingoBoard.BOARD_SIZE) {
                        return Optional.of(entry.getKey());
                    }
                    sum = table.column(i).values().stream().mapToInt(Integer::intValue).sum();
                    if (sum >= BingoBoard.BOARD_SIZE) {
                        return Optional.of(entry.getKey());
                    }
                    dDown += table.get(i, i);
                    dUp += table.get((BingoBoard.BOARD_SIZE - 1) - i, i);
                }
                if (dUp >= BingoBoard.BOARD_SIZE || dDown >= BingoBoard.BOARD_SIZE) {
                    return Optional.of(entry.getKey());
                }
            }
        }
        return Optional.empty();
    }


    public void clear() {
        boardTable.clear();
    }

}

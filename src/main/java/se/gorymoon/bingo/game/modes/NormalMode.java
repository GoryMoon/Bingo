package se.gorymoon.bingo.game.modes;

import net.minecraft.item.ItemStack;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BoardNormalMode;
import se.gorymoon.bingo.game.team.TeamManager;

import java.util.Optional;
import java.util.UUID;

public class NormalMode extends BaseMode {

    private BoardNormalMode boardNormalMode = new BoardNormalMode();

    public NormalMode(String description) {
        super("normal", description);
    }

    public NormalMode(String id, String description) {
        super(id, description);
    }

    @Override
    public String getName() {
        return "Normal";
    }

    @Override
    public void clear() {
        boardNormalMode.clear();
    }

    @Override
    public boolean handleItem(ItemStack stack, IBingoTeam team, IBingoBoard board) {
        boolean completed = false;
        for (IBingoItem item : board.getItems()) {
            if (checkItem(stack, item.getStack())) {
                if (item.isCompleted() && item.getCompletedBy().contains(team))
                    continue;

                item.complete(team);
                completed = true;
            }
        }
        return completed;
    }

    @Override
    public Optional<IBingoTeam> handleCompletion(IBingoBoard board) {
        boardNormalMode.update(board);
        Optional<UUID> bingo = boardNormalMode.getBingo();
        if (bingo.isPresent()) {
            return TeamManager.INSTANCE.getTeam(bingo.get());
        }
        return Optional.empty();
    }
}

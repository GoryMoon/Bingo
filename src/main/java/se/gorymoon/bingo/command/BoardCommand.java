package se.gorymoon.bingo.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import se.gorymoon.bingo.api.IBingoEffect;
import se.gorymoon.bingo.api.IBingoMode;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.effects.BingoEffects;
import se.gorymoon.bingo.game.modes.BingoModes;
import se.gorymoon.bingo.handlers.GameHandler;

public class BoardCommand {

    static ArgumentBuilder<CommandSource, ?> register() {
        LiteralArgumentBuilder<CommandSource> modes = Commands.literal("mode");
        BingoModes.INSTANCE.getBingoModes().forEach(mode -> modes.then(
                Commands.literal(mode.getName().toLowerCase().replaceAll(" ", "_"))
                        .executes(ctx -> setMode(ctx.getSource(), mode))));

        LiteralArgumentBuilder<CommandSource> effects = Commands.literal("effect");
        BingoEffects.INSTANCE.getBingoEffects().forEach(effect -> effects.then(
                Commands.literal(effect.getName().toLowerCase().replaceAll(" ", "_"))
                        .executes(ctx -> setEffect(ctx.getSource(), effect))
        ));

        return Commands.literal("board")
                .requires(cs -> cs.hasPermissionLevel(4))
                .then(Commands.literal("start").executes(ctx -> start(ctx.getSource())))
                .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource())))
                .then(Commands.literal("generate").executes(ctx -> generate(ctx.getSource())))
                .then(modes)
                .then(effects);
    }

    private static int start(CommandSource cs) {
        if (BoardManager.INSTANCE.isRunning()) {
            cs.sendErrorMessage(GameHandler.getMessage("Can't start when game is already running", TextFormatting.RED));
            return 0;
        } else if (!BoardManager.INSTANCE.canStart()) {
            cs.sendErrorMessage(GameHandler.getMessage("Need to generate a new board before starting", TextFormatting.RED));
            return 0;
        } else {
            BoardManager.INSTANCE.setRunTime(0);
            BoardManager.INSTANCE.sendSync();
            GameHandler.INSTANCE.startGame(cs.getWorld());
            cs.sendFeedback(GameHandler.getMessage("Started the game"), false);
        }
        return 1;
    }

    private static int stop(CommandSource cs) {
        if (!BoardManager.INSTANCE.isRunning()) {
            cs.sendErrorMessage(GameHandler.getMessage("Can't stop when game isn't running", TextFormatting.RED));
            return 0;
        } else {
            BoardManager.INSTANCE.setRunning(false);
            GameHandler.INSTANCE.setRunning(false);
            GameHandler.INSTANCE.teleportAllToSpawn(null);
            BoardManager.INSTANCE.sendSync();
            cs.sendFeedback(GameHandler.getMessage("Stopped game"), false);
        }
        return 1;
    }

    private static int generate(CommandSource cs) {
        if (BoardManager.INSTANCE.isRunning()) {
            cs.sendErrorMessage(GameHandler.getMessage("Can't generate new board when game is running", TextFormatting.RED));
            return 0;
        } else {
            BoardManager.INSTANCE.generateBoard();
            cs.sendFeedback(GameHandler.getMessage("Generated a new board"), false);
        }
        return 1;
    }

    private static int setMode(CommandSource cs, IBingoMode mode) {
        if (BoardManager.INSTANCE.isRunning()) {
            cs.sendErrorMessage(GameHandler.getMessage("Can't change mode when game is running", TextFormatting.RED));
            return 0;
        } else {
            BoardManager.INSTANCE.setMode(mode);
            cs.sendFeedback(GameHandler.getMessage("Mode set to: " + TextFormatting.GOLD + mode.getName()), false);
        }
        return 1;
    }

    private static int setEffect(CommandSource cs, IBingoEffect effect) {
        if (BoardManager.INSTANCE.isRunning()) {
            cs.sendErrorMessage(GameHandler.getMessage("Can't change effect when game is running", TextFormatting.RED));
            return 0;
        } else {
            BoardManager.INSTANCE.setEffect(effect);
            cs.sendFeedback(GameHandler.getMessage("Effect set to: " + TextFormatting.GOLD + effect.getName()), false);
        }
        return 1;
    }
}

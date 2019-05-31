package se.gorymoon.bingo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public class BingoCommand {
    public BingoCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSource>literal("bingo")
                .then(GuiCommand.register())
                .then(BoardCommand.register())
        );
    }
}

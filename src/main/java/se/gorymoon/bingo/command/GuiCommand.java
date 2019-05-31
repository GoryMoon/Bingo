package se.gorymoon.bingo.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.OpenBingoGuiMessage;

public class GuiCommand {

    static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("gui")
                .requires(cs -> cs.hasPermissionLevel(0))
                .then(Commands.literal("invites").executes(ctx -> openGui(ctx.getSource(), "invites")))
                .then(Commands.literal("team").executes(ctx -> openGui(ctx.getSource(), "team")))
                .then(Commands.literal("help").executes(ctx -> openGui(ctx.getSource(), "help")))
                .then(Commands.literal("settings").executes(ctx -> openGui(ctx.getSource(), "settings")))
                .executes(ctx -> openGui(ctx.getSource(), ""));
    }

    private static int openGui(CommandSource cs, String tab) throws CommandSyntaxException {
        EntityPlayerMP player = cs.asPlayer();
        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenBingoGuiMessage(tab));
        return 1;
    }
}

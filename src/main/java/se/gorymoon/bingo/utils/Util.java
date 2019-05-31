package se.gorymoon.bingo.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.modes.BingoModes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Util {

    public static final UUID DUMMY_UUID = UUID.randomUUID();
    public static final String BINGO_TITLE = "§bB§aI§eN§dG§cO§r";

    public static UUID getUUID(EntityPlayer player) {
        if (player.getEntityWorld().isRemote()) {
            UUID uuid = PlayerCache.INSTANCE.getUUID(player.getGameProfile().getName());
            if (uuid != null) {
                return uuid;
            }
        }
        return player.getGameProfile().getId();
    }

    public static void addFireworksToPlayer(EntityPlayerMP playerMP, int amount) {
        playerMP.getEntityData().putInt("Fireworks", amount);
        playerMP.getEntityData().putBoolean("FireworkDelay", false);
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        fontRenderer.drawString(text, (float)(x - fontRenderer.getStringWidth(text) / 2), y, color);
    }

    public static void renderToolTip(ItemStack stack, int x, int y, GuiScreen screen) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        GuiUtils.preItemToolTip(stack);
        List<String> toolTip = screen.getItemToolTip(stack);
        Optional<IBingoBoard> board = BoardManager.INSTANCE.getBingoBoard();
        if (board.isPresent()) {
            NonNullList<IBingoItem> items = board.get().getItems();
            for (IBingoItem item: items) {
                if (item.isCompleted() && ItemStack.areItemStacksEqual(stack, item.getStack())){
                    if (BoardManager.INSTANCE.getMode() == BingoModes.LOCKOUT) {
                        toolTip.add(TextFormatting.RED + "Locked");
                    }
                    toolTip.add(TextFormatting.GOLD + "Completed by:");
                    for (IBingoTeam team: item.getCompletedBy()) {
                        toolTip.add(team.getColor() + "" + team.getTeamName() + TextFormatting.RESET);
                    }
                    break;
                }
            }
        }
        GuiUtils.drawHoveringText(toolTip, x, y, screen.width, screen.height, -1, (font == null ? screen.mc.fontRenderer : font));
        GuiUtils.postItemToolTip();
    }
}

package se.gorymoon.bingo.network.messages;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.effects.BingoEffects;
import se.gorymoon.bingo.game.modes.BingoModes;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.utils.PlayerCache;
import se.gorymoon.bingo.utils.Util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Supplier;

public class BoardActionMessage {

    private static final Logger LOGGER = LogManager.getLogger();
    private final Action action;
    private final NBTTagCompound data;

    public BoardActionMessage(Action action) {
        this(action, new NBTTagCompound());
    }

    public BoardActionMessage(Action action, NBTTagCompound data) {
        this.action = action;
        this.data = data;
    }

    public static BoardActionMessage generate() {
        return new BoardActionMessage(Action.GENERATE);
    }

    public static BoardActionMessage start() {
        return new BoardActionMessage(Action.START);
    }

    public static BoardActionMessage stop() {
        return new BoardActionMessage(Action.STOP);
    }

    public static BoardActionMessage teleport() {
        return new BoardActionMessage(Action.TELEPORT);
    }

    public static BoardActionMessage edit(ResourceLocation mode, ResourceLocation effect, String seed) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putString("Mode", mode.toString());
        nbt.putString("Effect", effect.toString());
        nbt.putString("Seed", seed);
        return new BoardActionMessage(Action.EDIT, nbt);
    }

    public static BoardActionMessage decode(PacketBuffer buf) {
        return new BoardActionMessage(Action.getFromId(buf.readInt()), buf.readCompoundTag());
    }

    public static void encode(BoardActionMessage message, PacketBuffer buf) {
        buf.writeInt(message.action.id);
        buf.writeCompoundTag(message.data);
    }

    public static void handle(BoardActionMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            EntityPlayerMP player = ctx.get().getSender();
            UUID uuid = Util.getUUID(player);
            boolean isOp = PlayerCache.INSTANCE.isOp(uuid);

            if (isOp) {
                BoardManager manager = BoardManager.INSTANCE;
                if (message.action == Action.GENERATE) {
                    if (!manager.isRunning()) {
                        manager.generateBoard();
                    }
                } else if (message.action == Action.START && manager.canStart()) {
                    manager.setRunTime(0);
                    manager.sendSync();
                    GameHandler.INSTANCE.startGame(ctx.get().getSender().getServerWorld());
                } else if (message.action == Action.STOP) {
                    manager.setRunning(false);
                    GameHandler.INSTANCE.setRunning(false);
                    GameHandler.INSTANCE.teleportAllToSpawn(null);
                    manager.sendSync();
                } else if (message.action == Action.EDIT && message.data.contains("Mode", Constants.NBT.TAG_STRING) && message.data.contains("Effect", Constants.NBT.TAG_STRING) && message.data.contains("Seed", Constants.NBT.TAG_STRING)) {
                    manager.setMode(BingoModes.INSTANCE.get(ResourceLocation.tryCreate(message.data.getString("Mode"))).orElse(BingoModes.NORMAL));
                    manager.setEffect(BingoEffects.INSTANCE.get(ResourceLocation.tryCreate(message.data.getString("Effect"))).orElse(BingoEffects.VANILLA));
                    manager.setSeed(message.data.getString("Seed"));
                    manager.sendSync();
                }
            }
            if (message.action == Action.TELEPORT) {
                GameHandler.INSTANCE.teleportToTeam(uuid);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Action {
        GENERATE(0),
        START(1),
        STOP(2),
        TELEPORT(3),
        EDIT(4);

        private static final Action[] INDEX = Arrays.stream(values()).sorted(Comparator.comparing(Action::getId)).toArray(Action[]::new);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        private int getId() {
            return id;
        }

        public static Action getFromId(int id) {
            return INDEX[Math.abs(id % INDEX.length)];
        }
    }
}

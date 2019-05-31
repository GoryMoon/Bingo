package se.gorymoon.bingo.game.board;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import se.gorymoon.bingo.BingoConfig;
import se.gorymoon.bingo.api.*;
import se.gorymoon.bingo.game.effects.BingoEffects;
import se.gorymoon.bingo.game.events.ItemCollectedEvent;
import se.gorymoon.bingo.game.modes.BingoModes;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.BoardSyncMessage;
import se.gorymoon.bingo.network.messages.BoardTimeSyncMessage;
import se.gorymoon.bingo.utils.StackListManager;
import se.gorymoon.bingo.utils.Util;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BoardManager implements IBingoBoardManager {

    public static final BoardManager INSTANCE = new BoardManager();
    private static final Random rand = new Random();

    private boolean isRunning = false;
    private long runTime = 0;
    private IBingoBoard board;
    private IBingoEffect activeEffect = BingoEffects.VANILLA;
    private IBingoMode activeMode = BingoModes.NORMAL;

    private boolean canStart = true;
    private String seed = "";
    private boolean isSeedSet = false;

    private BoardManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public long getRunTime() {
        return runTime;
    }

    public String getFormattedRunTime() {
        int seconds = (int) (runTime / 20);
        int minutes = (seconds % 3600) / 60;
        int hours = seconds / 3600;
        seconds %= 60;
        StringBuilder builder = new StringBuilder();
        if (hours > 0) builder.append(String.format("%02d:", hours));
        builder.append(String.format("%02d:%02d", minutes, seconds));
        return  builder.toString();
    }

    public void setRunTime(long runTime) {
        this.runTime = runTime;
        sendSync();
    }

    public boolean canStart() {
        return !isRunning && canStart;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void setRunning(boolean status) {
        if (!isRunning && status) {
            runTime = 0;
            canStart = false;
        }
        isRunning = status;
    }

    public BoardSyncMessage getSyncMessage() {
        return new BoardSyncMessage(serializeNBT());
    }

    @Override
    public void sendSync() {
        NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), getSyncMessage());
    }

    public void sendTimeSync() {
        NetworkManager.INSTANCE.send(PacketDistributor.ALL.noArg(), new BoardTimeSyncMessage(isRunning(), getRunTime()));
    }

    @Override
    public void tick() {
        if (isRunning) {
            runTime++;
            if (runTime % 20 == 0) {
                sendTimeSync();
            }
        }
    }

    @Override
    public Optional<IBingoBoard> getBingoBoard() {
        return Optional.ofNullable(board);
    }

    @Override
    public void generateBoard() {
        canStart = true;
        BingoBoard board = new BingoBoard();
        getRandom();

        AtomicBoolean allowedBook = new AtomicBoolean(false);
        List<? extends String> whitelist = BingoConfig.SERVER.whitelist.get();
        List<ItemStack> itemStacks = StackListManager.getItemStacks().stream()
                .filter(stack -> whitelist.contains(String.valueOf(stack.getItem().getRegistryName())))
                .filter(stack -> {
                    if (stack.getItem() instanceof ItemEnchantedBook && !BingoConfig.SERVER.enchantedBookUnique.get()) {
                        if (!allowedBook.get()) {
                            allowedBook.set(true);
                            return true;
                        }
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        for (int i = 0; i < BingoBoard.BOARD_TOTAL_SIZE; i++) {
            Optional<ItemStack> optional = itemStacks.stream().skip((long) (itemStacks.size() * rand.nextDouble())).findFirst();
            if (optional.isPresent()) {
                ItemStack stack = optional.get();
                if (stack.getItem() instanceof ItemEnchantedBook && !BingoConfig.SERVER.enchantedBookUnique.get()) {
                    board.addItem(new ItemStack(Items.ENCHANTED_BOOK));
                } else {
                    board.addItem(stack);
                }
                itemStacks.remove(stack);
            } else {
                i--;
            }
        }
        this.board = board;
        sendSync();
    }


    @Nonnull
    @Override
    public IBingoMode getMode() {
        return activeMode;
    }

    @Nonnull
    @Override
    public IBingoEffect getEffect() {
        return activeEffect;
    }

    public void setMode(IBingoMode mode) {
        this.activeMode = mode;
    }

    public void setEffect(IBingoEffect effect) {
        this.activeEffect = effect;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putBoolean("Running", isRunning);
        nbt.putLong("Time", runTime);
        nbt.putBoolean("CanStart", canStart);
        if (board != null) {
            nbt.put("Board", board.serializeNBT());
        }
        nbt.putString("Seed", seed);
        nbt.putBoolean("SeedSet", isSeedSet);
        nbt.putString("Mode", activeMode.getId().toString());
        nbt.putString("Effect", activeEffect.getId().toString());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        clear();
        isRunning = nbt.getBoolean("Running");
        runTime = nbt.getLong("Time");
        canStart = nbt.getBoolean("CanStart");
        if (nbt.contains("Board", Constants.NBT.TAG_COMPOUND)) {
            board = new BingoBoard();
            board.deserializeNBT(nbt.getCompound("Board"));
        }
        seed = nbt.getString("Seed");
        isSeedSet = nbt.getBoolean("SeedSet");
        activeMode = BingoModes.INSTANCE.get(ResourceLocation.tryCreate(nbt.getString("Mode"))).orElse(BingoModes.NORMAL);
        activeEffect = BingoEffects.INSTANCE.get(ResourceLocation.tryCreate(nbt.getString("Effect"))).orElse(BingoEffects.VANILLA);

        if (EffectiveSide.get() == LogicalSide.SERVER) {
            sendSync();
        }
    }

    public void clear() {
        isRunning = false;
        runTime = 0;
        board = null;
        activeMode.clear();
    }

    public Random getRandom() {
        long seed = (new Random()).nextLong();
        if (!StringUtils.isEmpty(this.seed) && isSeedSet) {
            try {
                long j = Long.parseLong(this.seed);
                if (j != 0L) {
                    seed = j;
                }
            } catch (NumberFormatException e) {
                seed = (long)this.seed.hashCode();
            }
        }
        this.seed = String.valueOf(seed);
        rand.setSeed(seed);
        return rand;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        isSeedSet = !StringUtils.isEmpty(seed);
        this.seed = seed;
    }

    private void onTeamWon() {
        isRunning = false;
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        server.getPlayerList().sendMessage(new TextComponentString(GameHandler.BINGO_HEAD + TextFormatting.GREEN + " Teleporting back in 10 seconds"));
        GameHandler.INSTANCE.setRunning(false);
        GameHandler.INSTANCE.setTeleportBackTimer(200);
        sendSync();
    }

    @SubscribeEvent
    public void onItemCollected(ItemCollectedEvent event) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        server.getPlayerList().sendMessage(getPlayerCollectedMessage(event.getTeam(), event.getStack()));
        EntityPlayerMP player = event.getPlayer();
        player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 20.0F, 1.0F);
        player.connection.netManager.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, player.posX, player.posY, player.posZ, 20.0F, 1.0F));

        Optional<IBingoTeam> optional = activeMode.handleCompletion(board);
        optional.ifPresent(team -> {
            server.getPlayerList().sendMessage(getTeamWonMessage(event.getTeam()));
            List<UUID> members = event.getTeam().getMembers();
            for (UUID uuid: members) {
                EntityPlayerMP playerMP = server.getPlayerList().getPlayerByUUID(uuid);
                if (playerMP != null) {
                    Util.addFireworksToPlayer(playerMP, 4);
                }
            }
            onTeamWon();
        });
        sendSync();
    }

    private ITextComponent getPlayerCollectedMessage(IBingoTeam team, ItemStack stack) {
        ITextComponent message = new TextComponentString(team.getTeamName()).setStyle(new Style().setColor(team.getColor()));
        message.appendSibling(new TextComponentString(" got item: ").setStyle(new Style().setColor(TextFormatting.GREEN)));
        message.appendSibling(stack.getTextComponent());
        return message;
    }

    private ITextComponent getTeamWonMessage(IBingoTeam team) {
        ITextComponent message = new TextComponentString(team.getTeamName()).setStyle(new Style().setColor(team.getColor()));
        ITextComponent sub = new TextComponentString(" got §bB§aI§eN§dG§cO§r on ").setStyle(new Style().setColor(TextFormatting.RESET));
        sub.appendSibling(new TextComponentString(activeMode.getName()).setStyle(new Style().setColor(TextFormatting.GOLD)));
        sub.appendSibling(new TextComponentString(" mode in: "));
        sub.appendSibling(new TextComponentString(getFormattedRunTime()).setStyle(new Style().setColor(TextFormatting.GOLD)));
        message.appendSibling(sub);
        return message;
    }
}

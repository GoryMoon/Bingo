package se.gorymoon.bingo.handlers;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.DeOpCommand;
import net.minecraft.command.impl.OpCommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.api.BingoApi;
import se.gorymoon.bingo.api.IWorldStorage;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.SyncWorldStorageMessage;
import se.gorymoon.bingo.utils.PlayerCache;
import se.gorymoon.bingo.utils.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Bingo.MOD_ID)
public class WorldHandler {

    @SubscribeEvent
    public static void attachWorldStorage(AttachCapabilitiesEvent<World> event) {
        if (event.getObject().getWorldType() == Bingo.BINGO_WORLD && event.getObject().getDimension().getType() == DimensionType.OVERWORLD) {
            event.addCapability(new ResourceLocation(Bingo.MOD_ID, "world_storage"), new ICapabilitySerializable<NBTTagCompound>() {

                private IWorldStorage instance = BingoApi.WORLD_STORAGE_CAPABILITY.getDefaultInstance();
                private LazyOptional<IWorldStorage> capability = LazyOptional.of(() -> instance);

                @Override
                public NBTTagCompound serializeNBT() {
                     return (NBTTagCompound) BingoApi.WORLD_STORAGE_CAPABILITY.writeNBT(this.instance, null);
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt) {
                    BingoApi.WORLD_STORAGE_CAPABILITY.readNBT(this.instance, null, nbt);
                }

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
                    return BingoApi.WORLD_STORAGE_CAPABILITY.orEmpty(cap, capability);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onServerStopped(FMLServerAboutToStartEvent event) {
        // Cleanup
        TeamManager.INSTANCE.clear();
        BoardManager.INSTANCE.clear();
        GameHandler.INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onEntityCreated(EntityJoinWorldEvent event) {
        if (!(event.getWorld().getWorldType() == Bingo.BINGO_WORLD) || !(event.getEntity() instanceof EntityPlayerMP) || event.getWorld().isRemote()) return;

        PlayerContainerListener.updateListener((EntityPlayerMP) event.getEntity());

        event.getWorld().getCapability(BingoApi.WORLD_STORAGE_CAPABILITY).ifPresent(worldStorage -> {
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (EntityPlayerMP) event.getEntity()), new SyncWorldStorageMessage(worldStorage));
        });
        if (TeamManager.INSTANCE.getTeamFromPlayer(Util.getUUID((EntityPlayer) event.getEntity())).isPresent()) {
            if (BoardManager.INSTANCE.isRunning()) {
                BoardManager.INSTANCE.getEffect().start().accept((EntityPlayer) event.getEntity());
            } else {
                BoardManager.INSTANCE.getEffect().revert().accept((EntityPlayer) event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            BoardManager.INSTANCE.tick();
            GameHandler.INSTANCE.tick(event.world);
            if (BoardManager.INSTANCE.isRunning()) {
                for (EntityPlayer player: event.world.getServer().getPlayerList().getPlayers()) {
                    BoardManager.INSTANCE.getEffect().tick().accept(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCreateSpawn(WorldEvent.CreateSpawnPosition event) {
        if (event.getSettings().getTerrainType() == Bingo.BINGO_WORLD) {
            event.setCanceled(true);

            final WorldServer world = (WorldServer) event.getWorld();
            world.getGameRules().setOrCreateGameRule("spawnRadius", "1", null);
            world.getWorldInfo().setSpawn(new BlockPos(7, world.getChunkProvider().chunkGenerator.getGroundHeight(), 3));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer().getEntityWorld().isRemote() || event.getPlayer().getServer() == null || !(event.getPlayer() instanceof EntityPlayerMP)) {
            return;
        } else if (EffectiveSide.get() == LogicalSide.CLIENT && !event.getPlayer().getServer().isDedicatedServer() && event.getPlayer().getServer().getServerOwner().equals(event.getPlayer().getGameProfile().getName())) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.getPlayer();
        PlayerCache.INSTANCE.updateNames(player.getServer());

        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), TeamManager.INSTANCE.getSyncMessage());
        NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), BoardManager.INSTANCE.getSyncMessage());
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

        Command<CommandSource> command = event.getParseResults().getContext().getCommand();
        if(server != null && (command instanceof OpCommand || command instanceof DeOpCommand)) {
            PlayerCache.INSTANCE.updateNames(server);
        }
    }
}

package se.gorymoon.bingo.handlers;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.utils.Util;

import java.util.*;
import java.util.stream.Collectors;

public class GameHandler implements INBTSerializable<NBTTagCompound> {

    public static final GameHandler INSTANCE = new GameHandler();
    private static final int TELEPORT_WAIT = 300;

    private boolean isRunning;
    private long ticks = 0;
    private int teleportBackTimer = -1;

    public static final String BINGO_HEAD = "§6[" + Util.BINGO_TITLE + "§6]§r";
    private MinecraftServer server;
    private static Map<IBingoTeam, Position> positionMap = new HashMap<>();
    private static final Set<SPacketPlayerPosLook.EnumFlags> TELEPORT_FLAGS = EnumSet.of(SPacketPlayerPosLook.EnumFlags.Y, SPacketPlayerPosLook.EnumFlags.X_ROT, SPacketPlayerPosLook.EnumFlags.Y_ROT);

    public void tick(World world) {
        if (server == null) {
            server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        }
        if (isRunning) {
            ticks++;
            if (ticks == 20) {
                server.getPlayerList().sendMessage(getMessage("Waiting for world to load around players"));
            }
            if (!isWaiting()) {
                if (ticks == TELEPORT_WAIT) {
                    sendTitleMessage(world, "GO!", TextFormatting.GREEN);
                    BoardManager.INSTANCE.setRunning(true);
                }
            } else if (TELEPORT_WAIT - ticks <= 100 && ticks % 20 == 0) {
                sendTitleMessage(world, String.valueOf((TELEPORT_WAIT - ticks) / 20), TextFormatting.RED);
            }
            if (isWaiting()) {
                for (IBingoTeam team: TeamManager.INSTANCE.getTeams()) {
                    Position position = positionMap.get(team);
                    if (position != null) {
                        for (UUID uuid : team.getMembers()) {
                            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                            if (player != null) {
                                player.connection.setPlayerLocation(MathHelper.floor(position.x) + 0.5D, position.y, MathHelper.floor(position.z) + 0.5D, player.rotationYaw, player.rotationPitch, TELEPORT_FLAGS);
                            }
                        }
                    }
                }
            }
        }
        if (teleportBackTimer > 0) {
            teleportBackTimer--;
        } else if (teleportBackTimer == 0) {
            teleportAllToSpawn(null);
            teleportBackTimer = -1;
        }
    }

    private void sendTitleMessage(World world, String message, TextFormatting color) {
        for (EntityPlayerMP player: world.getServer().getPlayerList().getPlayers()) {
            player.connection.sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString(message).setStyle(new Style().setColor(color))));
        }
    }

    public boolean isWaiting() {
        return isRunning && ticks < TELEPORT_WAIT;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
        ticks = 0;
    }

    public void clear() {
        isRunning = false;
        ticks = 0;
        teleportBackTimer = -1;
    }

    public void setTeleportBackTimer(int teleportBackTimer) {
        this.teleportBackTimer = teleportBackTimer;
    }

    public void sendMessageTo(UUID uuid, ITextComponent message) {
        server.getPlayerList().getPlayerByUUID(uuid).sendStatusMessage(message, false);
    }

    public static ITextComponent getMessage(String message) {
        return getMessage(message, TextFormatting.GREEN);
    }

    public static ITextComponent getMessage(String message, TextFormatting color) {
        return new TextComponentString(BINGO_HEAD + color  + " " + message);
    }

    public void startGame(WorldServer world) {
        server.getPlayerList().sendMessage(getMessage("Teleporting players to start area, hold on..."));
        for(WorldServer worldserver : server.getWorlds()) {
            worldserver.setDayTime(200);
        }
        Random rand = BoardManager.INSTANCE.getRandom();

        clearAllInventories();
        float distance = 5000000;
        Vec2f center = new Vec2f(MathHelper.nextFloat(rand, -distance, distance), MathHelper.nextFloat(rand, -distance, distance));
        spreadPlayers(rand, world, center, 50, 500);
        setRunning(true);
    }

    public void teleportToTeam(UUID target) {
        PlayerList players = server.getPlayerList();
        EntityPlayer player = players.getPlayerByUUID(target);

        Optional<IBingoTeam> optional = TeamManager.INSTANCE.getTeamFromPlayer(target);
        if (player != null) {
            optional.ifPresent(team -> {
                List<EntityPlayer> members = team.getMembers().stream().map(players::getPlayerByUUID).collect(Collectors.toList());
                for (EntityPlayer member: members) {
                    if (member != null && !member.equals(player)) {
                        player.sendMessage(getMessage("Teleporting to a team member"));
                        Position position = positionMap.get(team);
                        if (position != null) {
                            setSpawn(player, position.getBlockPos());
                        }
                        player.setPositionAndUpdate(member.posX, member.posY, member.posZ);
                        return;
                    }
                }
                Position position = positionMap.get(team);
                if (position != null) {
                    player.sendMessage(getMessage("Teleporting to team spawn"));
                    teleportAndSetSpawnPlayer(player, position.getBlockPos());
                    return;
                }
                player.sendMessage(getMessage("Could not teleport to team, no online members to teleport to."));
            });
        }
    }

    public void teleportToSpawn(UUID target) {
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(target);
        if (player != null) {
            teleportToSpawn(player);
        }
    }

    public void teleportAllToSpawn(UUID except) {
        List<IBingoTeam> teams = TeamManager.INSTANCE.getTeams();
        List<EntityPlayer> players = teams.stream().map(team -> team.getMembers().stream().filter(uuid -> !uuid.equals(except)).map(server.getPlayerList()::getPlayerByUUID).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
        for (EntityPlayer player: players) {
            if (player != null) {
                teleportToSpawn(player);
            }
        }
    }

    public void clearAllInventories() {
        for (IBingoTeam team: TeamManager.INSTANCE.getTeams()) {
            for (UUID uuid : team.getMembers()) {
                EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(uuid);
                if (player != null) {
                    player.inventory.clearMatchingItems(stack -> true, -1);
                }
            }
        }
    }

    private void teleportToSpawn(EntityPlayer player) {
        BlockPos pos = new BlockPos(7.5D, 65D, 3.5D);
        setSpawn(player, pos);
        player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        BoardManager.INSTANCE.getEffect().revert().accept(player);
    }

    private void spreadPlayers(Random random, WorldServer world, Vec2f center, double spreadDistance, float maxRange) {
        double d0 = (double)(center.x - maxRange);
        double d1 = (double)(center.y - maxRange);
        double d2 = (double)(center.x + maxRange);
        double d3 = (double)(center.y + maxRange);
        Position[] positions = getPositions(random, TeamManager.INSTANCE.size(), d0, d1, d2, d3);
        ensureSufficientSeparation(spreadDistance, world, random, d0, d1, d2, d3, positions);
        doSpreading(world, positions);
    }

    private void ensureSufficientSeparation(double spreadDistance, WorldServer world, Random random, double minX, double minZ, double maxX, double maxZ, Position[] positions) {
        boolean flag = true;
        double d0;

        int i;
        for(i = 0; i < 10000 && flag; ++i) {
            flag = false;
            d0 = (double)Float.MAX_VALUE;

            for(int j = 0; j < positions.length; ++j) {
                int k = 0;
                Position pos = positions[j];
                Position newPos = new Position();

                for(int l = 0; l < positions.length; ++l) {
                    if (j != l) {
                        Position position2 = positions[l];
                        double d1 = pos.getDistance(position2);
                        d0 = Math.min(d1, d0);
                        if (d1 < spreadDistance) {
                            ++k;
                            newPos.x = newPos.x + (position2.x - pos.x);
                            newPos.z = newPos.z + (position2.z - pos.z);
                        }
                    }
                }

                if (k > 0) {
                    newPos.x = newPos.x / (double)k;
                    newPos.z = newPos.z / (double)k;
                    double d2 = (double)newPos.getMagnitude();
                    if (d2 > 0.0D) {
                        newPos.normalize();
                        pos.subtract(newPos);
                    } else {
                        pos.computeCoords(random, minX, minZ, maxX, maxZ);
                    }

                    flag = true;
                }

                if (pos.clampWithinRange(minX, minZ, maxX, maxZ)) {
                    flag = true;
                }
            }

            if (!flag) {
                for(Position position : positions) {
                    if (!position.isLocationSafe(world)) {
                        position.computeCoords(random, minX, minZ, maxX, maxZ);
                        flag = true;
                    }
                }
            }
        }
    }

    private void doSpreading(WorldServer world, Position[] positions) {
        positionMap = new HashMap<>();

        List<IBingoTeam> teams = TeamManager.INSTANCE.getTeams();
        List<EntityPlayer> players = teams.stream().map(team -> team.getMembers().stream().map(world::getPlayerEntityByUUID).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());

        int i = 0;
        for(EntityPlayer player : players) {
            if (player == null) continue;
            IBingoTeam team = TeamManager.INSTANCE.getTeamFromPlayer(Util.getUUID(player)).get();
            if (!positionMap.containsKey(team)) {
                positionMap.put(team, positions[i++]);
            }
            Position position = positionMap.get(team);
            position.y = (double)position.getHighestNonAirBlock(world);
            teleportAndSetSpawnPlayer(player, position.getBlockPos());

            player.takeStat(StatList.CUSTOM.get(StatList.TIME_SINCE_REST));
            player.setHealth(player.getMaxHealth());
            player.getFoodStats().addStats(20, 20);
            BoardManager.INSTANCE.getEffect().start().accept(player);
        }
    }

    private void teleportAndSetSpawnPlayer(EntityPlayer player, BlockPos pos) {
        setSpawn(player, null);
        player.setPositionAndUpdate(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }

    private void setSpawn(EntityPlayer player, BlockPos pos) {
        player.setSpawnPoint(pos, true, DimensionType.OVERWORLD);
    }

    private static Position[] getPositions(Random random, int count, double minX, double minZ, double maxX, double maxZ) {
        Position[] positions = new Position[count];

        for(int i = 0; i < positions.length; ++i) {
            Position position = new Position();
            position.computeCoords(random, minX, minZ, maxX, maxZ);
            positions[i] = position;
        }

        return positions;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.putLong("Ticks", ticks);
        nbt.putInt("TeleportTimer", teleportBackTimer);
        nbt.putBoolean("IsRunning", isRunning);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ticks = nbt.getLong("Ticks");
        teleportBackTimer = nbt.getInt("TeleportTimer");
        isRunning = nbt.getBoolean("IsRunning");
    }

    static class Position {
        public double y;
        private double x;
        private double z;

        double getDistance(Position other) {
            double d0 = this.x - other.x;
            double d1 = this.z - other.z;
            return Math.sqrt(d0 * d0 + d1 * d1);
        }

        void normalize() {
            double d0 = (double)this.getMagnitude();
            this.x /= d0;
            this.z /= d0;
        }

        float getMagnitude() {
            return MathHelper.sqrt(this.x * this.x + this.z * this.z);
        }

        void subtract(Position other) {
            this.x -= other.x;
            this.z -= other.z;
        }

        boolean clampWithinRange(double minX, double minZ, double maxX, double maxZ) {
            boolean flag = false;
            if (this.x < minX) {
                this.x = minX;
                flag = true;
            } else if (this.x > maxX) {
                this.x = maxX;
                flag = true;
            }

            if (this.z < minZ) {
                this.z = minZ;
                flag = true;
            } else if (this.z > maxZ) {
                this.z = maxZ;
                flag = true;
            }

            return flag;
        }

        int getHighestNonAirBlock(IBlockReader world) {
            BlockPos blockpos = new BlockPos(this.x, 256.0D, this.z);

            while(blockpos.getY() > 0) {
                blockpos = blockpos.down();
                if (!world.getBlockState(blockpos).isAir(world, blockpos)) {
                    return blockpos.getY() + 1;
                }
            }

            return 257;
        }

        boolean isLocationSafe(IBlockReader world) {
            BlockPos blockpos = new BlockPos(this.x, 256.0D, this.z);

            while(blockpos.getY() > 0) {
                blockpos = blockpos.down();
                IBlockState iblockstate = world.getBlockState(blockpos);
                if (!iblockstate.isAir(world, blockpos)) {
                    Material material = iblockstate.getMaterial();
                    return !material.isLiquid();
                }
            }

            return false;
        }

        BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
        }

        void computeCoords(Random random, double minX, double minZ, double maxX, double maZx) {
            this.x = MathHelper.nextDouble(random, minX, maxX);
            this.z = MathHelper.nextDouble(random, minZ, maZx);
        }
    }

}

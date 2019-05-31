package se.gorymoon.bingo.world.capability;

import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import se.gorymoon.bingo.api.IWorldStorage;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.team.TeamManager;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.utils.PlayerCache;

import javax.annotation.Nullable;

public class WorldStorage implements IWorldStorage {

    private static final int STORAGE_VERSION = 1;

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.putInt("Version", STORAGE_VERSION);
        nbt.put("Teams", TeamManager.INSTANCE.serializeNBT());
        nbt.put("Board", BoardManager.INSTANCE.serializeNBT());
        nbt.put("Game", GameHandler.INSTANCE.serializeNBT());
        nbt.put("PlayerCache", PlayerCache.INSTANCE.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        PlayerCache.INSTANCE.deserializeNBT(nbt.getList("PlayerCache", Constants.NBT.TAG_COMPOUND));
        TeamManager.INSTANCE.deserializeNBT(nbt.getList("Teams", Constants.NBT.TAG_COMPOUND));
        BoardManager.INSTANCE.deserializeNBT(nbt.getCompound("Board"));
        GameHandler.INSTANCE.deserializeNBT(nbt.getCompound("Game"));
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IWorldStorage.class, new Capability.IStorage<IWorldStorage>() {
            @Nullable
            @Override
            public INBTBase writeNBT(Capability<IWorldStorage> capability, IWorldStorage instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IWorldStorage> capability, IWorldStorage instance, EnumFacing side, INBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.deserializeNBT((NBTTagCompound) nbt);
                }
            }
        }, WorldStorage::new);
    }
}

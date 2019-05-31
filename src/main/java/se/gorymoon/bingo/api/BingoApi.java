package se.gorymoon.bingo.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class BingoApi {

    @CapabilityInject(IWorldStorage.class)
    public static final Capability<IWorldStorage> WORLD_STORAGE_CAPABILITY = null;
}

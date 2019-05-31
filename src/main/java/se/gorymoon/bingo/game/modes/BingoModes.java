package se.gorymoon.bingo.game.modes;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.ResourceLocation;
import se.gorymoon.bingo.api.IBingoMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BingoModes {

    public static final BingoModes INSTANCE = new BingoModes();

    public static final IBingoMode NORMAL = INSTANCE.add(new NormalMode("First to 5 in a row wins"));
    public static final IBingoMode ALL = INSTANCE.add(new AllMode("First to collect all items wins"));
    public static final IBingoMode LOCKOUT = INSTANCE.add(new LockoutMode("First to 5 or the one that gets the majority of items wins\nCan't collect already collected item"));

    private final Map<ResourceLocation, IBingoMode> modes = new Object2ObjectArrayMap<>();

    public IBingoMode add(IBingoMode mode) {
        return add(mode.getId(), mode);
    }

    public IBingoMode add(ResourceLocation id, IBingoMode mode) {
        if (!modes.containsKey(id)) {
            modes.put(id, mode);
            return mode;
        } else {
            throw new RuntimeException("A BingoMode with id [" + id + "] is already added");
        }
    }

    public Optional<IBingoMode> get(ResourceLocation id) {
        return Optional.ofNullable(modes.get(id));
    }

    public List<IBingoMode> getBingoModes() {
        return new ArrayList<>(modes.values());
    }
}

package se.gorymoon.bingo.game.effects;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.ResourceLocation;
import se.gorymoon.bingo.api.IBingoEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BingoEffects {
    public static final BingoEffects INSTANCE = new BingoEffects();

    private Map<ResourceLocation, IBingoEffect> effects = new Object2ObjectArrayMap<>();

    public static final IBingoEffect VANILLA = INSTANCE.add(new VanillaEffect());
    public static final IBingoEffect NIGHT_VISION = INSTANCE.add(new NightVisionEffect());

    public IBingoEffect add(IBingoEffect mode) {
        return add(mode.getId(), mode);
    }

    public IBingoEffect add(ResourceLocation id, IBingoEffect mode) {
        if (!effects.containsKey(id)) {
            effects.put(id, mode);
            return mode;
        } else {
            throw new RuntimeException("A BingoMode with id [" + id + "] is already added");
        }
    }

    public Optional<IBingoEffect> get(ResourceLocation id) {
        return Optional.ofNullable(effects.get(id));
    }

    public List<IBingoEffect> getBingoEffects() {
        return new ArrayList<>(effects.values());
    }
}

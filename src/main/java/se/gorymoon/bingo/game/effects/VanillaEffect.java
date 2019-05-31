package se.gorymoon.bingo.game.effects;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class VanillaEffect extends BaseEffect {

    public VanillaEffect() {
        super("vanilla");
    }

    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public List<String> getDescription() {
        return ImmutableList.of("No special effect");
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> start() {
        return NOOP;
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> tick() {
        return NOOP;
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> revert() {
        return NOOP;
    }
}

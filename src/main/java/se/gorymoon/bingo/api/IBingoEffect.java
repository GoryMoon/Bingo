package se.gorymoon.bingo.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public interface IBingoEffect {

    Consumer<EntityPlayer> NOOP = player -> {};

    ResourceLocation getId();

    String getName();

    List<String> getDescription();

    @Nonnull
    Consumer<EntityPlayer> start();

    @Nonnull
    Consumer<EntityPlayer> tick();

    @Nonnull
    Consumer<EntityPlayer> revert();

}

package se.gorymoon.bingo.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Optional;

public interface IBingoMode {

    ResourceLocation getId();

    String getName();

    List<String> getDescription();

    default void clear() {}

    boolean handleItem(ItemStack stack, IBingoTeam team, IBingoBoard board);

    Optional<IBingoTeam> handleCompletion(IBingoBoard board);
}

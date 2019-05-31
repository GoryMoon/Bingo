package se.gorymoon.bingo.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public interface IBingoBoard extends INBTSerializable<NBTTagCompound> {

    NonNullList<IBingoItem> getItems();

    Optional<IBingoItem> getItem(int row, int col);

    boolean completeItem(ItemStack stack, IBingoTeam team);
}

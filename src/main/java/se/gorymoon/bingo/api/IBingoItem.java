package se.gorymoon.bingo.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.INBTSerializable;

public interface IBingoItem extends INBTSerializable<NBTTagCompound> {

    ItemStack getStack();

    boolean isCompleted();

    NonNullList<IBingoTeam> getCompletedBy();

    void complete(IBingoTeam team);

}

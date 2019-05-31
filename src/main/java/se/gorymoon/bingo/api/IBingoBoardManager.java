package se.gorymoon.bingo.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

public interface IBingoBoardManager extends INBTSerializable<NBTTagCompound> {

    void tick();

    Optional<IBingoBoard> getBingoBoard();

    long getRunTime();

    boolean isRunning();

    void setRunning(boolean status);

    void sendSync();

    void generateBoard();

    IBingoMode getMode();

    IBingoEffect getEffect();
}

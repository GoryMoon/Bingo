package se.gorymoon.bingo.game.board;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import se.gorymoon.bingo.api.IBingoBoard;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;

import java.util.Optional;
import java.util.stream.Collectors;

public class BingoBoard implements IBingoBoard {

    private NonNullList<IBingoItem> bingoItems = NonNullList.create();

    public static final int BOARD_SIZE = 5;
    public static final int BOARD_TOTAL_SIZE = BOARD_SIZE * BOARD_SIZE;

    @Override
    public NonNullList<IBingoItem> getItems() {
        return bingoItems;
    }

    @Override
    public Optional<IBingoItem> getItem(int row, int col) {
        if (col >= BOARD_SIZE || row >= BOARD_SIZE) return Optional.empty();
        int id = (row * BOARD_SIZE) + col;
        return Optional.of(bingoItems.get(id));
    }

    public void addItem(ItemStack stack) {
        bingoItems.add(new BingoItem(stack));
    }

    @Override
    public boolean completeItem(ItemStack stack, IBingoTeam team) {
        return BoardManager.INSTANCE.getMode().handleItem(stack, team, this);
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.put("Items", bingoItems.stream().map(INBTSerializable::serializeNBT).collect(Collectors.toCollection(NBTTagList::new)));

        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        NBTTagList nbtList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
        for (INBTBase base: nbtList) {
            BingoItem item = new BingoItem();
            item.deserializeNBT((NBTTagCompound) base);
            bingoItems.add(item);
        }
    }

    @Override
    public String toString() {
        return "BingoBoard{" + "bingoItems=" + bingoItems + '}';
    }
}

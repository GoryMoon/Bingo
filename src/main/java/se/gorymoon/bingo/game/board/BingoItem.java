package se.gorymoon.bingo.game.board;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import se.gorymoon.bingo.api.IBingoItem;
import se.gorymoon.bingo.api.IBingoTeam;
import se.gorymoon.bingo.game.team.TeamManager;

import java.util.UUID;

public class BingoItem implements IBingoItem {

    private ItemStack stack = ItemStack.EMPTY;
    private boolean isCompleted = false;
    private NonNullList<IBingoTeam> completedBy = NonNullList.create();

    public BingoItem(ItemStack stack) {
        this.stack = stack;
    }

    public BingoItem() {}

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public NonNullList<IBingoTeam> getCompletedBy() {
        return completedBy;
    }

    @Override
    public void complete(IBingoTeam team) {
        isCompleted = true;
        completedBy.add(team);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.put("Stack", stack.serializeNBT());
        nbt.putBoolean("Completed", isCompleted);
        NBTTagList teams = new NBTTagList();
        for (IBingoTeam team: completedBy) {
            NBTTagCompound teamNbt = new NBTTagCompound();
            teamNbt.putUniqueId("Id", team.getTeamId());
            teams.add(teamNbt);
        }
        nbt.put("Teams", teams);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        stack = ItemStack.read(nbt.getCompound("Stack"));
        isCompleted = nbt.getBoolean("Completed");
        NBTTagList nbtList = nbt.getList("Teams", Constants.NBT.TAG_COMPOUND);
        for (INBTBase base: nbtList) {
            UUID id = ((NBTTagCompound) base).getUniqueId("Id");
            TeamManager.INSTANCE.getTeam(id).ifPresent(completedBy::add);
        }
    }

    @Override
    public String toString() {
        return "BingoItem{" + "stack=" + stack + ", isCompleted=" + isCompleted + ", completedBy=" + completedBy + '}';
    }
}

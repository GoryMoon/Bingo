package se.gorymoon.bingo.game.modes;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.BingoConfig;
import se.gorymoon.bingo.api.IBingoMode;

import java.util.List;

public abstract class BaseMode implements IBingoMode {

    private final String description;
    private final ResourceLocation id;

    BaseMode(String id, String description) {
        this.id = new ResourceLocation(Bingo.MOD_ID, id);
        this.description = description;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<String> getDescription() {
        return Lists.newArrayList(description.split("\n"));
    }

    protected boolean checkItem(ItemStack stack, ItemStack boardStack) {
        if (!BingoConfig.SERVER.enchantedBookUnique.get() && stack.getItem() instanceof ItemEnchantedBook && boardStack.getItem() instanceof ItemEnchantedBook) {
            return true;
        }
        return ItemStack.areItemStacksEqual(stack, boardStack);
    }
}

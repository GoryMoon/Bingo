package se.gorymoon.bingo.utils;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemSpawnEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StackListManager {

    private static final Logger LOGGER = LogManager.getLogger();
    public static List<ItemStack> itemStacks = new ArrayList<>();

    public static void build() {
        Collection<Item> values = ForgeRegistries.ITEMS.getValues();
        for (Item item: values) {
            NonNullList<ItemStack> stacks = NonNullList.create();
            try {
                item.fillItemGroup(ItemGroup.SEARCH, stacks);
            } catch (Exception e) {
                LOGGER.error("Error while getting itemstacks from {}, will be missing in board", item.getRegistryName());
            }
            for (ItemStack stack: stacks) {
                if (stack.isEmpty()) {
                    LOGGER.error("Found empty itemstack from item: {}", item.getRegistryName());
                } else {
                    addItemStack(stack);
                }
            }
        }
    }

    private static void addItemStack(ItemStack stack) {
        // Game freezes when loading player skulls, see https://bugs.mojang.com/browse/MC-65587
        if (stack.getItem() == Items.PLAYER_HEAD) {
            return;
        }

        if (stack.getItem() instanceof ItemSpawnEgg) {
            return;
        }

        itemStacks.add(stack);
    }

    public static List<ItemStack> getItemStacks() {
        return Collections.unmodifiableList(itemStacks);
    }
}

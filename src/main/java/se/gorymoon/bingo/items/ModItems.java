package se.gorymoon.bingo.items;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.blocks.ModBlocks;

public class ModItems {

    @Mod.EventBusSubscriber(modid = Bingo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            Block block = ModBlocks.BINGO_TROPHY.orElse(null);
            event.getRegistry().register(new ItemBlock(block, new Item.Properties()).setRegistryName(block.getRegistryName()));
        }
    }
}

package se.gorymoon.bingo.blocks;


import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import se.gorymoon.bingo.Bingo;

public class ModBlocks {

    public static final RegistryObject<Block> BINGO_TROPHY = RegistryObject.of("bingo:bingo_trophy", () -> Block.class);
    public static TileEntityType<TrophyTileEntity> BINGO_TROPHY_TILE;


    @Mod.EventBusSubscriber(modid = Bingo.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(new TrophyBlock().setRegistryName(Bingo.MOD_ID, "bingo_trophy"));
        }

        @SubscribeEvent
        public static void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
            BINGO_TROPHY_TILE = TileEntityType.Builder.create(TrophyTileEntity::new).build(null);
            BINGO_TROPHY_TILE.setRegistryName(Bingo.MOD_ID, "trophy");
            event.getRegistry().register(BINGO_TROPHY_TILE);
        }
    }
}

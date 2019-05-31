package se.gorymoon.bingo;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import se.gorymoon.bingo.blocks.TrophyTileEntity;
import se.gorymoon.bingo.client.rendering.TrophyTESR;
import se.gorymoon.bingo.command.BingoCommand;
import se.gorymoon.bingo.handlers.GuiHandler;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.utils.StackListManager;
import se.gorymoon.bingo.world.capability.WorldStorage;
import se.gorymoon.bingo.world.gen.BingoChunkGenerator;
import se.gorymoon.bingo.world.gen.BingoOverworldGenSettings;
import se.gorymoon.bingo.world.gen.feature.SpawnPlaceStructure;


@Mod(Bingo.MOD_ID)
public class Bingo {

    public static final String MOD_ID = "bingo";
    public static WorldType BINGO_WORLD;

    public Bingo() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);

        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(ModConfig.Type.SERVER, BingoConfig.serverSpec);
        bus.register(BingoConfig.class);
    }

    private void setup(FMLCommonSetupEvent event) {
        NetworkManager.register();
        WorldStorage.register();
        DeferredWorkQueue.runLater(() -> {
            BINGO_WORLD = new WorldType("bingo") {
                @Override
                public IChunkGenerator<?> createChunkGenerator(World world) {
                    BingoOverworldGenSettings genSettings = new BingoOverworldGenSettings();
                    OverworldBiomeProviderSettings overworldbiomeprovidersettings = BiomeProviderType.VANILLA_LAYERED.createSettings().setWorldInfo(world.getWorldInfo()).setGeneratorSettings(genSettings);
                    return new BingoChunkGenerator(world, BiomeProviderType.VANILLA_LAYERED.create(overworldbiomeprovidersettings), genSettings);
                }
            };
            SpawnPlaceStructure.registerStructure();
            SpawnPlaceStructure.registerStructurePieces();
            new Thread(StackListManager::build).start();
        });
    }

    private void serverStarting(FMLServerStartingEvent event) {
        new BingoCommand(event.getCommandDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class ClientRegistration {

        @SubscribeEvent
        public static void onClientRegistartion(FMLClientSetupEvent event) {
            GuiHandler.showPopup = new KeyBinding("Show bingo board", GLFW.GLFW_KEY_B, "Bingo");
            ClientRegistry.registerKeyBinding(GuiHandler.showPopup);
            GuiHandler.showBingo = new KeyBinding("Show bingo interface", GLFW.GLFW_KEY_V, "Bingo");
            ClientRegistry.registerKeyBinding(GuiHandler.showBingo);

            ClientRegistry.bindTileEntitySpecialRenderer(TrophyTileEntity.class, new TrophyTESR());
        }

    }
}

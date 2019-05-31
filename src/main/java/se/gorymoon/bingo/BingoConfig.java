package se.gorymoon.bingo;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

import static net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import static net.minecraftforge.common.ForgeConfigSpec.Builder;

public class BingoConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> DEFAULT_ITEMS;

    static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;
    public static ModConfig serverConfig;

    static {
        DEFAULT_ITEMS = create(
                "diamond", "bone", "ender_pearl", "fern", "brick", "melon_slice", "ink_sac", "apple", "flint", "diamond_hoe", "gray_dye",
                "vine", "flower_pot", "book", "golden_shovel", "diamond_axe", "slime_ball", "dead_bush", "glistering_melon_slice",
                "writable_book", "golden_apple", "flint_and_steel", "spruce_sapling", "name_tag", "milk_bucket", "sign", "golden_sword",
                "hopper", "saddle", "egg", "item_frame", "clock", "acacia_sapling", "enchanted_book", "cake", "painting", "powered_rail",
                "hopper_minecart", "cocoa_beans", "pumpkin_seeds", "sugar", "cactus_green", "lapis_lazuli", "beetroot_soup",
                "furnace_minecart", "gunpoweder", "compass", "cookie", "pumpkin_seeds", "spider_eye", "purple_dye", "emerald",
                "chest_minecart", "firework_rocket", "pumpkin_pie", "fermented_spider_eye", "lime_dye", "cyan_dye",
                "tnt_minecart", "map", "glass_bottle", "rail", "mushroom_stew", "cauldron", "cod", "salmon", "tropical_fish",
                "pufferfish", "repeater", "dried_kelp", "golden_carrot"
        );

        Pair<Server, ForgeConfigSpec> spec = new Builder().configure(Server::new);
        serverSpec = spec.getRight();
        SERVER = spec.getLeft();
    }

    private static List<String> create(String... names) {
        return Arrays.stream(names).map(s -> "minecraft:" + s).collect(ImmutableList.toImmutableList());
    }

    public static class Server {

        public ConfigValue<List<? extends String>> whitelist;
        public BooleanValue enchantedBookUnique;

        public Server(Builder builder) {

            builder.comment("Server configs")
                    .push("server");

            whitelist = builder
                    .comment("The items to use in the game")
                    .translation("")
                    .defineList("whitelist", DEFAULT_ITEMS, o -> {
                        if (o instanceof String) {
                            return ResourceLocation.tryCreate((String) o) != null;
                        }
                         return false;
                    });
            enchantedBookUnique = builder
                    .comment("If enchanted books should require a specific enchantment or just any enchantment, true to require uniqueness")
                    .translation("")
                    .define("enchanted_book_unique", false);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ModConfig config = configEvent.getConfig();
        if (config.getType() == ModConfig.Type.SERVER) {
            serverConfig = config;
        }
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.ConfigReloading configEvent) {
        LOGGER.debug("Config just got changed on the file system!");
    }

}

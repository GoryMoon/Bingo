package se.gorymoon.bingo.handlers;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import se.gorymoon.bingo.Bingo;

import javax.annotation.Nonnull;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Bingo.MOD_ID)
public class FireworkHandler {

    private static final Random rand = new Random();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;

        if (!player.world.isRemote && event.phase == TickEvent.Phase.END) {
            int fireworks = player.getEntityData().getInt("Fireworks");
            if (fireworks > 0 && (!player.getEntityData().getBoolean("FireworkDelay") || player.world.getGameTime() % 20 == 0)) {
                BlockPos pos = new BlockPos(player).up(2);
                spawnFirework(pos, player.world.dimension, 12);
                player.getEntityData().putInt("Fireworks", fireworks - 1);

                if (fireworks > 5) {
                    player.getEntityData().putBoolean("FireworkDelay", true);
                } else {
                    player.getEntityData().putBoolean("FireworkDelay", false);
                }
            }
        }
    }

    public static EntityFireworkRocket getRandomFirework(@Nonnull World world, @Nonnull BlockPos pos) {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.setTag(new NBTTagCompound());
        NBTTagCompound expl = new NBTTagCompound();
        expl.putBoolean("Flicker", true);

        int[] colors = new int[rand.nextInt(8) + 1];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = EnumDyeColor.values()[rand.nextInt(16)].getFireworkColor();
        }
        expl.putIntArray("Colors", colors);
        byte type = (byte) (rand.nextInt(3) + 1);
        type = type == 3 ? 4 : type;
        expl.putByte("Type", type);

        NBTTagList explosions = new NBTTagList();
        explosions.add(expl);

        NBTTagCompound fireworkTag = new NBTTagCompound();
        fireworkTag.put("Explosions", explosions);
        fireworkTag.putByte("Flight", (byte) 1);
        firework.setTagInfo("Fireworks", fireworkTag);

        EntityFireworkRocket e = new EntityFireworkRocket(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, firework);
        return e;
    }

    public static void spawnFirework(@Nonnull BlockPos block, Dimension dimension) {
        spawnFirework(block, dimension, 0);
    }

    public static void spawnFirework(@Nonnull BlockPos pos, Dimension dimension, int range) {
        World world = dimension.getWorld();
        BlockPos spawnPos = pos;

        // don't bother if there's no randomness at all
        if (range > 0) {
            spawnPos = new BlockPos(moveRandomly(spawnPos.getX(), range), spawnPos.getY(), moveRandomly(spawnPos.getZ(), range));

            int tries = -1;
            while (!world.isAirBlock(new BlockPos(spawnPos))) {
                tries++;
                if (tries > 100) {
                    return;
                }
            }
        }

        world.spawnEntity(getRandomFirework(world, spawnPos));
    }

    private static double moveRandomly(double base, double range) {
        return base + 0.5 + rand.nextDouble() * range - (range / 2);
    }

}

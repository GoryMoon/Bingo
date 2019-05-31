package se.gorymoon.bingo.game.effects;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class NightVisionEffect extends BaseEffect {

    public NightVisionEffect() {
        super("night_vision");
    }

    @Override
    public String getName() {
        return "Night Vision";
    }

    @Override
    public List<String> getDescription() {
        return ImmutableList.of("Gives all players night vision");
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> start() {
        return player -> player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, Short.MAX_VALUE, 0, false, false));
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> tick() {
        return NOOP;
    }

    @Nonnull
    @Override
    public Consumer<EntityPlayer> revert() {
        return player -> player.removePotionEffect(MobEffects.NIGHT_VISION);
    }
}

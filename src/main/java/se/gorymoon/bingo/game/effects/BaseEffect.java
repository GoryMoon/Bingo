package se.gorymoon.bingo.game.effects;

import net.minecraft.util.ResourceLocation;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.api.IBingoEffect;

public abstract class BaseEffect implements IBingoEffect {

    private ResourceLocation id;

    protected BaseEffect(ResourceLocation id) {
        this.id = id;
    }

    protected BaseEffect(String id) {
        this.id = new ResourceLocation(Bingo.MOD_ID, id);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
}

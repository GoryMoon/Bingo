package se.gorymoon.bingo.client.rendering;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.text.TextFormatting;
import se.gorymoon.bingo.blocks.TrophyTileEntity;
import se.gorymoon.bingo.handlers.GuiHandler;

public class TrophyTESR extends TileEntityRenderer<TrophyTileEntity> {

    @Override
    public void render(TrophyTileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tileEntityIn != null && tileEntityIn.getPos().getDistance(rendererDispatcher.entity.getPosition()) < 50) {
            this.setLightmapDisabled(true);
            this.drawNameplate(tileEntityIn, "Open bingo gui with " + TextFormatting.GOLD + "[" + GuiHandler.showBingo.getLocalizedName() + "]", x, y, z, 15);
            this.setLightmapDisabled(false);
        }
    }
}

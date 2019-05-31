package se.gorymoon.bingo.client.gui.screens;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.TeamActionMessage;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.client.gui.IProgressMeter.LOADING_STRINGS;

public class StartGui extends BaseBingoGui {


    private GuiButton joinButton;
    private boolean loading = false;

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        joinButton = new GuiButtonExt(nextId.get(), getLeft() + (getWidth() / 2) - 100, getTop() + (getHeight() / 2) - 10, "Join Bingo") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), TeamActionMessage.add(mc.player.getGameProfile().getName()));

                loading = true;
                joinButton.visible = false;
            }
        };
        buttonConsumer.accept(joinButton);
    }

    @Override
    public void show() {
        MinecraftForge.EVENT_BUS.register(this);
        joinButton.visible = true;
    }

    @Override
    public void hide() {
        MinecraftForge.EVENT_BUS.unregister(this);
        joinButton.visible = false;
        loading = false;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        if (loading) {
            this.drawCenteredString(this.fontRenderer, LOADING_STRINGS[(int)(Util.milliTime() / 150L % (long)LOADING_STRINGS.length)], getWidth() / 2, getHeight() / 2 - this.fontRenderer.FONT_HEIGHT, 0xFFFFFF);
        }
    }
}

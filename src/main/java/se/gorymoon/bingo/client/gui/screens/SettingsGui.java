package se.gorymoon.bingo.client.gui.screens;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.network.PacketDistributor;
import se.gorymoon.bingo.api.IBingoEffect;
import se.gorymoon.bingo.api.IBingoMode;
import se.gorymoon.bingo.game.board.BoardManager;
import se.gorymoon.bingo.game.effects.BingoEffects;
import se.gorymoon.bingo.game.events.BoardSyncEvent;
import se.gorymoon.bingo.game.modes.BingoModes;
import se.gorymoon.bingo.handlers.GameHandler;
import se.gorymoon.bingo.network.NetworkManager;
import se.gorymoon.bingo.network.messages.BoardActionMessage;
import se.gorymoon.bingo.utils.PlayerCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SettingsGui extends BaseBingoGui {

    private int selectedModeIndex = 0;
    private int selectedEffectIndex = 0;
    private IBingoMode mode;
    private IBingoEffect effect;
    private String seed = "";
    private GuiButtonExt modeButton;
    private GuiButtonExt effectButton;
    private GuiButtonExt saveButton;
    private GuiTextField seedField;

    @Override
    public void show() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void hide() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void initGui(Consumer<GuiButton> buttonConsumer, Consumer<IGuiEventListener> listenerConsumer, Supplier<Integer> nextId) {
        mode = BoardManager.INSTANCE.getMode();
        effect = BoardManager.INSTANCE.getEffect();
        seed = BoardManager.INSTANCE.getSeed();
        selectedModeIndex = BingoModes.INSTANCE.getBingoModes().indexOf(mode);
        selectedEffectIndex = BingoEffects.INSTANCE.getBingoEffects().indexOf(effect);

        modeButton = new GuiButtonExt(nextId.get(), getLeft() + 5, getTop() + 26, 105, 20, "Select Mode") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                selectedModeIndex++;
                List<IBingoMode> modes = BingoModes.INSTANCE.getBingoModes();
                if (selectedModeIndex >= modes.size()) {
                    selectedModeIndex = 0;
                }
                mode = modes.get(selectedModeIndex);
                updateData();
            }
        };

        effectButton = new GuiButtonExt(nextId.get(), getLeft() + 114, getTop() + 26, 105, 20, "Select Effect") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                selectedEffectIndex++;
                List<IBingoEffect> effects = BingoEffects.INSTANCE.getBingoEffects();
                if (selectedEffectIndex >= effects.size()) {
                    selectedEffectIndex = 0;
                }
                effect = effects.get(selectedEffectIndex);
                updateData();
            }
        };
        modeButton.enabled = effectButton.enabled = !BoardManager.INSTANCE.isRunning() && !GameHandler.INSTANCE.isWaiting() && PlayerCache.INSTANCE.isOp(getGui().playerID);

        saveButton = new GuiButtonExt(nextId.get(), getLeft() + 5, getTop() + 70, 100, 15, "Save Settings") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                seed = !seedField.getText().equals(seed) ? seedField.getText(): "";
                NetworkManager.INSTANCE.send(PacketDistributor.SERVER.noArg(), BoardActionMessage.edit(mode.getId(), effect.getId(), seed));
            }
        };

        seedField = new GuiTextField(nextId.get(), fontRenderer(), getLeft() + 5, getTop() + 48, 150, 20);
        seedField.setTextAcceptHandler((integer, s) -> updateData());
        seedField.setText(seed);
        seedField.setEnabled(modeButton.enabled);

        updateData();

        buttonConsumer.accept(modeButton);
        buttonConsumer.accept(effectButton);
        buttonConsumer.accept(saveButton);
        listenerConsumer.accept(seedField);
        listenerConsumer.accept(addTab(getBackTab()));
    }

    private void updateData() {
        effectButton.displayString = effect.getName();
        modeButton.displayString = mode.getName();
        saveButton.enabled = PlayerCache.INSTANCE.isOp(getGui().playerID) && (!seed.equals(seedField.getText()) || mode != BoardManager.INSTANCE.getMode() || effect != BoardManager.INSTANCE.getEffect());
    }

    @Override
    public void tick() {
        if (getGui().getFocused() != this && !seedField.isFocused()) {
            getGui().focusOn(this);
        }
        seedField.tick();
    }

    @Override
    public boolean charTyped(char key, int modifiers) {
        if (seedField.isFocused()) {
            seedField.charTyped(key, modifiers);
            return true;
        }
        return super.charTyped(key, modifiers);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (seedField.isFocused()) {
            seedField.keyPressed(key, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void drawBackground(float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(partialTicks, mouseX, mouseY);

        GlStateManager.disableLighting();
        seedField.drawTextField(mouseX, mouseY, partialTicks);
        GlStateManager.enableLighting();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

        GlStateManager.disableLighting();
        fontRenderer.drawSplitString("Item Whitelist available in config for now", 5, 90, 200, 0xFF000000);
        GlStateManager.enableLighting();

        if (isPointInButton(modeButton, mouseX, mouseY)) {
            ArrayList<String> strings = Lists.newArrayList("Select game mode", "", TextFormatting.GRAY + mode.getName());
            strings.addAll(mode.getDescription().stream().map(s -> TextFormatting.DARK_GRAY + s).collect(Collectors.toList()));
            drawHoveringText(strings, mouseX, mouseY);
        }

        if (isPointInButton(effectButton, mouseX, mouseY)) {
            ArrayList<String> strings = Lists.newArrayList("Select game effect", "", TextFormatting.GRAY + effect.getName());
            strings.addAll(effect.getDescription().stream().map(s -> TextFormatting.DARK_GRAY + s).collect(Collectors.toList()));
            drawHoveringText(strings, mouseX, mouseY);
        }

        if (isPointInRegion(seedField.x, seedField.y, seedField.width, seedField.height, mouseX, mouseY)) {
            drawHoveringText("Seed to use, empty generates random seed", mouseX, mouseY);
        }

        super.drawForeground(mouseX, mouseY);
    }

    @SubscribeEvent
    public void onBoardChange(BoardSyncEvent event) {
        updateData();
    }
}

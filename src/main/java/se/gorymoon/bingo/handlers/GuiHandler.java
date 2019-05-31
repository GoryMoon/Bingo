package se.gorymoon.bingo.handlers;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ForgeI18n;
import net.minecraftforge.fml.common.Mod;
import se.gorymoon.bingo.Bingo;
import se.gorymoon.bingo.client.gui.BingoGui;
import se.gorymoon.bingo.client.gui.PopupGui;
import se.gorymoon.bingo.utils.Util;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Bingo.MOD_ID, value = Dist.CLIENT)
public class GuiHandler {

    private static boolean alreadyGenerated = false;
    public static PopupGui inGamePopup = new PopupGui();
    public static PopupGui inventoryPopup = new PopupGui();
    private static boolean keyPopupDown = false;
    private static boolean keyDown = false;

    static {
        inventoryPopup.setRenderOnScreen(true);
    }

    public static KeyBinding showPopup;
    public static KeyBinding showBingo;

    @SubscribeEvent
    public static void onGuiDraw(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
            if (!(Minecraft.getInstance().currentScreen instanceof BingoGui)) {
                inGamePopup.setRenderTooltip(null);
                inGamePopup.setRenderOnScreen(false);
                inGamePopup.render(0, 0, event.getPartialTicks());
            }
        }
    }

    @SubscribeEvent
    public static void onBackground(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.getGui() instanceof GuiChat) {
            inGamePopup.setRenderTooltip((stack, x, y) -> Util.renderToolTip(stack, x, y, event.getGui()));
            inGamePopup.setRenderOnScreen(true);
            inGamePopup.render(event.getMouseX(), event.getMouseY(), 0);
        } else if (event.getGui() instanceof GuiContainer && !(event.getGui() instanceof BingoGui)) {
            inventoryPopup.setRenderTooltip((stack, x, y) -> Util.renderToolTip(stack, x, y, event.getGui()));
            inventoryPopup.render(event.getMouseX(), event.getMouseY(), 0);
        }
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiWorldSelection) {
            alreadyGenerated = false;
            event.addButton(new GuiButton(42, event.getGui().width / 2 - 152, 22, 50, 20, ForgeI18n.parseMessage("generator.bingo")) {
                @Override
                public void onClick(double mouseX, double mouseY) {
                    startWorld(event.getGui().mc);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().currentScreen == null) {
            if (!keyPopupDown && showPopup.isActiveAndMatches(InputMappings.getInputByCode(event.getKey(), event.getScanCode()))) {
                inGamePopup.toggleVisibility();
                keyPopupDown = true;
            } else {
                keyPopupDown = false;
            }
            if (!keyDown && showBingo.isActiveAndMatches(InputMappings.getInputByCode(event.getKey(), event.getScanCode()))) {
                Minecraft.getInstance().displayGuiScreen(new BingoGui(""));
                keyDown = true;
            } else {
                keyDown = false;
            }
        }
    }

    private static void startWorld(Minecraft mc) {
        mc.displayGuiScreen(null);
        if (!alreadyGenerated) {
            alreadyGenerated = true;
            long i = (new Random()).nextLong();

            //TODO GAMETYPE SURVIVAL when not in dev
            WorldSettings worldsettings = new WorldSettings(i, GameType.SURVIVAL, true, false, Bingo.BINGO_WORLD);
            worldsettings.setGeneratorOptions(Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, new NBTTagCompound()));
            worldsettings.enableCommands();

            mc.launchIntegratedServer(GuiCreateWorld.getUncollidingSaveDirName(mc.getSaveLoader(), "Bingo"), "Bingo", worldsettings);
        }
    }

}

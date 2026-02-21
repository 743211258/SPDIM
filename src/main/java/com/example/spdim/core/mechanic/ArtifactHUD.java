package com.example.spdim.core.mechanic;

import com.example.spdim.core.artifact.ChaliceOfBlood;
import com.example.spdim.core.artifact.TimekeepersHourglass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArtifactHUD {

    private static int cachedEnergy = 0;
    private static final int maxEnergy = 100;

    private static int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 5;

    // Update the energy information for every 0.25 seconds.
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // A counter to keep track of the time.
        tickCounter++;
        if (tickCounter < TICKS_PER_SECOND) {
            return;
        }
        tickCounter = 0;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        // Reinitialize the data.
        cachedEnergy = 0;
        if (player == null) {
            return;
        }

        // Check for both main hand and off hand, search for artifacts.
        for (ItemStack stack : new ItemStack[]{player.getOffhandItem(), player.getMainHandItem()}) {
            if (stack.getItem() instanceof TimekeepersHourglass hourglass) {
                // Graph only if the hourglass is available.
                if (hourglass.isApplicable(stack, player.level())) {
                    cachedEnergy = maxEnergy;
                    break;
                }
            }
            if (stack.getItem() instanceof ChaliceOfBlood chalice) {
                cachedEnergy = maxEnergy;
                break;
            }
        }
    }

    // Graph the energy bar for every tick.
    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        // Render the energy bar only when the server is rendering hotbar.
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        // player might not exists (During world loading)
        if (mc.player == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();

        // Location of the bar.
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 50;
        int y = mc.getWindow().getGuiScaledHeight() - 60;
        int width = 100;
        int height = 4;

        // Graph the bar.
        graphics.fill(x, y, x + width, y + height, 0xAA000000);

        // Graph the energy.
        float ratio = (float) cachedEnergy / maxEnergy;
        int fillWidth = (int) (width * ratio);
        graphics.fill(x, y, x + fillWidth, y + height, 0xFFFFFF00);
    }
}

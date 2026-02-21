package com.example.spdim.core.mechanic;

import com.example.spdim.core.wand.EnergyWand;
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
public class WandHUD {

    private static EnergyWand cachedWand = null;
    private static ItemStack cachedStack = ItemStack.EMPTY;
    private static int cachedEnergy = 0;
    private static int cachedMaxEnergy = 100;

    private static int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 5;

    private static EnergyWand findWand(Player player) {
        // Detect if the player's main hand is holding any wand.
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof EnergyWand wand) return wand;

        // Or detect if the off hand is holding.
        stack = player.getOffhandItem();
        if (stack.getItem() instanceof EnergyWand wand) return wand;

        // The player is not holding any wand.
        return null;
    }

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
        if (player == null) {
            cachedWand = null;
            cachedStack = ItemStack.EMPTY;
            cachedEnergy = 0;
            cachedMaxEnergy = 100;
            return;
        }

        // Check if the player is holding wand.
        EnergyWand wand = findWand(player);
        ItemStack stack = ItemStack.EMPTY;
        if (wand != null) {
            // The wand is either in the main hand or off hand.
            stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof EnergyWand)) {
                stack = player.getOffhandItem();
            }

            // Get cached energy and max energy.
            cachedEnergy = wand.getCurrentEnergy(stack);
            cachedMaxEnergy = wand.getCurrentMaxEnergy(stack);
        } else {
            cachedEnergy = 0;
            cachedMaxEnergy = 100;
        }

        cachedWand = wand;
        cachedStack = stack;
    }

    // Render the energy bar for every tick.
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
        int y = mc.getWindow().getGuiScaledHeight() - 45;
        int width = 100;
        int height = 4;

        // Graph the bar
        graphics.fill(x, y, x + width, y + height, 0xAA000000);

        // Graph the energy
        float ratio = (float) cachedEnergy / cachedMaxEnergy;
        int fillWidth = (int)(width * ratio);
        graphics.fill(x, y, x + fillWidth, y + height, 0xFFFFFF00);
    }
}

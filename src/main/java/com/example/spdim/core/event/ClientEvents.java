package com.example.spdim.core.event;

import com.example.spdim.core.Artifact;
import com.example.spdim.core.data_structure.IntVec2;
import com.example.spdim.core.mechanic.Rooted;
import com.example.spdim.core.mechanic.TickFreeze;
import com.example.spdim.core.wand.EnergyWand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "spdim", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    private static final Map<IntVec2, String> wandInventoryRenderCache = new HashMap<>();
    private static final Map<IntVec2, String> wandHotBarRenderCache = new HashMap<>();
    private static final Set<IntVec2> artifactInventoryRenderCache = new HashSet<>();
    private static final Set<IntVec2> artifactHotBarRenderCache = new HashSet<>();

    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;

    private static void updateInventoryRenderCache(AbstractContainerScreen<?> screen, Player player) {
        wandInventoryRenderCache.clear();
        artifactInventoryRenderCache.clear();
        for (Slot slot : screen.getMenu().slots) {
            if (!(slot.container == player.getInventory())) {
                continue;
            }
            ItemStack stack = slot.getItem();

            int x = screen.getGuiLeft() + slot.x;
            int y = screen.getGuiTop() + slot.y;

            IntVec2 pos = new IntVec2(x, y);
            if (stack.getItem() instanceof EnergyWand) {
                pos.setX(x + 6);
                pos.setY(y + 11);

                CompoundTag tag = stack.getTag();
                if (tag == null || tag.isEmpty()) {
                    continue;
                }

                int currentEnergy = tag.getInt("CurrentEnergy");
                int maxEnergy = tag.getInt("CurrentMaxEnergy");
                String text = currentEnergy + "/" + maxEnergy;
                wandInventoryRenderCache.put(pos, text);
            } else if (stack.getItem() instanceof Artifact artifact) {
                if (artifact.isApplicable(stack, player.level())) {
                    artifactInventoryRenderCache.add(pos);
                }
            }
        }
    }

    private static void updateHotBarRenderCache(Player player) {

        wandHotBarRenderCache.clear();
        artifactHotBarRenderCache.clear();

        Minecraft mc = Minecraft.getInstance();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        for (int i = 0; i < 10; i++) {
            ItemStack stack;
            int x;
            if (i < 9) {
                stack = player.getInventory().getItem(i);
                x = (screenWidth / 2 - 90) + i * 20 + 2;
            } else {
                stack = player.getOffhandItem();
                x = (screenWidth / 2 - 90) - 27;
            }
            int y = screenHeight - 21 + 2;
            IntVec2 pos = new IntVec2(x, y);

            if (stack.getItem() instanceof EnergyWand) {
                pos.setX(x + 6);
                pos.setY(y + 11);

                CompoundTag tag = stack.getTag();
                if (tag == null || tag.isEmpty()) {
                    continue;
                }

                int currentEnergy = tag.getInt("CurrentEnergy");
                int maxEnergy = tag.getInt("CurrentMaxEnergy");
                String text = currentEnergy + "/" + maxEnergy;
                wandHotBarRenderCache.put(pos, text);
            } else if (stack.getItem() instanceof Artifact artifact) {
                if (artifact.isApplicable(stack, player.level())) {
                    artifactHotBarRenderCache.add(pos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }
        updateHotBarRenderCache(player);

        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            updateInventoryRenderCache(screen, player);
        } else {
            wandInventoryRenderCache.clear();
            artifactInventoryRenderCache.clear();
        }
    }

        @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        if (TickFreeze.isFrozen(player) || Rooted.isRooted(player)) {
            var input = event.getInput();

            // Disable all movement keys.
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        // Disable interaction keys.
        if (player != null && TickFreeze.isFrozen(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderInventory(ScreenEvent.Render.Post event) {
        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        for (Map.Entry<IntVec2, String> entry : wandInventoryRenderCache.entrySet()) {
            gui.pose().pushPose();
            gui.pose().scale(0.5f, 0.5f, 1);
            gui.drawString(font, entry.getValue(), entry.getKey().getX() / 0.5f, entry.getKey().getY() / 0.5f, 0xFFFFFF, true);
            gui.pose().popPose();
        }
        for (IntVec2 entry : artifactInventoryRenderCache) {
            int x = entry.getX();
            int y = entry.getY();
            gui.fill(x, y, x + WIDTH, y + HEIGHT, 0x80FF0000);
        }
    }

    @SubscribeEvent
    public static void onRenderHotbar(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        GuiGraphics gui = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        Font font = mc.font;

        for (Map.Entry<IntVec2, String> entry : wandHotBarRenderCache.entrySet()) {
            gui.pose().pushPose();
            gui.pose().scale(0.5f, 0.5f, 1);
            gui.drawString(font, entry.getValue(), entry.getKey().getX() / 0.5f, entry.getKey().getY() / 0.5f, 0xFFFFFF, true);
            gui.pose().popPose();
        }
        for (IntVec2 entry : artifactHotBarRenderCache) {
            int x = entry.getX();
            int y = entry.getY();
            gui.fill(x, y, x + WIDTH, y + HEIGHT, 0x80FF0000);
        }
    }
}

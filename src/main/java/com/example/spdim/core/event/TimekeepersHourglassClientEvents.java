package com.example.spdim.core.event;

import com.example.spdim.core.artifact.TimekeepersHourglass;
import com.example.spdim.core.mechanic.TickFreeze;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = "spdim", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class TimekeepersHourglassClientEvents {

    // Bind button z with freeze others ability.
    public static KeyMapping keyFreezeOthers = new KeyMapping(
            "key.mymod.freeze_others",
            GLFW.GLFW_KEY_Z,
            "key.categories.gameplay"
    );

    // Bind button x with freeze myself ability.
    public static KeyMapping keyFreezeSelf = new KeyMapping(
            "key.mymod.freeze_self",
            GLFW.GLFW_KEY_X,
            "key.categories.gameplay"
    );


    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Activate only when the keys are pressed.
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        // The player might not exists (During world loading)
        Player player = mc.player;
        if (player == null) {
            return;
        }

        if (keyFreezeOthers.consumeClick()) {
            ItemStack stack = player.getOffhandItem();
            // Call the function only when the player's offhand is holding timekeeper's hourglass and the player is not frozen.
            if (stack.getItem() instanceof TimekeepersHourglass item && !TickFreeze.isFrozen(player)) {
                item.FreezeOthersClient(player.level(), player, stack);
            }
        }

        if (keyFreezeSelf.consumeClick()) {
            ItemStack stack = player.getOffhandItem();
            // Call the function only when the player's offhand is holding timekeeper's hourglass and the player is not frozen.
            if (stack.getItem() instanceof TimekeepersHourglass item && !TickFreeze.isFrozen(player)) {
                item.FreezeMyselfClient(player.level(), player, stack);
            }
        }
    }
}

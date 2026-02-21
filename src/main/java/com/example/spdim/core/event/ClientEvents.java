package com.example.spdim.core.event;

import com.example.spdim.core.mechanic.Rooted;
import com.example.spdim.core.mechanic.TickFreeze;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "spdim", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
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
}

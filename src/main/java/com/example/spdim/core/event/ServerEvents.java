package com.example.spdim.core.event;

import com.example.spdim.core.mechanic.RegenerationDisabled;
import com.example.spdim.core.mechanic.Rooted;
import com.example.spdim.core.mechanic.TickFreeze;
import com.example.spdim.core.mechanic.Untargetable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "spdim", bus = Mod.EventBusSubscriber.Bus.FORGE)

public class ServerEvents {
    // Run all tick function for every tick.
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        TickFreeze.tick();
        Untargetable.tick();
        RegenerationDisabled.tick();
        Rooted.tick();
    }

    // Lock the speed and location of rooted player.
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (!Rooted.isRooted(player)) {
            return;
        }

        Vec3 lp = Rooted.LOCKED.get(player);
        if (lp == null) {
            return;
        }

        player.setPos(lp.x, lp.y, lp.z);

        player.setDeltaMovement(Vec3.ZERO);
    }
}

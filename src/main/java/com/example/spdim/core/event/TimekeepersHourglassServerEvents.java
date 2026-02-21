package com.example.spdim.core.event;

import com.example.spdim.core.mechanic.Untargetable;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = "spdim", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TimekeepersHourglassServerEvents {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        // Diable right click if the target is untargetable.
        if (event.getTarget() instanceof LivingEntity target && Untargetable.isUntargetable(target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // Diable attack if the target is untargetable.
        if (event.getTarget() instanceof LivingEntity target && Untargetable.isUntargetable(target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMobAttack(TickEvent.PlayerTickEvent event) {
        // Happens only at the end phase.
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        // Get the player.
        Player player = event.player;
        // Nothing need to be done if the player is not untargetable.
        if (!(player.level() instanceof ServerLevel) || !Untargetable.isUntargetable(player)) {
            return;
        }
        //
        double radius = 32.0;
        AABB area = player.getBoundingBox().inflate(radius);
        List<Mob> mobsNearby = player.level().getEntitiesOfClass(Mob.class, area);

        // Remove mobs' attack intentions if they are within the radius
        // No need to check for inscribed sphere since most mobs has an attacking radius less than 32
        for (Mob mob : mobsNearby) {
            if (mob.getTarget() instanceof Player target && Untargetable.isUntargetable(target)) {
                mob.setTarget(null);
            }
        }
    }
    @SubscribeEvent
    public static void onMobChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();

        // Prevent mobs from choosing untargetable players.
        if (newTarget instanceof Player player && Untargetable.isUntargetable(player)) {
            event.setNewTarget(null);
        }
    }
}

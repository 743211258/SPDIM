package com.example.spdim.core.mechanic;

import com.example.spdim.core.data_structure.PosAndDirection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TickFreeze {
    private static final Map<LivingEntity, Integer> active = new HashMap<>();
    public static final Map<LivingEntity, PosAndDirection> LOCKED = new HashMap<>();


    public static void freeze(LivingEntity target, int ticks) {
        if (target == null || ticks < 0) {
            return;
        }
        // Increment the effect time if the player has been frozen for many times.
        if (active.containsKey(target)) {
            int oldTick =  active.get(target);
            active.put(target, oldTick + ticks);
        } else {
            active.put(target, ticks);
        }
    }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, Integer>> iterator = active.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = iterator.next(); // 只调用一次
            LivingEntity entity = entry.getKey();
            int ticks = entry.getValue();
            // Remove the entity from the hashmap if the effect doesn't apply anymore.
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                iterator.remove();
                LOCKED.remove(entity);
                continue;
            }
            if (ticks <= 0) {
                unfreeze(entity);
                iterator.remove();
                LOCKED.remove(entity);
                continue;
            }
            // Lock the position, speed, and direction for every tick.
            if (entity instanceof Mob mob) {

                // Add the mob to all hashmap if it isn't in them.
                if (!LOCKED.containsKey(mob)) {
                    PosAndDirection lp = new PosAndDirection();
                    lp.pos = mob.position();
                    lp.yRot = mob.getYRot();
                    lp.xRot = mob.getXRot();
                    lp.yHeadRot = mob.yHeadRot;
                    lp.yBodyRot = mob.yBodyRot;
                    LOCKED.put(mob, lp);
                }

                PosAndDirection lp = LOCKED.get(mob);

                // Lock the position.
                mob.setPos(lp.pos.x, lp.pos.y, lp.pos.z);

                // Turn off AI
                mob.setNoAi(true);

                // 2. Lock the speed.
                mob.setDeltaMovement(Vec3.ZERO);

                // Update them to the client side.
                mob.hurtMarked = true;

                // Stop auto navigation.
                mob.getNavigation().stop();

                // Lock the direction.
                mob.setYRot(lp.yRot);
                mob.setXRot(lp.xRot);
                mob.yHeadRot = lp.yHeadRot;
                mob.yBodyRot = lp.yBodyRot;
            } else if (entity instanceof Player player) {

                // Add the player to all hashmap if it isn't in them.
                if (!LOCKED.containsKey(player)) {
                    PosAndDirection lp = new PosAndDirection();
                    lp.pos = player.position();
                    lp.yRot = player.getYRot();
                    lp.xRot = player.getXRot();
                    lp.yHeadRot = player.yHeadRot;
                    lp.yBodyRot = player.yBodyRot;
                    LOCKED.put(player, lp);
                }
                PosAndDirection lp = LOCKED.get(player);

                // Lock the position.
                player.setPos(lp.pos.x, lp.pos.y, lp.pos.z);

                // 2. Lock the speed.
                player.setDeltaMovement(Vec3.ZERO);

                // Lock the direction
                player.setYRot(lp.yRot);
                player.setXRot(lp.xRot);
                player.yHeadRot = lp.yHeadRot;
                player.yBodyRot = lp.yBodyRot;

                // Send the direction and location package to all player.
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.teleport(
                            lp.pos.x, lp.pos.y, lp.pos.z,
                            lp.yRot, lp.xRot
                    );
                }
            }
            entry.setValue(ticks - 1);
        }
    }

    private static void unfreeze(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // Turn on the AI
            mob.setNoAi(false);
            // Activate the path navigation of the mob
            mob.getNavigation().recomputePath();

            mob.setDeltaMovement(mob.getDeltaMovement());

            // 5end the information to the client.
            mob.hurtMarked = true;
        }
    }

    public static boolean isFrozen(LivingEntity entity) {
        return active.containsKey(entity);
    }

    public static boolean isLocked(LivingEntity entity) {
        return LOCKED.containsKey(entity);
    }
}

package com.example.spdim.core.mechanic;

import com.example.spdim.core.wand.energyWand.WandOfRegrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class Rooted {
    // Hashmap to keep track of players that are rooted.
    private static Map<LivingEntity, Integer> active = new HashMap<>();
    // Hashmap to keep track of the location of them.
    public static final Map<LivingEntity, Vec3> LOCKED = new HashMap<>();
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void root(LivingEntity target, int ticks) {
        if (target == null || ticks < 0) {
            return;
        }
        active.put(target, ticks);
    }

    // Lock the location for every tick.
    public static void tick() {
        Iterator<Map.Entry<LivingEntity, Integer>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = iterator.next();
            LivingEntity entity = entry.getKey();
            int ticks = entry.getValue();
            // Remove the entity from the hashmap if the effect doesn't apply anymore.
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                delete(entity);
                iterator.remove();
                LOCKED.remove(entity);
                continue;
            }
            if (ticks <= 0) {
                delete(entity);
                unfreeze(entity);
                iterator.remove();
                LOCKED.remove(entity);
                continue;
            }
            // Lock their location.
            if (entity instanceof Mob mob) {
                if (!LOCKED.containsKey(mob)) {
                    LOCKED.put(mob, mob.position());
                }
                Vec3 lp = LOCKED.get(mob);
                mob.setPos(lp.x, lp.y, lp.z);
                // Set the speed to zero.
                mob.setDeltaMovement(Vec3.ZERO);
                // Update the position and speed to the client side.
                mob.hurtMarked = true;
            } else if (entity instanceof Player player) {
                if (!LOCKED.containsKey(player)) {
                    LOGGER.info("[DEBUG]: Player location is updated!!");
                    LOCKED.put(player, player.position());
                }
                Vec3 lp = LOCKED.get(player);
                player.setPos(lp.x, lp.y, lp.z);
                player.setDeltaMovement(Vec3.ZERO);
                player.hurtMarked = true;
            }
            entry.setValue(ticks - 1);
        }
    }

    private static void unfreeze(LivingEntity entity) {
        if (entity instanceof Mob mob) {

            mob.setDeltaMovement(mob.getDeltaMovement());

            mob.hurtMarked = true;
        }
    }

    private static void delete(LivingEntity entity) {
        // Remove all blocks that surround the player.
        if (WandOfRegrowth.BLOCKS.containsKey(entity)) {
            for (BlockPos pos : WandOfRegrowth.BLOCKS.get(entity)) {
                entity.level().destroyBlock(pos, false);
            }
        }
    }
    public static boolean isRooted(LivingEntity entity) {
        return active.containsKey(entity);
    }
}

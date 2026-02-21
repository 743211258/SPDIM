package com.example.spdim.core.mechanic;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class RegenerationDisabled {
    // Hashmap to store all player with the effect active.
    private static Map<LivingEntity, Integer> active = new HashMap<>();
    // Put the target to the hashmap.
    public static void disable(LivingEntity target, int ticks) {
        if (target == null || ticks < 0) {
            return;
        }
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
            Map.Entry<LivingEntity, Integer> entry = iterator.next();
            LivingEntity entity = entry.getKey();
            int ticks = entry.getValue();
            // Conditions to check for removal of hashmap.
            if (entity == null || !entity.isAlive() || entity.isRemoved() || ticks <= 0) {
                iterator.remove();
                continue;
            }
            // Decrease the tick time one for each tick.
            entry.setValue(ticks - 1);
        }
    }

    public static boolean isDisabled(LivingEntity entity) {
        return active.containsKey(entity);
    }

}

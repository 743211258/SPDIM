package com.example.spdim.core.mechanic;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Untargetable {
    private static Map<LivingEntity, Integer> active = new HashMap<>();

    public static void activate(LivingEntity target, int ticks) {
        if (target == null || ticks < 0) {
            return;
        }
        active.put(target, ticks);
    }

    public static void tick() {
        Iterator<Map.Entry<LivingEntity, Integer>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = iterator.next(); // 只调用一次
            LivingEntity entity = entry.getKey();
            int ticks = entry.getValue();
            if (entity == null || !entity.isAlive() || entity.isRemoved() || ticks <= 0) {
                iterator.remove();
                continue;
            }
            entry.setValue(ticks - 1);
        }
    }

    public static boolean isUntargetable(LivingEntity entity) {
        return active.containsKey(entity);
    }
}

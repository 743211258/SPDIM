package com.example.spdim.core.event;

import com.example.spdim.core.mechanic.Rooted;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "spdim")

public class WandOfRegrowthServerEvents {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (Rooted.isRooted(entity)) {
            event.setCanceled(true);
        }
    }
}

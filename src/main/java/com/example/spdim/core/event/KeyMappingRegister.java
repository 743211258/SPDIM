package com.example.spdim.core.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "spdim", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyMappingRegister {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TimekeepersHourglassClientEvents.keyFreezeOthers);
        event.register(TimekeepersHourglassClientEvents.keyFreezeSelf);
    }
}
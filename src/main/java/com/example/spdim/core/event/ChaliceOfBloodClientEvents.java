package com.example.spdim.core.event;

import com.example.spdim.core.artifact.ChaliceOfBlood;
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

public class ChaliceOfBloodClientEvents {
    public static KeyMapping keyOnUse = new KeyMapping(
            "key.mymod.ChaliceOfBloodOnUse",
            GLFW.GLFW_KEY_Z,
            "key.categories.gameplay"
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (keyOnUse.consumeClick()) {
            ItemStack stack = player.getOffhandItem();
            if (stack.getItem() instanceof ChaliceOfBlood item) {
                item.onUseClientSide(player.level(), player, stack);
            }
        }
    }
}

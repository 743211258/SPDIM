package com.example.spdim.core.network;

import com.example.spdim.core.artifact.TimekeepersHourglass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FreezeSelfPacket {

    public FreezeSelfPacket() {
        // Does not contain any additional information.
    }

    public static void encode(FreezeSelfPacket msg, FriendlyByteBuf buf) {
        // Empty since there is no data.
    }

    public static FreezeSelfPacket decode(FriendlyByteBuf buf) {
        return new FreezeSelfPacket();
    }

    public static void handle(FreezeSelfPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            // Check if the player's offhand is holding timekeeper's hourglass
            ItemStack stack = player.getOffhandItem();
            if (!stack.isEmpty() && stack.getItem() instanceof TimekeepersHourglass item) {
                item.FreezeMyselfServerSide(player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}

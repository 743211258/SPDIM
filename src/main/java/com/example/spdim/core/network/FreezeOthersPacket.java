package com.example.spdim.core.network;

import com.example.spdim.core.artifact.TimekeepersHourglass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class FreezeOthersPacket {

    public FreezeOthersPacket() {
        // Does not contain any additional information.
    }

    public static void encode(FreezeOthersPacket msg, FriendlyByteBuf buf) {
        // Empty since there is no data.
    }

    public static FreezeOthersPacket decode(FriendlyByteBuf buf) {
        return new FreezeOthersPacket();
    }

    public static void handle(FreezeOthersPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                System.out.println("[DEBUG] FreezeOthersPacket received but player is null!");
                return;
            }
            System.out.println("[DEBUG] FreezeOthersPacket received from player: " + player.getName().getString());

            var stack = player.getOffhandItem();
            // Check if the player's offhand is holding timekeeper's hourglass
            if (!stack.isEmpty() && stack.getItem() instanceof TimekeepersHourglass item) {
                item.FreezeOthersServerSide(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
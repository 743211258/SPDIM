package com.example.spdim.core.network;

import com.example.spdim.core.artifact.ChaliceOfBlood;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChaliceOfBloodOnUsePacket {

    public ChaliceOfBloodOnUsePacket() {
        // Does not contain any additional information.
    }

    public static void encode(ChaliceOfBloodOnUsePacket msg, FriendlyByteBuf buf) {
        // Empty since there is no data.
    }

    public static ChaliceOfBloodOnUsePacket decode(FriendlyByteBuf buf) {
        return new ChaliceOfBloodOnUsePacket();
    }

    public static void handle(ChaliceOfBloodOnUsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            // Check if the player's offhand is holding chalice of blood.
            ItemStack offHand = player.getOffhandItem();

            if (!offHand.isEmpty() && offHand.getItem() instanceof ChaliceOfBlood chalice) {
                chalice.onUseServerSide(player);
            }
            offHand.shrink(1);

        });

        ctx.get().setPacketHandled(true);
    }
}

package com.example.spdim.core.network;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class MyModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    // Register all customized network packets.
    public static void register() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath("spdim", "main"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        int id = 0;

        CHANNEL.registerMessage(id++, FreezeOthersPacket.class,
                FreezeOthersPacket::encode,
                FreezeOthersPacket::decode,
                FreezeOthersPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(id++, FreezeSelfPacket.class,
                FreezeSelfPacket::encode,
                FreezeSelfPacket::decode,
                FreezeSelfPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(id++, ChaliceOfBloodOnUsePacket.class,
                ChaliceOfBloodOnUsePacket::encode,
                ChaliceOfBloodOnUsePacket::decode,
                ChaliceOfBloodOnUsePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}


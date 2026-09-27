package org.kill.wpr.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.kill.wpr.Wpr;

public record SpawnUndyingPayload() implements CustomPacketPayload {
    public static final SpawnUndyingPayload INSTANCE = new SpawnUndyingPayload();
    public static final Type<SpawnUndyingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Wpr.MODID, "spawn_undying"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnUndyingPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<SpawnUndyingPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(TYPE, STREAM_CODEC, SpawnUndyingHandler::handle);
    }

    public static void sendToServer() {
        PacketDistributor.sendToServer(INSTANCE);
    }
}

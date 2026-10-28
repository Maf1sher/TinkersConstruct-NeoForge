package slimeknights.tconstruct.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;

import java.util.function.BiConsumer;
import java.util.function.Function;

/** Common helpers for Tinkers' Construct custom payloads. */
public interface TinkerPayload extends ISimplePacket {
  static <T extends TinkerPayload> Type<T> type(String path) {
    return new Type<>(TConstruct.getResource(path));
  }

  static <T extends TinkerPayload> StreamCodec<RegistryFriendlyByteBuf,T> codec(
    Function<RegistryFriendlyByteBuf,T> decoder, BiConsumer<RegistryFriendlyByteBuf,T> encoder) {
    return StreamCodec.of(encoder::accept, decoder::apply);
  }

  /** Schedules packet work on the game thread, matching the former thread-safe packet contract. */
  default void enqueue(IPayloadContext context, Runnable work) {
    context.enqueueWork(work);
  }
}

package slimeknights.tconstruct.library.modifiers.fluid;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerPayload;
import java.util.ArrayList;
import java.util.List;

/** Packet to sync fluid predicates to the client */
@Internal
public record UpdateFluidEffectsPacket(List<FluidEffects.Entry> fluids) implements TinkerPayload {
  public static final Type<UpdateFluidEffectsPacket> TYPE = TinkerPayload.type("update_fluid_effects_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateFluidEffectsPacket> CODEC = TinkerPayload.codec(UpdateFluidEffectsPacket::decode, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateFluidEffectsPacket> type() { return TYPE; }

  /** Clientside constructor, reading from the buffer */
  public static UpdateFluidEffectsPacket decode(RegistryFriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    List<FluidEffects.Entry> entries = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ResourceLocation key = buffer.readResourceLocation();
      try {
        FluidEffects effects = FluidEffects.LOADABLE.decode(buffer, ContextKey.addLookupContext(FluidEffectManager.contextBuilder(key), buffer.registryAccess()).build());
        entries.add(new FluidEffects.Entry(key, effects));
      } catch (RuntimeException e) {
        // put exception in the log with a bit more info
        TConstruct.LOG.error("Failed to decode fluid effects with ID {}", key, e);
        throw e;
      }
    }
    return new UpdateFluidEffectsPacket(List.copyOf(entries));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(fluids.size());
    for (FluidEffects.Entry entry : fluids) {
      ResourceLocation key = entry.name();
      buffer.writeResourceLocation(key);
      try {
        FluidEffects.LOADABLE.encode(buffer, entry.effects());
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Failed to encode fluid effects with ID {}", key, e);
        throw e;
      }
    }
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      FluidEffectManager.INSTANCE.updateFromServer(fluids);
    });
  }
}

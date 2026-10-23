package slimeknights.tconstruct.library.tools.layout;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;

import java.util.Collection;

/**
 * Packet to update the slot layouts for the tinker station
 */
@RequiredArgsConstructor
public class UpdateTinkerSlotLayoutsPacket implements TinkerPayload {
  public static final Type<UpdateTinkerSlotLayoutsPacket> TYPE = TinkerPayload.type("update_tinker_slot_layouts_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateTinkerSlotLayoutsPacket> CODEC = TinkerPayload.codec(UpdateTinkerSlotLayoutsPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateTinkerSlotLayoutsPacket> type() { return TYPE; }

  @Getter(AccessLevel.PACKAGE) @VisibleForTesting
  private final Collection<StationSlotLayout> layouts;

  public UpdateTinkerSlotLayoutsPacket(RegistryFriendlyByteBuf buffer) {
    ImmutableList.Builder<StationSlotLayout> builder = ImmutableList.builder();
    int max = buffer.readVarInt();
    for (int i = 0; i < max; i++) {
      builder.add(StationSlotLayout.read(buffer));
    }
    layouts = builder.build();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(layouts.size());
    for (StationSlotLayout layout : layouts) {
      layout.write(buffer);
    }
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      StationSlotLayoutLoader.getInstance().setSlots(layouts);
    });
  }
}

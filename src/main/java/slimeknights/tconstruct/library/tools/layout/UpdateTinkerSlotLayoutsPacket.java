package slimeknights.tconstruct.library.tools.layout;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;

import java.util.Collection;

/**
 * Packet to update the slot layouts for the tinker station
 */
@RequiredArgsConstructor
public class UpdateTinkerSlotLayoutsPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateTinkerSlotLayoutsPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_tinker_slot_layouts"));
  public static final StreamCodec<FriendlyByteBuf, UpdateTinkerSlotLayoutsPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateTinkerSlotLayoutsPacket::encode, UpdateTinkerSlotLayoutsPacket::new);

  @Getter(AccessLevel.PACKAGE) @VisibleForTesting
  private final Collection<StationSlotLayout> layouts;

  public UpdateTinkerSlotLayoutsPacket(FriendlyByteBuf buffer) {
    ImmutableList.Builder<StationSlotLayout> builder = ImmutableList.builder();
    int max = buffer.readVarInt();
    for (int i = 0; i < max; i++) {
      builder.add(StationSlotLayout.read(buffer));
    }
    layouts = builder.build();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(layouts.size());
    for (StationSlotLayout layout : layouts) {
      layout.write(buffer);
    }
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateTinkerSlotLayoutsPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> StationSlotLayoutLoader.getInstance().setSlots(payload.layouts));
  }
}

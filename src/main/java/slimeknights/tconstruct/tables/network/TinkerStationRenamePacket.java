package slimeknights.tconstruct.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@RequiredArgsConstructor
public class TinkerStationRenamePacket implements TinkerPayload {
  public static final Type<TinkerStationRenamePacket> TYPE = TinkerPayload.type("tinker_station_rename_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,TinkerStationRenamePacket> CODEC = TinkerPayload.codec(TinkerStationRenamePacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<TinkerStationRenamePacket> type() { return TYPE; }

  private final String name;

  public TinkerStationRenamePacket(RegistryFriendlyByteBuf buf) {
    this.name = buf.readUtf(Short.MAX_VALUE);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buf) {
    buf.writeUtf(name);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      if (context.player() instanceof ServerPlayer sender && sender.containerMenu instanceof TinkerStationContainerMenu station) {
        TinkerStationBlockEntity tile = station.getTile();
        if (tile != null) {
          station.getTile().setItemName(name);
        }
      }
    });
  }
}

package slimeknights.tconstruct.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@RequiredArgsConstructor
public class TinkerStationRenamePacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<TinkerStationRenamePacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("tinker_station_rename"));
  public static final StreamCodec<FriendlyByteBuf, TinkerStationRenamePacket> STREAM_CODEC = StreamCodec.ofMember(TinkerStationRenamePacket::encode, TinkerStationRenamePacket::new);

  private final String name;

  public TinkerStationRenamePacket(FriendlyByteBuf buf) {
    this.name = buf.readUtf(Short.MAX_VALUE);
  }

  public void encode(FriendlyByteBuf buf) {
    buf.writeUtf(name);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(TinkerStationRenamePacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      ServerPlayer sender = (ServerPlayer) context.player();
      if (sender.containerMenu instanceof TinkerStationContainerMenu station) {
        TinkerStationBlockEntity tile = station.getTile();
        if (tile != null) {
          station.getTile().setItemName(payload.name);
        }
      }
    });
  }
}

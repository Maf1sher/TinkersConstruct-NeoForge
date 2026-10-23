package slimeknights.tconstruct.tables.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

@RequiredArgsConstructor
public class TinkerStationSelectionPacket implements TinkerPayload {
  public static final Type<TinkerStationSelectionPacket> TYPE = TinkerPayload.type("tinker_station_selection_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,TinkerStationSelectionPacket> CODEC = TinkerPayload.codec(TinkerStationSelectionPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<TinkerStationSelectionPacket> type() { return TYPE; }

  private final ResourceLocation layoutName;
  public TinkerStationSelectionPacket(RegistryFriendlyByteBuf buffer) {
    this.layoutName = buffer.readResourceLocation();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.layoutName);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      if (context.player() instanceof ServerPlayer sender) {
        AbstractContainerMenu container = sender.containerMenu;
        if (container instanceof TinkerStationContainerMenu tinker) {
          tinker.setToolSelection(StationSlotLayoutLoader.getInstance().get(layoutName));
        }
      }
    });
  }
}

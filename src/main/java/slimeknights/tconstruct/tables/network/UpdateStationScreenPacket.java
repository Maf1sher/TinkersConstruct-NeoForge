package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

public class UpdateStationScreenPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateStationScreenPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_station_screen"));
  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();
  public static final StreamCodec<FriendlyByteBuf, UpdateStationScreenPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateStationScreenPacket::encode, buf -> INSTANCE);

  private UpdateStationScreenPacket() {}

  public void encode(FriendlyByteBuf packetBuffer) {}

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateStationScreenPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle());
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null) {
        if (screen instanceof BaseTabbedScreen) {
          ((BaseTabbedScreen<?,?>) screen).updateDisplay();
        }
      }
    }
  }
}

package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

public class UpdateStationScreenPacket implements TinkerPayload {
  public static final Type<UpdateStationScreenPacket> TYPE = TinkerPayload.type("update_station_screen_packet");
  @Override public Type<UpdateStationScreenPacket> type() { return TYPE; }

  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateStationScreenPacket> CODEC = StreamCodec.unit(INSTANCE);

  private UpdateStationScreenPacket() {}

  @Override
  public void encode(RegistryFriendlyByteBuf packetBuffer) {}

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      HandleClient.handle();
    });
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

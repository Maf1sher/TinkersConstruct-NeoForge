package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;

/** Packet to sync player persistent data to the client */
@RequiredArgsConstructor
public class SyncPersistentDataPacket implements TinkerPayload {
  public static final Type<SyncPersistentDataPacket> TYPE = TinkerPayload.type("sync_persistent_data_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,SyncPersistentDataPacket> CODEC = TinkerPayload.codec(SyncPersistentDataPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<SyncPersistentDataPacket> type() { return TYPE; }

  private final CompoundTag data;

  public SyncPersistentDataPacket(RegistryFriendlyByteBuf buffer) {
    data = buffer.readNbt();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeNbt(data);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      HandleClient.handle(this);
    });
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        player.getData(PersistentDataCapability.DATA).copyFrom(packet.data);
      }
    }
  }
}

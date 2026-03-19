package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;

/** Packet to sync player persistent data to the client */
@RequiredArgsConstructor
public class SyncPersistentDataPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<SyncPersistentDataPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("sync_persistent_data"));
  public static final StreamCodec<FriendlyByteBuf, SyncPersistentDataPacket> STREAM_CODEC = StreamCodec.ofMember(SyncPersistentDataPacket::encode, SyncPersistentDataPacket::new);

  private final CompoundTag data;

  public SyncPersistentDataPacket(FriendlyByteBuf buffer) {
    data = buffer.readNbt();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeNbt(data);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(SyncPersistentDataPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(payload));
  }

  /** Handles client side only code safely */
  private static class HandleClient {
    private static void handle(SyncPersistentDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        PersistentDataCapability.getOrWarn(player).copyFrom(packet.data);
      }
    }
  }
}

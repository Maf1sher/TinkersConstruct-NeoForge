package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.GameData;
import slimeknights.tconstruct.TConstruct;

/**
 * Packet to notify neighbors that a block changed, used when breaking blocks in weird contexts that vanilla suppresses updates in for some reason
 */
@RequiredArgsConstructor
public class UpdateNeighborsPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateNeighborsPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_neighbors"));
  public static final StreamCodec<FriendlyByteBuf, UpdateNeighborsPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateNeighborsPacket::encode, UpdateNeighborsPacket::new);

  private final BlockState state;
  private final BlockPos pos;

  public UpdateNeighborsPacket(FriendlyByteBuf buffer) {
    this.state = GameData.getBlockStateIDMap().byId(buffer.readVarInt());
    this.pos = buffer.readBlockPos();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(Block.getId(state));
    buffer.writeBlockPos(pos);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateNeighborsPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(payload));
  }

  private static class HandleClient {
    private static void handle(UpdateNeighborsPacket packet) {
      Level level = Minecraft.getInstance().level;
      if (level != null) {
        packet.state.updateNeighbourShapes(level, packet.pos, Block.UPDATE_CLIENTS, 511);
        packet.state.updateIndirectNeighbourShapes(level, packet.pos, Block.UPDATE_CLIENTS, 511);
      }
    }
  }
}

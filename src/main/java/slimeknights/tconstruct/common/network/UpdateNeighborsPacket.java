package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to notify neighbors that a block changed, used when breaking blocks in weird contexts that vanilla suppresses updates in for some reason
 */
@RequiredArgsConstructor
public class UpdateNeighborsPacket implements TinkerPayload {
  public static final Type<UpdateNeighborsPacket> TYPE = TinkerPayload.type("update_neighbors_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateNeighborsPacket> CODEC = TinkerPayload.codec(UpdateNeighborsPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateNeighborsPacket> type() { return TYPE; }

  private final BlockState state;
  private final BlockPos pos;

  public UpdateNeighborsPacket(RegistryFriendlyByteBuf buffer) {
    this.state = Block.stateById(buffer.readVarInt());
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(Block.getId(state));
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      HandleClient.handle(this);
    });
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

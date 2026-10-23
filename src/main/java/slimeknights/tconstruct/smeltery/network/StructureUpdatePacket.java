package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerBlockEntityPayload;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent when the smeltery or foundry structure changes
 */
@AllArgsConstructor
public class StructureUpdatePacket implements TinkerBlockEntityPayload<HeatingStructureBlockEntity> {
  public static final Type<StructureUpdatePacket> TYPE = TinkerPayload.type("structure_update_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,StructureUpdatePacket> CODEC = TinkerPayload.codec(StructureUpdatePacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<StructureUpdatePacket> type() { return TYPE; }

  private final BlockPos pos;
  private final BlockPos minPos;
  private final BlockPos maxPos;
  private final List<BlockPos> tanks;

  public StructureUpdatePacket(RegistryFriendlyByteBuf buffer) {
    pos = buffer.readBlockPos();
    minPos = buffer.readBlockPos();
    maxPos = buffer.readBlockPos();
    int count = buffer.readVarInt();
    tanks = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      tanks.add(buffer.readBlockPos());
    }
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeBlockPos(minPos);
    buffer.writeBlockPos(maxPos);
    buffer.writeVarInt(tanks.size());
    for (BlockPos tank : tanks) {
      buffer.writeBlockPos(tank);
    }
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<HeatingStructureBlockEntity> blockEntityType() {
    return HeatingStructureBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, HeatingStructureBlockEntity be) {
    be.setStructureSize(minPos, maxPos, tanks);
  }
}

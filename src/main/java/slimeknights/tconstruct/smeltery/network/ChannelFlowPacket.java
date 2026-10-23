package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerBlockEntityPayload;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;

/**
 * Packet for when the flowing state changes on a channel side.
 * TODO 1.21: make a record.
 */
@RequiredArgsConstructor
@ToString
public class ChannelFlowPacket implements TinkerBlockEntityPayload<ChannelBlockEntity> {
  public static final Type<ChannelFlowPacket> TYPE = TinkerPayload.type("channel_flow_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,ChannelFlowPacket> CODEC = TinkerPayload.codec(ChannelFlowPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<ChannelFlowPacket> type() { return TYPE; }

	private final BlockPos pos;
	private final Direction side;
	private final boolean flow;

	public ChannelFlowPacket(RegistryFriendlyByteBuf buffer) {
		pos = buffer.readBlockPos();
		side = buffer.readEnum(Direction.class);
		flow = buffer.readBoolean();
	}

	@Override
	public void encode(RegistryFriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeEnum(side);
		buffer.writeBoolean(flow);
	}

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<ChannelBlockEntity> blockEntityType() {
    return ChannelBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ChannelBlockEntity be) {
    be.setFlow(side, flow);
  }
}

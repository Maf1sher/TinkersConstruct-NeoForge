package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerBlockEntityPayload;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent whenever the contents of the smeltery tank change.
 * TODO 1.21: make record
 */
@AllArgsConstructor
public class SmelteryTankUpdatePacket implements TinkerBlockEntityPayload<ISmelteryTankHandler> {
  public static final Type<SmelteryTankUpdatePacket> TYPE = TinkerPayload.type("smeltery_tank_update_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,SmelteryTankUpdatePacket> CODEC = TinkerPayload.codec(SmelteryTankUpdatePacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<SmelteryTankUpdatePacket> type() { return TYPE; }

  private final BlockPos pos;
  private final List<FluidStack> fluids;

  public SmelteryTankUpdatePacket(RegistryFriendlyByteBuf buffer) {
    pos = buffer.readBlockPos();
    int size = buffer.readVarInt();
    fluids = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      fluids.add(FluidStack.STREAM_CODEC.decode(buffer));
    }
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeVarInt(fluids.size());
    for (FluidStack fluid : fluids) {
      FluidStack.STREAM_CODEC.encode(buffer, fluid);
    }
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<ISmelteryTankHandler> blockEntityType() {
    return ISmelteryTankHandler.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ISmelteryTankHandler be) {
    be.updateFluidsFromPacket(fluids);
  }
}

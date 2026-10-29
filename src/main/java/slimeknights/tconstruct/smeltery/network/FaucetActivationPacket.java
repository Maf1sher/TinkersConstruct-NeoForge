package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerBlockEntityPayload;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

/**
 * Sent to clients to activate the faucet animation clientside.
 * TODO 1.21: make record.
 */
@RequiredArgsConstructor
@ToString(callSuper = true)
public class FaucetActivationPacket implements TinkerBlockEntityPayload<FaucetBlockEntity> {
  public static final Type<FaucetActivationPacket> TYPE = TinkerPayload.type("faucet_activation_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,FaucetActivationPacket> CODEC = TinkerPayload.codec(FaucetActivationPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<FaucetActivationPacket> type() { return TYPE; }

  protected final BlockPos pos;
  protected final FluidStack fluid;
  private final boolean isPouring;

  public FaucetActivationPacket(RegistryFriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.fluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.isPouring = buffer.readBoolean();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
    buffer.writeBoolean(isPouring);
  }

  @Override
  public BlockPos pos() {
    return pos;
  }

  @Override
  public Class<FaucetBlockEntity> blockEntityType() {
    return FaucetBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, FaucetBlockEntity be) {
    be.onActivationPacket(fluid, isPouring);
  }
}

package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
@AllArgsConstructor
public class SmelteryFluidClickedPacket implements TinkerPayload {
  public static final Type<SmelteryFluidClickedPacket> TYPE = TinkerPayload.type("smeltery_fluid_clicked_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,SmelteryFluidClickedPacket> CODEC = TinkerPayload.codec(SmelteryFluidClickedPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<SmelteryFluidClickedPacket> type() { return TYPE; }

  private final int index;

  public SmelteryFluidClickedPacket(RegistryFriendlyByteBuf buffer) {
    index = buffer.readVarInt();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(index);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      if (context.player() instanceof ServerPlayer sender && !sender.isSpectator()) {
        AbstractContainerMenu container = sender.containerMenu;
        if (container instanceof BaseContainerMenu<?> base && base.getTile() instanceof ISmelteryTankHandler tank) {
          tank.getTank().moveFluidToBottom(index);
        }
      }
    });
  }
}

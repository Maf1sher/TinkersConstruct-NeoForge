package slimeknights.tconstruct.tools.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements TinkerPayload {
  public static final Type<ToolContainerFluidUpdatePacket> TYPE = TinkerPayload.type("tool_container_fluid_update_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,ToolContainerFluidUpdatePacket> CODEC = TinkerPayload.codec(ToolContainerFluidUpdatePacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<ToolContainerFluidUpdatePacket> type() { return TYPE; }

  public ToolContainerFluidUpdatePacket(RegistryFriendlyByteBuf buffer) {
    this(FluidStack.STREAM_CODEC.decode(buffer));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    FluidStack.STREAM_CODEC.encode(buffer, fluid);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      Player player = SafeClientAccess.getPlayer();
      if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
        toolMenu.getTank().setFluid(fluid);
      }
    });
  }
}

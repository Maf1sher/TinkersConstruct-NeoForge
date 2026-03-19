package slimeknights.tconstruct.tools.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<ToolContainerFluidUpdatePacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("tool_container_fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolContainerFluidUpdatePacket> STREAM_CODEC = StreamCodec.ofMember(ToolContainerFluidUpdatePacket::encode, ToolContainerFluidUpdatePacket::decode);

  public static ToolContainerFluidUpdatePacket decode(RegistryFriendlyByteBuf buffer) {
    return new ToolContainerFluidUpdatePacket(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer));
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(ToolContainerFluidUpdatePacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Player player = SafeClientAccess.getPlayer();
      if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
        toolMenu.getTank().setFluid(payload.fluid);
      }
    });
  }
}

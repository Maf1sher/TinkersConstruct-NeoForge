package slimeknights.tconstruct.tools.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;

class ToolContainerFluidUpdatePacketTest extends BaseMcTest {
  @Test
  void emptyFluidRoundTrip() {
    ToolContainerFluidUpdatePacket packet = new ToolContainerFluidUpdatePacket(FluidStack.EMPTY);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    ToolContainerFluidUpdatePacket.CODEC.encode(buffer, packet);

    ToolContainerFluidUpdatePacket decoded = ToolContainerFluidUpdatePacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.fluid().isEmpty()).isTrue();
  }

  @Test
  void fluidRoundTrip() {
    FluidStack fluid = new FluidStack(Fluids.WATER, 500);
    ToolContainerFluidUpdatePacket packet = new ToolContainerFluidUpdatePacket(fluid);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    ToolContainerFluidUpdatePacket.CODEC.encode(buffer, packet);

    ToolContainerFluidUpdatePacket decoded = ToolContainerFluidUpdatePacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.fluid().isFluidEqual(fluid)).isTrue();
    assertThat(decoded.fluid().getAmount()).isEqualTo(500);
  }
}

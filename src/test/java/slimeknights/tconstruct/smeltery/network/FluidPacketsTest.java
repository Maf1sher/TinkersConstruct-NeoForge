package slimeknights.tconstruct.smeltery.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;

/** Regression tests for fluid payloads that may carry an empty stack. */
class FluidPacketsTest extends BaseMcTest {
  @Test
  void faucetActivation_emptyFluidRoundTrip() {
    FaucetActivationPacket packet = new FaucetActivationPacket(BlockPos.ZERO, FluidStack.EMPTY, false);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    FaucetActivationPacket.CODEC.encode(buffer, packet);

    FaucetActivationPacket decoded = FaucetActivationPacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.pos()).isEqualTo(BlockPos.ZERO);
    assertThat(decoded.fluid.isEmpty()).isTrue();
  }

  @Test
  void faucetActivation_fluidRoundTrip() {
    FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
    FaucetActivationPacket packet = new FaucetActivationPacket(BlockPos.ZERO, fluid, true);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    FaucetActivationPacket.CODEC.encode(buffer, packet);

    FaucetActivationPacket decoded = FaucetActivationPacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.fluid.isFluidEqual(fluid)).isTrue();
    assertThat(decoded.fluid.getAmount()).isEqualTo(1000);
  }

  @Test
  void fluidUpdate_emptyFluidRoundTrip() {
    FluidUpdatePacket packet = new FluidUpdatePacket(BlockPos.ZERO, FluidStack.EMPTY);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    FluidUpdatePacket.CODEC.encode(buffer, packet);

    FluidUpdatePacket decoded = FluidUpdatePacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.pos()).isEqualTo(BlockPos.ZERO);
    assertThat(decoded.fluid.isEmpty()).isTrue();
  }

  @Test
  void fluidUpdate_fluidRoundTrip() {
    FluidStack fluid = new FluidStack(Fluids.WATER, 144);
    FluidUpdatePacket packet = new FluidUpdatePacket(BlockPos.ZERO, fluid);
    RegistryFriendlyByteBuf buffer = TestHelper.registryBuffer();
    FluidUpdatePacket.CODEC.encode(buffer, packet);

    FluidUpdatePacket decoded = FluidUpdatePacket.CODEC.decode(buffer);
    assertThat(buffer.readableBytes()).isZero();
    assertThat(decoded.fluid.isFluidEqual(fluid)).isTrue();
    assertThat(decoded.fluid.getAmount()).isEqualTo(144);
  }
}

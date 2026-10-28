package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantFluidContainerWrapperTest extends BaseMcTest {
  @Test
  void onlyDrainsTheWholeContainerAndSimulationDoesNotConsumeIt() {
    ItemStack filledContainer = new ItemStack(Items.POTION);
    ConstantFluidContainerWrapper handler = new ConstantFluidContainerWrapper(
      new FluidStack(Fluids.WATER, 250), filledContainer, new ItemStack(Items.GLASS_BOTTLE));

    assertThat(handler.getTankCapacity(0)).isEqualTo(250);
    assertThat(handler.fill(new FluidStack(Fluids.LAVA, 250), FluidAction.EXECUTE)).isZero();
    assertThat(handler.drain(249, FluidAction.EXECUTE).isEmpty()).isTrue();
    assertThat(handler.drain(250, FluidAction.SIMULATE).getAmount()).isEqualTo(250);
    assertThat(handler.getContainer()).isSameAs(filledContainer);
    assertThat(handler.getFluidInTank(0).getAmount()).isEqualTo(250);

    assertThat(handler.drain(250, FluidAction.EXECUTE).getAmount()).isEqualTo(250);
    assertThat(handler.getContainer().is(Items.GLASS_BOTTLE)).isTrue();
    assertThat(handler.getFluidInTank(0).isEmpty()).isTrue();
  }
}

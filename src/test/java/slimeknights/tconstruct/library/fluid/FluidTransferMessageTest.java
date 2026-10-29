package slimeknights.tconstruct.library.fluid;

import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the drain chat message. Fluid names used to be passed as {@code Fluid} objects, which cannot be
 * encoded by the component codec; the exception is thrown on the Netty thread and disconnects the client.
 */
class FluidTransferMessageTest extends BaseMcTest {

  @Test
  void drainedMessageEncodesFluidNameAsComponent() {
    Component message = FluidTransferHelper.drainedMessage(150, Component.literal("Liquid Soul"));

    assertThat(message.getContents()).isInstanceOf(TranslatableContents.class);
    Object[] args = ((TranslatableContents) message.getContents()).getArgs();
    assertThat(args).hasSize(2);
    // amount is a formatted string, the fluid name must be a component (not a Fluid or other registry object)
    assertThat(args[1]).isInstanceOf(Component.class);

    // this is what the clientbound system_chat packet does; registry objects made it fail here
    assertThat(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, message).result()).isPresent();
  }

  @Test
  void filledMessageEncodesFluidNameAsComponent() {
    Component message = FluidTransferHelper.filledMessage(1000, Component.literal("Lava"));

    Object[] args = ((TranslatableContents) message.getContents()).getArgs();
    assertThat(args).hasSize(2);
    assertThat(args[1]).isInstanceOf(Component.class);
    assertThat(ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, message).result()).isPresent();
  }
}

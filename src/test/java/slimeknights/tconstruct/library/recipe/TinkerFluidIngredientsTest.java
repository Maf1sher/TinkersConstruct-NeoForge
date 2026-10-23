package slimeknights.tconstruct.library.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.tools.network.PushBlockRowPacket;

import static org.assertj.core.api.Assertions.assertThat;

class TinkerFluidIngredientsTest {
  private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  @Test
  void sizedFluidIngredientPreservesAmountAndJsonCodec() {
    SizedFluidIngredient ingredient = SizedFluidIngredient.of(Fluids.WATER, 250);

    assertThat(ingredient.test(new FluidStack(Fluids.WATER, 250))).isTrue();
    assertThat(ingredient.test(new FluidStack(Fluids.WATER, 249))).isFalse();
    assertThat(ingredient.test(new FluidStack(Fluids.LAVA, 250))).isFalse();
    assertThat(TinkerFluidIngredients.matchesFluid(ingredient, new FluidStack(Fluids.WATER, 1))).isTrue();
    assertThat(TinkerFluidIngredients.amountFor(ingredient, Fluids.WATER)).isEqualTo(250);
    assertThat(TinkerFluidIngredients.amountFor(ingredient, Fluids.LAVA)).isZero();
    assertThat(TinkerFluidIngredients.displayFluids(null)).isEmpty();

    RegistryOps<JsonElement> ops = REGISTRIES.createSerializationContext(JsonOps.INSTANCE);
    JsonElement json = SizedFluidIngredient.FLAT_CODEC.encodeStart(ops, ingredient).getOrThrow();
    assertThat(json.getAsJsonObject().get("amount").getAsInt()).isEqualTo(250);
    assertThat(SizedFluidIngredient.FLAT_CODEC.parse(ops, json).getOrThrow()).isEqualTo(ingredient);
  }

  @Test
  void payloadCodecRoundTripsRegistryAwarePacket() {
    PushBlockRowPacket packet = new PushBlockRowPacket(new BlockPos(2, 3, 4), Direction.NORTH, true, 5);
    RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), REGISTRIES);
    try {
      PushBlockRowPacket.CODEC.encode(buffer, packet);
      assertThat(PushBlockRowPacket.CODEC.decode(buffer)).isEqualTo(packet);
    } finally {
      buffer.release();
    }
  }
}

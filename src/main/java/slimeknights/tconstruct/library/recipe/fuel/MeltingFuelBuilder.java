package slimeknights.tconstruct.library.recipe.fuel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

import java.util.function.Consumer;

import static slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe.getTemperature;

/**
 * Builds a new recipe for a melter or smeltery fuel
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeltingFuelBuilder extends AbstractRecipeBuilder<MeltingFuelBuilder> {
  @Nullable private final SizedFluidIngredient input;
  private final int duration;
  private final int temperature;
  @Setter
  @Accessors(fluent = true)
  private int rate;

  /**
   * Creates a new builder instance with set temperature
   * @param fluid     Fluid stack
   * @param duration  Fluid duration
   * @return  Builder instance
   */
  public static MeltingFuelBuilder fuel(@Nullable SizedFluidIngredient fluid, int duration, int temperature) {
    return new MeltingFuelBuilder(fluid, duration, temperature, temperature / 100);
  }

  /**
   * Creates a new builder instance with automatic temperature
   * @param fluid     Fluid stack
   * @param duration  Fluid duration
   * @return  Builder instance
   */
  public static MeltingFuelBuilder fuel(FluidStack fluid, int duration) {
    return fuel(SizedFluidIngredient.of(fluid), duration, getTemperature(fluid));
  }

  /** Setups the builder for solid fuel */
  @Internal
  public static MeltingFuelBuilder solid(int temperature) {
    return fuel(null, 0, temperature);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    if (input == null || input.getFluids().length == 0) {
      throw new IllegalStateException("Must have at least one fluid for dynamic input");
    }
    save(consumer, Loadables.FLUID.getKey(input.getFluids()[0].getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "melting_fuel");
    consumer.accept(new LoadableFinishedRecipe<>(new MeltingFuel(id, input, duration, temperature, rate), MeltingFuel.LOADER, advancementId));
  }
}

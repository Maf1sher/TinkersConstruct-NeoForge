package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/** Helpers for preserving Tinkers' fluid ingredient matching semantics. */
public final class TinkerFluidIngredients {
  private TinkerFluidIngredients() {}

  /** Checks a fluid identity without applying the ingredient's required amount. */
  public static boolean matchesFluid(SizedFluidIngredient ingredient, FluidStack fluid) {
    return ingredient.ingredient().test(fluid);
  }

  /** Checks a fluid identity without applying the ingredient's required amount. */
  public static boolean matchesFluid(SizedFluidIngredient ingredient, net.minecraft.world.level.material.Fluid fluid) {
    return ingredient.ingredient().test(new FluidStack(fluid, 1));
  }

  /** Gets the required amount only when the fluid matches. */
  public static int amountFor(@Nullable SizedFluidIngredient ingredient, net.minecraft.world.level.material.Fluid fluid) {
    return ingredient != null && matchesFluid(ingredient, fluid) ? ingredient.amount() : 0;
  }

  /**
   * Checks if the given stack can be shown in recipe viewers.
   * Fluid tags such as {@code c:molten_iron} include both the still and flowing variants, but JEI only registers source
   * fluids as ingredients. Filtering here keeps display lists in sync with the ingredients JEI accepts, which matters
   * for focus links as JEI requires every linked slot to have the same number of ingredients.
   */
  public static boolean isDisplayFluid(FluidStack stack) {
    Fluid fluid = stack.getFluid();
    return !stack.isEmpty() && fluid.isSource(fluid.defaultFluidState());
  }

  /** Gets display stacks, filtering out fluids JEI does not accept as ingredients. Null represents the former empty-fluid sentinel. */
  public static List<FluidStack> displayFluids(@Nullable SizedFluidIngredient ingredient) {
    if (ingredient == null) {
      return List.of();
    }
    return Arrays.stream(ingredient.getFluids()).filter(TinkerFluidIngredients::isDisplayFluid).toList();
  }
}

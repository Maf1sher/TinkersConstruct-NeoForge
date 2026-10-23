package slimeknights.tconstruct.library.recipe;

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

  /** Gets display stacks, with null representing the former empty-fluid sentinel. */
  public static List<FluidStack> displayFluids(@Nullable SizedFluidIngredient ingredient) {
    return ingredient == null ? List.of() : Arrays.asList(ingredient.getFluids());
  }
}

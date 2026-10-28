package slimeknights.tconstruct.plugin.jei.util.manager;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.display.FilteredFluidRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/** Cache for searching for recipes from a list of {@link FilteredFluidRecipe}. */
public class FluidRecipeCache<W, T extends FilteredFluidRecipe> extends AbstractRecipeCache<W, FluidStack> {
  private final Function<W,T> value;
  private final boolean output;
  public FluidRecipeCache(IIngredientHelper<FluidStack> helper, List<? extends W> recipes, Function<W,T> value, boolean output) {
    super(helper, recipes);
    this.value = value;
    this.output = output;
  }

  @Override
  protected boolean matches(W recipe, Predicate<FluidStack> predicate) {
    return value.apply(recipe).matchesFluid(predicate, this.output);
  }

  @Override
  public List<W> filterRecipes(FluidStack focus) {
    List<W> recipes = matchingRecipes(focus);
    if (recipes.isEmpty()) {
      return List.of();
    }
    List<W> filtered = new ArrayList<>(recipes.size());
    for (W recipe : recipes) {
      if (value.apply(recipe).isVisibleFromFluid(focus, output)) {
        filtered.add(recipe);
      }
    }
    return filtered;
  }
}

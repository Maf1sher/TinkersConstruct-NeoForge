package slimeknights.tconstruct.plugin.jei.util.manager;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.recipe.display.FilteredItemRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/** Cache for searching for recipes from a list of {@link FilteredItemRecipe} */
public class ItemRecipeCache<W, T extends FilteredItemRecipe> extends AbstractRecipeCache<W, ItemStack> {
  private final Function<W,T> value;
  private final boolean output;
  public ItemRecipeCache(IIngredientHelper<ItemStack> helper, List<? extends W> recipes, Function<W,T> value, boolean output) {
    super(helper, recipes);
    this.value = value;
    this.output = output;
  }

  @Override
  protected boolean matches(W recipe, Predicate<ItemStack> predicate) {
    return value.apply(recipe).matchesItem(predicate, output);
  }

  @Override
  public List<W> filterRecipes(ItemStack focus) {
    List<W> recipes = matchingRecipes(focus);
    if (recipes.isEmpty()) {
      return List.of();
    }
    List<W> filtered = new ArrayList<>(recipes.size());
    for (W recipe : recipes) {
      if (value.apply(recipe).isVisibleFromItem(focus, output)) {
        filtered.add(recipe);
      }
    }
    return filtered;
  }
}

package slimeknights.tconstruct.library.recipe.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Interface for vanilla recipes to allow them to expose filtered recipes. Used since JEI will not filter the list to remove filtered recipes from the recipe manager. */
public interface VanillaFilteredRecipe<T extends FilteredRecipe> {
  /**
   * Gets a list of filtered recipes for adding to a recipe manager plugin.
   * @param id      ID of the parent recipe
   * @param access  Registry access instance
   * @return  List of recipes
   */
  List<T> getFilteredRecipes(ResourceLocation id, RegistryAccess access);


  /** Gets a modifiable list of filtered recipes matching the given recipe type. */
  static <T extends FilteredRecipe, I extends RecipeInput, R extends Recipe<I>> List<T> getRecipes(RegistryAccess access, RecipeManager manager, RecipeType<R> type, Class<T> recipeClass) {
    return manager.getAllRecipesFor(type).stream()
      .flatMap(holder -> holder.value() instanceof VanillaFilteredRecipe<?> filtered ? filtered.getFilteredRecipes(holder.id(), access).stream() : Stream.empty())
      .filter(recipeClass::isInstance).map(recipeClass::cast).collect(Collectors.toList());
  }

  /** Gets a modifiable list of filtered recipes matching the given recipe type, wrapped in holders with derived IDs. */
  static <T extends FilteredRecipe, I extends RecipeInput, R extends Recipe<I>> List<RecipeHolder<T>> getRecipeHolders(RegistryAccess access, RecipeManager manager, RecipeType<R> type, Class<T> recipeClass) {
    List<RecipeHolder<T>> recipes = new ArrayList<>();
    for (RecipeHolder<R> holder : manager.getAllRecipesFor(type)) {
      if (holder.value() instanceof VanillaFilteredRecipe<?> filtered) {
        List<?> filteredRecipes = filtered.getFilteredRecipes(holder.id(), access);
        for (int i = 0; i < filteredRecipes.size(); i++) {
          Object recipe = filteredRecipes.get(i);
          if (recipeClass.isInstance(recipe)) {
            recipes.add(new RecipeHolder<>(holder.id().withSuffix("/" + i), recipeClass.cast(recipe)));
          }
        }
      }
    }
    return recipes;
  }
}

package slimeknights.tconstruct.library.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.tconstruct.library.recipe.display.DisplayRecipe;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/** Helper methods for getting recipes from the recipe manager, replicating removed Mantle helpers */
public class RecipeHelper {
  private RecipeHelper() {}

  /** Gets all recipes of the given type, filtering to the passed type for safety */
  public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends Recipe<I>> List<T> getRecipes(RecipeManager manager, RecipeType<T> type, Class<T> recipeClass) {
    return manager.getAllRecipesFor(type).stream().map(RecipeHolder::value).filter(recipeClass::isInstance).map(recipeClass::cast).toList();
  }

  /**
   * Gets JEI display recipes of the given type, expanding multi recipes, matching the removed Mantle helper.
   * @param provider     Registry access
   * @param manager      Recipe manager
   * @param type         Recipe type to query
   * @param recipeClass  Class for final recipes
   * @return  List of display recipes
   */
  public static <T, R extends Recipe<?>> List<T> getJEIRecipes(Provider provider, RecipeManager manager, RecipeType<R> type, Class<T> recipeClass) {
    return MultiNamedRecipe.streamRecipes(provider, manager, type, recipeClass).toList();
  }

  /**
   * Gets JEI display recipes of the given type wrapped in holders, expanding multi recipes.
   * Keeps the recipe IDs so JEI categories can report a registry name for bookmarks and lookups.
   * @param provider     Registry access
   * @param manager      Recipe manager
   * @param type         Recipe type to query
   * @param recipeClass  Class for final recipes
   * @return  List of display recipe holders
   */
  public static <T, R extends Recipe<?>> List<DisplayRecipe<T>> getJEIDisplayRecipes(Provider provider, RecipeManager manager, RecipeType<R> type, Class<T> recipeClass) {
    return MultiNamedRecipe.streamNamedRecipes(provider, manager, type, recipeClass)
      .map(named -> new DisplayRecipe<>(named.id(), named.recipe()))
      .toList();
  }

  /** Wraps a list of recipes in display recipes using the given ID as a prefix, matching the multi recipe naming scheme. */
  public static <T> List<DisplayRecipe<T>> hold(ResourceLocation id, List<T> recipes) {
    return DisplayRecipe.of(id, recipes);
  }

  /** Wraps a list of recipes in display recipes using IDs supplied by the recipe. */
  public static <T> List<DisplayRecipe<T>> hold(Function<T,ResourceLocation> idGetter, List<T> recipes) {
    return recipes.stream().map(recipe -> new DisplayRecipe<>(idGetter.apply(recipe), recipe)).toList();
  }

  /** Gets the recipe holder with the given ID, filtering to the passed type for safety */
  public static <T extends Recipe<?>> java.util.Optional<RecipeHolder<T>> getRecipeHolder(RecipeManager manager, ResourceLocation id, Class<T> recipeClass) {
    return manager.byKey(id)
      .filter(holder -> recipeClass.isInstance(holder.value()))
      .map(holder -> new RecipeHolder<>(holder.id(), recipeClass.cast(holder.value())));
  }

  /** Wraps a list of display recipes with unique derived names for the given base recipe */
  public static <T> List<NamedRecipe<T>> named(ResourceLocation id, List<T> recipes) {
    return IntStream.range(0, recipes.size()).mapToObj(i -> NamedRecipe.of(id.withSuffix("/" + i), recipes.get(i))).toList();
  }
}

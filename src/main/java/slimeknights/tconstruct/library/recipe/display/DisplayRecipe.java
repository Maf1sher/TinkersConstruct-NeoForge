package slimeknights.tconstruct.library.recipe.display;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.IntStream;

/**
 * A recipe value paired with its ID, used for JEI display of custom recipe types.
 * Unlike {@link net.minecraft.world.item.crafting.RecipeHolder}, the value does not need to be a vanilla recipe,
 * which is required for our display-only recipe interfaces. Vanilla recipes still use {@code RecipeHolder}.
 * @param id     Recipe ID
 * @param value  Recipe value
 * @param <T>    Recipe type
 */
public record DisplayRecipe<T>(ResourceLocation id, T value) {
  /** Wraps a list of recipes with unique derived names for the given base recipe */
  public static <T> List<DisplayRecipe<T>> of(ResourceLocation id, List<T> recipes) {
    int size = recipes.size();
    return IntStream.range(0, size)
      .mapToObj(i -> new DisplayRecipe<T>(size == 1 ? id : id.withSuffix("/" + i), recipes.get(i)))
      .toList();
  }
}

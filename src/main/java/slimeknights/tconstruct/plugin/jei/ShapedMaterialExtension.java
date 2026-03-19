package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.plugin.jei.material.MaterialsCraftingExtension;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.IntStream;

/**
 * Logic to show {@link ShapedMaterialRecipe} in JEI.
 * Singleton extension that caches per-recipe data.
 */
public class ShapedMaterialExtension implements ICraftingCategoryExtension<ShapedMaterialRecipe> {

  /** Cached data for each recipe */
  private record CachedData(ItemStack plainResult, List<ItemStack> result, int[] materialSlots) {}

  private final Map<ShapedMaterialRecipe, CachedData> cache = new WeakHashMap<>();

  private CachedData getData(ShapedMaterialRecipe recipe) {
    return cache.computeIfAbsent(recipe, r -> {
      MaterialValueIngredient materials = r.getMaterial();
      ItemStack plainResult = r.getResultItem(Objects.requireNonNull(SafeClientAccess.getRegistryAccess()));
      List<ItemStack> result;
      if (materials != null) {
        result = MaterialRecipeCache.getAllRecipes().stream().filter(materials::test).flatMap(mat -> {
          ItemStack stack = plainResult.copy();
          r.setMaterial(stack, mat.getMaterial().getVariant());
          return IntStream.range(0, mat.getIngredient().getItems().length).mapToObj(i -> stack);
        }).toList();
      } else {
        result = List.of(plainResult);
      }
      List<Ingredient> inputs = r.getIngredients();
      int[] materialSlots = IntStream.range(0, inputs.size()).filter(i -> inputs.get(i).getCustomIngredient() instanceof MaterialValueIngredient).toArray();
      return new CachedData(plainResult, result, materialSlots);
    });
  }

  @Override
  public int getWidth(RecipeHolder<ShapedMaterialRecipe> recipeHolder) {
    return recipeHolder.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedMaterialRecipe> recipeHolder) {
    return recipeHolder.value().getHeight();
  }

  @Override
  public void setRecipe(RecipeHolder<ShapedMaterialRecipe> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focusGroup) {
    ShapedMaterialRecipe recipe = recipeHolder.value();
    CachedData data = getData(recipe);
    MaterialsCraftingExtension.setRecipe(this, recipeHolder, builder, craftingGridHelper, recipe, data.result, data.plainResult, data.materialSlots);
  }
}

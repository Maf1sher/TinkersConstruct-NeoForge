package slimeknights.tconstruct.plugin.jei.material;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;

import java.util.List;
import java.util.stream.IntStream;

/** Logic to show {@link ShapedMaterialsRecipe} in JEI */
public class ShapedMaterialsExtension extends MaterialsCraftingExtension<ShapedMaterialsRecipe> {

  @Override
  public boolean isHandled(RecipeHolder<ShapedMaterialsRecipe> recipeHolder) {
    for (Ingredient ingredient : recipeHolder.value().getParts()) {
      if (ingredient.getItems().length == 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected int[] getMaterialSlots(ShapedMaterialsRecipe recipe, Ingredient firstPart) {
    List<Ingredient> inputs = recipe.getIngredients();
    return IntStream.range(0, inputs.size()).filter(i -> inputs.get(i) == firstPart).toArray();
  }

  @Override
  public int getWidth(RecipeHolder<ShapedMaterialsRecipe> recipeHolder) {
    return recipeHolder.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedMaterialsRecipe> recipeHolder) {
    return recipeHolder.value().getHeight();
  }
}

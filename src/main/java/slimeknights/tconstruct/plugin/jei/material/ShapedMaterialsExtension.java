package slimeknights.tconstruct.plugin.jei.material;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;

import java.util.List;
import java.util.stream.IntStream;

/** Logic to show {@link ShapedMaterialsRecipe} in JEI */
public class ShapedMaterialsExtension extends MaterialsCraftingExtension<ShapedMaterialsRecipe> {
  public ShapedMaterialsExtension() {}

  @Override
  protected boolean isValid(ShapedMaterialsRecipe recipe) {
    for (Ingredient ingredient : recipe.getParts()) {
      if (ingredient.getItems().length == 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected int[] getMaterialSlots(ShapedMaterialsRecipe recipe, Ingredient part) {
    List<Ingredient> inputs = recipe.getIngredients();
    return IntStream.range(0, inputs.size()).filter(i -> inputs.get(i) == part).toArray();
  }

  @Override
  public int getWidth(RecipeHolder<ShapedMaterialsRecipe> holder) {
    return holder.value().getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<ShapedMaterialsRecipe> holder) {
    return holder.value().getHeight();
  }
}

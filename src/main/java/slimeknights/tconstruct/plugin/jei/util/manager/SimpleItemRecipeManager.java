package slimeknights.tconstruct.plugin.jei.util.manager;

import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.library.recipe.display.FilteredItemRecipe;

import java.util.List;
import java.util.function.Function;

/** Recipe manager handling filtering recipes that use just items for inputs and outputs. Used notably in {@link slimeknights.tconstruct.plugin.jei.modifiers.ToolTinkeringCategory}. */
public class SimpleItemRecipeManager<W, T extends FilteredItemRecipe> implements ISimpleRecipeManagerPlugin<W> {
  @Getter
  private final List<W> allRecipes;
  private final ItemRecipeCache<W,T> inputItemCache, outputItemCache;

  public SimpleItemRecipeManager(IIngredientManager ingredientManager, List<W> recipes, Function<W,T> value) {
    this.allRecipes = recipes.stream().filter(recipe -> value.apply(recipe).showUnfocused()).toList();
    IIngredientHelper<ItemStack> itemHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
    inputItemCache = new ItemRecipeCache<>(itemHelper, recipes, value, false);
    outputItemCache = new ItemRecipeCache<>(itemHelper, recipes, value, true);
  }

  @Override
  public boolean isHandledInput(ITypedIngredient<?> input) {
    return input.getType() == VanillaTypes.ITEM_STACK;
  }

  @Override
  public boolean isHandledOutput(ITypedIngredient<?> output) {
    return output.getType() == VanillaTypes.ITEM_STACK;
  }

  @Override
  public List<W> getRecipesForInput(ITypedIngredient<?> input) {
    ITypedIngredient<ItemStack> item = input.cast(VanillaTypes.ITEM_STACK);
    if (item != null) {
      return inputItemCache.filterRecipes(item.getIngredient());
    }
    return List.of();
  }

  @Override
  public List<W> getRecipesForOutput(ITypedIngredient<?> output) {
    ITypedIngredient<ItemStack> item = output.cast(VanillaTypes.ITEM_STACK);
    if (item != null) {
      return outputItemCache.filterRecipes(item.getIngredient());
    }
    return List.of();
  }

  /** Creates an instance of the category for the crafting table. */
  @SuppressWarnings("unchecked")
  public static <T extends CraftingRecipe & FilteredItemRecipe> ISimpleRecipeManagerPlugin<RecipeHolder<CraftingRecipe>> createCrafting(IIngredientManager ingredientManager, List<RecipeHolder<T>> recipes) {
    List<RecipeHolder<CraftingRecipe>> holders = new java.util.ArrayList<>(recipes.size());
    for (RecipeHolder<T> holder : recipes) {
      holders.add(new RecipeHolder<>(holder.id(), holder.value()));
    }
    return new SimpleItemRecipeManager<>(ingredientManager, holders, holder -> (T) holder.value());
  }
}

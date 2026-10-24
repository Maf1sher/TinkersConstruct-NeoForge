package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.common.TinkerTags;


/**
 * Builder for overslime recipes
 */
@RequiredArgsConstructor(staticName = "modifier")
public class OverslimeModifierRecipeBuilder extends AbstractRecipeBuilder<OverslimeModifierRecipeBuilder> {
  @Setter @Accessors(chain = true)
  private Ingredient tools = Ingredient.of(TinkerTags.Items.DURABILITY);
  private final Ingredient ingredient;
  private final int restoreAmount;

  /** Creates a new builder for the given item */
  public static OverslimeModifierRecipeBuilder modifier(ItemLike item, int restoreAmount) {
    return modifier(Ingredient.of(item), restoreAmount);
  }

  @Override
  public void save(RecipeOutput output) {
    ItemStack[] stacks = ingredient.getItems();
    if (stacks.length == 0) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    save(output, Loadables.ITEM.getKey(stacks[0].getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    LoadableFinishedRecipe.save(output, id, OverslimeModifierRecipe.LOADER, new OverslimeModifierRecipe(id, tools, ingredient, restoreAmount), buildOptionalAdvancement(output, id, "modifiers"));
  }

  /** Creates a crafting table overslime repair recipe */
  public OverslimeModifierRecipeBuilder saveCrafting(RecipeOutput output, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    LoadableFinishedRecipe.save(output, id, OverslimeCraftingTableRecipe.LOADER, new OverslimeCraftingTableRecipe(id, tools, ingredient, restoreAmount), buildOptionalAdvancement(output, id, "modifiers"));
    return this;
  }
}

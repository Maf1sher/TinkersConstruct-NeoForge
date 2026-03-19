package slimeknights.tconstruct.tables.recipe;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

/** Builder for {@link TinkerStationPartSwapping} */
@RequiredArgsConstructor(staticName = "tools")
public class TinkerStationPartSwappingBuilder extends AbstractRecipeBuilder<TinkerStationPartSwappingBuilder> {
  private final Ingredient tools;
  @Setter
  @Accessors(fluent = true)
  private int maxStackSize = 16;


  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, Loadables.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(RecipeOutput consumer, ResourceLocation id) {
    TinkerStationPartSwapping recipe = new TinkerStationPartSwapping(id, tools, maxStackSize);
    saveRecipe(consumer, id, recipe, null);
  }
}

package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;


/**
 * Builds a recipe to repair a tool using a modifier
 * Builder for {@link ModifierRepairTinkerStationRecipe} and {@link ModifierRepairCraftingRecipe}
 */
@RequiredArgsConstructor(staticName = "repair")
public class ModifierRepairRecipeBuilder extends AbstractRecipeBuilder<ModifierRepairRecipeBuilder> {
  private final ModifierId modifier;
  private final Ingredient ingredient;
  private final int repairAmount;

  public static ModifierRepairRecipeBuilder repair(LazyModifier modifier, Ingredient ingredient, int repairAmount) {
    return repair(modifier.getId(), ingredient, repairAmount);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, modifier.id());
  }

  /** Builds the recipe for the crafting table using a repair kit */
  public ModifierRepairRecipeBuilder buildCraftingTable(RecipeOutput output, ResourceLocation id) {
    LoadableFinishedRecipe.save(output, id, ModifierRepairCraftingRecipe.LOADER, new ModifierRepairCraftingRecipe(id, modifier, ingredient, repairAmount), buildOptionalAdvancement(output, id, "tinker_station"));
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    LoadableFinishedRecipe.save(output, id, ModifierRepairTinkerStationRecipe.LOADER, new ModifierRepairTinkerStationRecipe(id, modifier, ingredient, repairAmount), buildOptionalAdvancement(output, id, "tinker_station"));
  }
}

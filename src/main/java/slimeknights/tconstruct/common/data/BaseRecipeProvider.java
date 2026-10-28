package slimeknights.tconstruct.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;

import java.util.concurrent.CompletableFuture;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {
  /** Registry lookup passed to the provider; vanilla keeps its copy private, so we keep our own for component serialization */
  protected final CompletableFuture<HolderLookup.Provider> registriesFuture;

  public BaseRecipeProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
    super(generator, registries);
    this.registriesFuture = registries;
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  protected abstract void buildRecipes(RecipeOutput consumer);

  @Override
  public abstract String getName();

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }
}

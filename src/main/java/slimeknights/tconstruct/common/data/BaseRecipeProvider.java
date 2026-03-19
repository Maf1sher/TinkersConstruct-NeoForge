package slimeknights.tconstruct.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import slimeknights.mantle.recipe.data.IRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.ResourceId;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {
  /** Thread-local storage for additional futures from raw JSON writing during buildRecipes */
  private final List<CompletableFuture<?>> additionalFutures = new ArrayList<>();
  @Nullable
  private CachedOutput currentOutput;

  public BaseRecipeProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
    super(generator, registries);
    TConstruct.sealTinkersClass(this, "BaseRecipeProvider", "BaseRecipeProvider is trivial to recreate and directly extending can lead to addon recipes polluting our namespace.");
  }

  @Override
  protected CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider registries) {
    // Store output reference for raw JSON writing
    this.currentOutput = output;
    this.additionalFutures.clear();
    CompletableFuture<?> result = super.run(output, registries);
    // Wait for both normal recipes and any raw JSON recipes
    if (!additionalFutures.isEmpty()) {
      List<CompletableFuture<?>> all = new ArrayList<>(additionalFutures);
      all.add(result);
      return CompletableFuture.allOf(all.toArray(CompletableFuture[]::new));
    }
    return result;
  }

  @Override
  protected abstract void buildRecipes(RecipeOutput consumer);

  /**
   * Writes a recipe as raw JSON, bypassing the standard codec serialization.
   * This is needed for cross-mod recipes that reference items not registered at datagen time.
   *
   * @param id         Recipe ID
   * @param type       Recipe serializer registry name (e.g., "tconstruct:part_builder_recycling")
   * @param recipeJson The recipe's JSON fields (WITHOUT the "type" field - it will be added)
   * @param conditions Optional conditions (e.g., ModLoadedCondition)
   */
  protected void saveRawJsonRecipe(ResourceLocation id, ResourceLocation type, JsonObject recipeJson,
                                   net.neoforged.neoforge.common.conditions.ICondition... conditions) {
    if (currentOutput == null) {
      throw new IllegalStateException("saveRawJsonRecipe can only be called during buildRecipes");
    }
    // Build the final JSON with type field
    JsonObject finalJson = new JsonObject();
    // Add conditions first (NeoForge format)
    if (conditions.length > 0) {
      com.google.gson.JsonArray conditionsArray = new com.google.gson.JsonArray();
      for (net.neoforged.neoforge.common.conditions.ICondition condition : conditions) {
        conditionsArray.add(
          net.neoforged.neoforge.common.conditions.ICondition.CODEC.encodeStart(
            com.mojang.serialization.JsonOps.INSTANCE, condition
          ).getOrThrow()
        );
      }
      finalJson.add("neoforge:conditions", conditionsArray);
    }
    finalJson.addProperty("type", type.toString());
    // Copy all recipe fields
    for (java.util.Map.Entry<String, JsonElement> entry : recipeJson.entrySet()) {
      finalJson.add(entry.getKey(), entry.getValue());
    }
    // Write the JSON file
    Path path = recipePathProvider.json(id);
    additionalFutures.add(DataProvider.saveStable(currentOutput, finalJson, path));
  }

  /* ResourceId location helpers - delegates to ResourceLocation overloads in IRecipeHelper */

  /** Wraps the ResourceId in the given prefix and suffix */
  public ResourceLocation wrap(ResourceId location, String prefix, String suffix) {
    return wrap(location.location(), prefix, suffix);
  }

  /** Prefixes the ResourceId */
  public ResourceLocation prefix(ResourceId location, String prefix) {
    return prefix(location.location(), prefix);
  }

  /** Suffixes the ResourceId */
  public ResourceLocation suffix(ResourceId location, String suffix) {
    return suffix(location.location(), suffix);
  }

  @Override
  public String getModId() {
    return TConstruct.MOD_ID;
  }
}

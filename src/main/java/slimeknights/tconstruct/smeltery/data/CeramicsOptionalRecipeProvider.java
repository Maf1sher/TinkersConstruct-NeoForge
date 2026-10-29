package slimeknights.tconstruct.smeltery.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.concurrent.CompletableFuture;

/** Generates recipes for the optional Ceramics kiln without a compile-time dependency on that mod. */
public class CeramicsOptionalRecipeProvider implements DataProvider {
  private static final String MOD_ID = "ceramics";
  private final PackOutput.PathProvider recipePathProvider;

  public CeramicsOptionalRecipeProvider(PackOutput output) {
    this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
  }

  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    return CompletableFuture.allOf(
      save(output, "smeltery/seared/seared_brick_kiln_ceramics", TinkerSmeltery.grout, TinkerSmeltery.searedBrick),
      save(output, "smeltery/scorched/scorched_brick_kiln_ceramics", TinkerSmeltery.netherGrout, TinkerSmeltery.scorchedBrick)
    );
  }

  private CompletableFuture<?> save(CachedOutput output, String path, ItemLike input, ItemLike result) {
    JsonObject recipe = new JsonObject();
    recipe.addProperty("type", "ceramics:kiln");
    recipe.addProperty("category", "misc");
    recipe.addProperty("cookingtime", 100);
    recipe.addProperty("experience", 0.3f);
    JsonObject ingredient = new JsonObject();
    ingredient.addProperty("item", itemId(input.asItem()).toString());
    recipe.add("ingredient", ingredient);
    recipe.addProperty("result", itemId(result.asItem()).toString());

    JsonObject modLoaded = new JsonObject();
    modLoaded.addProperty("type", "neoforge:mod_loaded");
    modLoaded.addProperty("modid", MOD_ID);
    JsonArray conditions = new JsonArray();
    conditions.add(modLoaded);
    recipe.add("neoforge:conditions", conditions);

    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TConstruct.MOD_ID, path);
    return DataProvider.saveStable(output, recipe, recipePathProvider.json(id));
  }

  private static ResourceLocation itemId(Item item) {
    return BuiltInRegistries.ITEM.getKey(item);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Ceramics recipes";
  }
}

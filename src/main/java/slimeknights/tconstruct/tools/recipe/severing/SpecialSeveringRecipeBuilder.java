package slimeknights.tconstruct.tools.recipe.severing;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;

import java.util.Objects;
import java.util.function.Supplier;

/** Builder for severing recipes that have only the base chance and looting bonus as fields */
@Setter
@Accessors(chain = true)
public class SpecialSeveringRecipeBuilder extends AbstractRecipeBuilder<SpecialSeveringRecipeBuilder> {
  /** Factory function to create the recipe from id, baseChance, and lootingBonus */
  @FunctionalInterface
  public interface RecipeFactory {
    SeveringRecipe create(ResourceLocation id, float baseChance, float lootingBonus);
  }

  private final RecipeSerializer<? extends SeveringRecipe> serializer;
  private final RecipeFactory factory;
  private float baseChance = 0.05f;
  private float lootingBonus = 0.01f;

  private SpecialSeveringRecipeBuilder(RecipeSerializer<? extends SeveringRecipe> serializer, RecipeFactory factory) {
    this.serializer = serializer;
    this.factory = factory;
  }

  /** Creates a new builder for the given serializer and factory. */
  public static SpecialSeveringRecipeBuilder serializer(RecipeSerializer<? extends SeveringRecipe> serializer, RecipeFactory factory) {
    return new SpecialSeveringRecipeBuilder(serializer, factory);
  }

  /** Creates a new builder for the given serializer supplier and factory. */
  public static SpecialSeveringRecipeBuilder serializer(Supplier<? extends RecipeSerializer<? extends SeveringRecipe>> supplier, RecipeFactory factory) {
    return serializer(supplier.get(), factory);
  }

  /** Doubles the drop chances for this rare mob */
  public SpecialSeveringRecipeBuilder rareMob() {
    baseChance = 0.1f;
    lootingBonus = 0.02f;
    return this;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void save(RecipeOutput consumer) {
    save(consumer, Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer)));
  }

  @Override
  public void save(RecipeOutput consumer, ResourceLocation id) {
    SeveringRecipe recipe = factory.create(id, baseChance, lootingBonus);
    saveRecipe(consumer, id, recipe, null);
  }
}

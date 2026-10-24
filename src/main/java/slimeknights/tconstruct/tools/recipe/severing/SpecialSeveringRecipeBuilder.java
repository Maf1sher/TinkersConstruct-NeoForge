package slimeknights.tconstruct.tools.recipe.severing;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.Objects;
import java.util.function.Supplier;

/** Builder for severing recipes that have only the base chance and looting bonus as fields */
@Setter
@Accessors(chain = true)
public class SpecialSeveringRecipeBuilder extends AbstractRecipeBuilder<SpecialSeveringRecipeBuilder> {
  private final RecipeSerializer<? extends SeveringRecipe> serializer;
  private final RecipeFactory recipeFactory;
  private float baseChance = 0.05f;
  private float lootingBonus = 0.01f;

  private SpecialSeveringRecipeBuilder(RecipeSerializer<? extends SeveringRecipe> serializer, RecipeFactory recipeFactory) {
    this.serializer = serializer;
    this.recipeFactory = recipeFactory;
  }

  /** Creates recipes for a special severing serializer. */
  public static SpecialSeveringRecipeBuilder serializer(RecipeSerializer<? extends SeveringRecipe> serializer) {
    if (serializer == TinkerModifiers.playerBeheadingSerializer.get()) {
      return serializer(serializer, PlayerBeheadingRecipe::new);
    }
    if (serializer == TinkerModifiers.snowGolemBeheadingSerializer.get()) {
      return serializer(serializer, SnowGolemBeheadingRecipe::new);
    }
    if (serializer == TinkerModifiers.mooshroomDemushroomingSerializer.get()) {
      return serializer(serializer, MooshroomDemushroomingRecipe::new);
    }
    if (serializer == TinkerModifiers.sheepShearing.get()) {
      return serializer(serializer, SheepShearingRecipe::new);
    }
    throw new IllegalArgumentException("No severing recipe factory registered for " + serializer);
  }

  /** Creates recipes for a custom special severing serializer. */
  public static SpecialSeveringRecipeBuilder serializer(RecipeSerializer<? extends SeveringRecipe> serializer, RecipeFactory recipeFactory) {
    return new SpecialSeveringRecipeBuilder(serializer, recipeFactory);
  }

  @FunctionalInterface
  public interface RecipeFactory {
    SeveringRecipe create(ResourceLocation id, float baseChance, float lootingBonus);
  }

  /** Creates a new builder for the given serializer. */
  public static SpecialSeveringRecipeBuilder serializer(Supplier<? extends RecipeSerializer<? extends SeveringRecipe>> supplier) {
    return serializer(supplier.get());
  }

  /** Doubles the drop chances for this rare mob */
  public SpecialSeveringRecipeBuilder rareMob() {
    baseChance = 0.1f;
    lootingBonus = 0.02f;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer)));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, recipeFactory.create(id, baseChance, lootingBonus), null);
  }
}

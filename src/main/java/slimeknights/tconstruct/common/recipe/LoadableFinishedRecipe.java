package slimeknights.tconstruct.common.recipe;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;

/**
 * Adapter for accepting record-loadable recipes through Minecraft's recipe output API.
 * Recipe serialization is handled by the recipe's registered serializer.
 * TODO: move to Mantle
 */
public record LoadableFinishedRecipe<T extends Recipe<?>>(RecordLoadable<T> loader, T recipe) {
  /** Accepts this recipe through the 1.21 recipe output API. */
  public void save(RecipeOutput output, ResourceLocation id, @Nullable AdvancementHolder advancement) {
    output.accept(id, recipe, advancement);
  }

  /** One-liner to accept a recipe with its optional advancement. */
  public static <T extends Recipe<?>> void save(RecipeOutput output, ResourceLocation id, RecordLoadable<T> loader, T recipe, @Nullable AdvancementHolder advancement) {
    new LoadableFinishedRecipe<>(loader, recipe).save(output, id, advancement);
  }
}

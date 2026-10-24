package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
/** Helper to add custom data to shaped recipe results while preserving their serializer. */
public record CraftingNBTWrapper(RecipeOutput output, CompoundTag nbt) implements RecipeOutput {
  @Override
  public Advancement.Builder advancement() {
    return output.advancement();
  }

  @Override
  public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
    if (!(recipe instanceof ShapedRecipe shaped)) {
      throw new IllegalArgumentException("Custom-data output requires a shaped recipe, got " + recipe.getClass().getName());
    }
    shaped.result.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt.copy()));
    output.accept(id, recipe, advancement, conditions);
  }

  /** Creates a wrapped recipe output, adding the given custom data to result stacks. */
  public static RecipeOutput wrap(RecipeOutput base, CompoundTag nbt) {
    return new CraftingNBTWrapper(base, nbt);
  }
}

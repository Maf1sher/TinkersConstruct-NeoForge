package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;

/**
 * Helper to add custom NBT data to vanilla recipes' results.
 * In 1.21, this wraps RecipeOutput to apply CustomData to recipe results.
 */
public class CraftingNBTWrapper {
  private CraftingNBTWrapper() {}

  /** Creates a wrapped recipe output that applies the given NBT as CustomData to recipe results */
  public static RecipeOutput wrap(RecipeOutput base, CompoundTag nbt) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        // For shaped/shapeless recipes, we can modify the result to include custom data
        // In 1.21, custom NBT goes into DataComponents.CUSTOM_DATA
        Recipe<?> modifiedRecipe = applyCustomData(recipe, nbt);
        base.accept(id, modifiedRecipe, advancement, conditions);
      }

      @Override
      public Advancement.Builder advancement() {
        return base.advancement();
      }
    };
  }

  /** Applies custom NBT data to a recipe's result item stack via CUSTOM_DATA component */
  private static Recipe<?> applyCustomData(Recipe<?> recipe, CompoundTag nbt) {
    if (recipe instanceof ShapedRecipe shaped) {
      ItemStack result = shaped.getResultItem(net.minecraft.core.RegistryAccess.EMPTY).copy();
      applyNbtToStack(result, nbt);
      return new ShapedRecipe(shaped.getGroup(), shaped.category(), shaped.pattern, result, shaped.showNotification());
    } else if (recipe instanceof ShapelessRecipe shapeless) {
      ItemStack result = shapeless.getResultItem(net.minecraft.core.RegistryAccess.EMPTY).copy();
      applyNbtToStack(result, nbt);
      return new ShapelessRecipe(shapeless.getGroup(), shapeless.category(), result, shapeless.getIngredients());
    }
    // For other recipe types, pass through unchanged
    return recipe;
  }

  /** Applies NBT compound tag data to an item stack via CustomData component */
  private static void applyNbtToStack(ItemStack stack, CompoundTag nbt) {
    // Merge the NBT into existing CustomData or create new CustomData
    CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    CompoundTag merged = existing.copyTag();
    // Merge all entries from the provided nbt
    for (String key : nbt.getAllKeys()) {
      merged.put(key, nbt.get(key).copy());
    }
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(merged));

    // Special handling: if nbt contains display.Name, also set CUSTOM_NAME component
    if (nbt.contains("display") && nbt.getCompound("display").contains("Name")) {
      String nameJson = nbt.getCompound("display").getString("Name");
      try {
        net.minecraft.network.chat.Component name = net.minecraft.network.chat.Component.Serializer.fromJson(nameJson, net.minecraft.core.RegistryAccess.EMPTY);
        if (name != null) {
          stack.set(DataComponents.CUSTOM_NAME, name);
        }
      } catch (Exception ignored) {
        // If parsing fails, just keep the custom data
      }
    }
  }
}

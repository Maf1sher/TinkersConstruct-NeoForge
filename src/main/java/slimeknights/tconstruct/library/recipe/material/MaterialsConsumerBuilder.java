package slimeknights.tconstruct.library.recipe.material;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data-generation builder for shaped and shapeless material recipes. */
public class MaterialsConsumerBuilder {
  private final String partSymbols;
  private final int partCount;
  private final List<MaterialVariantId> extraMaterials = new ArrayList<>();

  private MaterialsConsumerBuilder(String partSymbols, int partCount) {
    this.partSymbols = partSymbols;
    this.partCount = partCount;
  }

  /** Creates a shaped recipe builder with the given symbols identifying material parts. */
  public static MaterialsConsumerBuilder shaped(String parts) {
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Parts may not be empty");
    }
    return new MaterialsConsumerBuilder(parts, 0);
  }

  /** Creates an output adapter for shapeless recipes; the first inputs become the parts. */
  public static MaterialsConsumerBuilder shapeless(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be greater than 0");
    }
    return new MaterialsConsumerBuilder("", parts);
  }

  /** Adds an additional material to write to the crafted result. */
  public MaterialsConsumerBuilder material(MaterialVariantId material) {
    extraMaterials.add(material);
    return this;
  }

  /** Creates a shaped recipe builder that retains its symbol-to-ingredient mapping. */
  public ShapedRecipeBuilder shapedRecipe(RecipeCategory category, ItemLike result) {
    if (partCount != 0) {
      throw new IllegalStateException("Shapeless material recipes cannot create a shaped builder");
    }
    return new MaterialShapedRecipeBuilder(category, result, partSymbols, List.copyOf(extraMaterials));
  }

  /** Wraps shapeless recipe output to attach material metadata. */
  public RecipeOutput build(RecipeOutput output) {
    if (partCount == 0) {
      throw new IllegalStateException("Shaped material recipes must use shapedRecipe() to preserve pattern symbols");
    }
    List<MaterialVariantId> materials = List.copyOf(extraMaterials);
    return new RecipeOutput() {
      @Override
      public Advancement.Builder advancement() {
        return output.advancement();
      }

      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        if (!(recipe instanceof ShapelessRecipe shapeless)) {
          throw new IllegalArgumentException("Expected a shapeless material recipe, got " + recipe.getClass().getName());
        }
        output.accept(id, new ShapelessMaterialsRecipe(shapeless, partCount, materials), advancement, conditions);
      }
    };
  }

  private static class MaterialShapedRecipeBuilder extends ShapedRecipeBuilder {
    private final String partSymbols;
    private final List<MaterialVariantId> extraMaterials;
    private final Map<Character,Ingredient> key = new LinkedHashMap<>();
    private final List<String> pattern = new ArrayList<>();

    private MaterialShapedRecipeBuilder(RecipeCategory category, ItemLike result, String partSymbols, List<MaterialVariantId> extraMaterials) {
      super(category, result, 1);
      this.partSymbols = partSymbols;
      this.extraMaterials = extraMaterials;
    }

    @Override
    public ShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
      super.define(symbol, ingredient);
      key.put(symbol, ingredient);
      return this;
    }

    @Override
    public ShapedRecipeBuilder pattern(String row) {
      super.pattern(row);
      pattern.add(row);
      return this;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
      RecipeOutput materialOutput = new RecipeOutput() {
        @Override
        public Advancement.Builder advancement() {
          return output.advancement();
        }

        @Override
        public void accept(ResourceLocation recipeId, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
          if (!(recipe instanceof ShapedRecipe shaped)) {
            throw new IllegalArgumentException("Expected a shaped material recipe, got " + recipe.getClass().getName());
          }
          ShapedRecipePattern.Data patternData = new ShapedRecipePattern.Data(new LinkedHashMap<>(key), List.copyOf(pattern));
          output.accept(recipeId, new ShapedMaterialsRecipe(shaped, patternData, partSymbols, extraMaterials), advancement, conditions);
        }
      };
      super.save(materialOutput, id);
    }
  }
}

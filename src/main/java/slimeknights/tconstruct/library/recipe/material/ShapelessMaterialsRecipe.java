package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.mantle.data.codec.LoadableCodec;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.List;

/**
 * Shapeless recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  /** Number of parts to match */
  @Getter
  private final int partCount;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  public ShapelessMaterialsRecipe(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    this(group, category, result, ingredients, partCount, extraMaterials);
  }

  public ShapelessMaterialsRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(group, category, result, ingredients);
    this.partCount = partCount;
    this.extraMaterials = List.copyOf(extraMaterials);
  }

  public ShapelessMaterialsRecipe(ShapelessRecipe recipe, int partCount, List<MaterialVariantId> extraMaterials) {
    this(recipe.getGroup(), recipe.category(), recipe.result, recipe.getIngredients(), partCount, extraMaterials);
  }

  @Override
  public List<Ingredient> getParts() {
    return getIngredients();
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registries) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory, registries), inventory, getIngredients(), partCount, false, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements RecipeSerializer<ShapelessMaterialsRecipe> {
    static final Loadable<List<MaterialVariantId>> EXTRA_MATERIALS = ShapedMaterialsRecipe.Serializer.EXTRA_MATERIALS;
    private static final Codec<List<MaterialVariantId>> EXTRA_MATERIALS_CODEC = new LoadableCodec<>(EXTRA_MATERIALS);
    private static final Codec<NonNullList<Ingredient>> INGREDIENTS_CODEC = Ingredient.CODEC_NONEMPTY.listOf().flatXmap(ingredients -> {
      if (ingredients.isEmpty()) {
        return DataResult.error(() -> "No ingredients for shapeless material recipe");
      }
      if (ingredients.size() > ShapedRecipePattern.getMaxWidth() * ShapedRecipePattern.getMaxHeight()) {
        return DataResult.error(() -> "Too many ingredients for shapeless material recipe");
      }
      return DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new)));
    }, DataResult::success);
    private static final MapCodec<JsonData> DATA_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      Codec.STRING.optionalFieldOf("group", "").forGetter(JsonData::group),
      CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(JsonData::category),
      ItemStack.STRICT_CODEC.fieldOf("result").forGetter(JsonData::result),
      INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(JsonData::ingredients),
      Codec.INT.fieldOf("parts").forGetter(JsonData::partCount),
      EXTRA_MATERIALS_CODEC.optionalFieldOf("extra_materials", List.of()).forGetter(JsonData::extraMaterials)
    ).apply(instance, JsonData::new));
    public static final MapCodec<ShapelessMaterialsRecipe> CODEC = DATA_CODEC.flatXmap(JsonData::toRecipe, JsonData::fromRecipe);
    public static final StreamCodec<RegistryFriendlyByteBuf,ShapelessMaterialsRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    @Override
    public MapCodec<ShapelessMaterialsRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ShapelessMaterialsRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static ShapelessMaterialsRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      ShapelessRecipe recipe = ShapelessRecipe.Serializer.STREAM_CODEC.decode(buffer);
      int parts = buffer.readUnsignedByte();
      List<MaterialVariantId> extraMaterials = EXTRA_MATERIALS.decode(buffer);
      return new ShapelessMaterialsRecipe(recipe, parts, extraMaterials);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapelessMaterialsRecipe recipe) {
      ShapelessRecipe.Serializer.STREAM_CODEC.encode(buffer, recipe);
      buffer.writeByte(recipe.partCount);
      EXTRA_MATERIALS.encode(buffer, recipe.extraMaterials);
    }

    private record JsonData(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
      private DataResult<ShapelessMaterialsRecipe> toRecipe() {
        if (partCount < 1 || partCount > ingredients.size()) {
          return DataResult.error(() -> "Parts must be between 1 and the number of ingredients " + ingredients.size());
        }
        return DataResult.success(new ShapelessMaterialsRecipe(group, category, result, ingredients, partCount, extraMaterials));
      }

      private static DataResult<JsonData> fromRecipe(ShapelessMaterialsRecipe recipe) {
        if (recipe.partCount < 1 || recipe.partCount > recipe.getIngredients().size()) {
          return DataResult.error(() -> "Parts must be between 1 and the number of ingredients " + recipe.getIngredients().size());
        }
        return DataResult.success(new JsonData(recipe.getGroup(), recipe.category(), recipe.result, recipe.getIngredients(), recipe.partCount, recipe.extraMaterials));
      }
    }
  }
}

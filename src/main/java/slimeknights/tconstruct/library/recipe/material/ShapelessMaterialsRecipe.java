package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.ArrayList;
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

  public ShapelessMaterialsRecipe(ShapelessRecipe recipe, int partCount, List<MaterialVariantId> extraMaterials) {
    super(recipe.getGroup(), recipe.category(), recipe.result, recipe.getIngredients());
    this.partCount = partCount;
    this.extraMaterials = extraMaterials;
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
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registryAccess) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory, registryAccess), inventory, getIngredients(), partCount, false, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapelessMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements RecipeSerializer<ShapelessMaterialsRecipe> {
    /** Codec for MaterialVariantId */
    private static final Codec<MaterialVariantId> MATERIAL_VARIANT_CODEC = Codec.STRING.comapFlatMap(
      s -> {
        MaterialVariantId id = MaterialVariantId.tryParse(s);
        return id != null ? DataResult.success(id) : DataResult.error(() -> "Invalid material variant ID: " + s);
      },
      MaterialVariantId::toString
    );
    private static final Codec<List<MaterialVariantId>> EXTRA_MATERIALS_CODEC = MATERIAL_VARIANT_CODEC.listOf();

    /** StreamCodec for list of MaterialVariantId */
    private static final StreamCodec<RegistryFriendlyByteBuf, List<MaterialVariantId>> EXTRA_MATERIALS_STREAM_CODEC =
      StreamCodec.of(
        (buf, list) -> {
          buf.writeVarInt(list.size());
          for (MaterialVariantId id : list) {
            id.toNetwork(buf);
          }
        },
        buf -> {
          int size = buf.readVarInt();
          ArrayList<MaterialVariantId> list = new ArrayList<>(size);
          for (int i = 0; i < size; i++) {
            list.add(MaterialVariantId.fromNetwork(buf));
          }
          return List.copyOf(list);
        }
      );

    public static final MapCodec<ShapelessMaterialsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      RecipeSerializer.SHAPELESS_RECIPE.codec().forGetter(r -> (ShapelessRecipe) r),
      Codec.INT.fieldOf("parts").forGetter(r -> r.partCount),
      EXTRA_MATERIALS_CODEC.optionalFieldOf("extra_materials", List.of()).forGetter(r -> r.extraMaterials)
    ).apply(instance, (base, parts, extra) -> new ShapelessMaterialsRecipe(base, parts, extra)));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
      Serializer::toNetwork, Serializer::fromNetwork
    );

    @Override
    public MapCodec<ShapelessMaterialsRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapelessMaterialsRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static ShapelessMaterialsRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      ShapelessRecipe base = ShapelessRecipe.Serializer.STREAM_CODEC.decode(buffer);
      int parts = buffer.readVarInt();
      List<MaterialVariantId> extraMaterials = EXTRA_MATERIALS_STREAM_CODEC.decode(buffer);
      return new ShapelessMaterialsRecipe(base, parts, extraMaterials);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapelessMaterialsRecipe recipe) {
      ShapelessRecipe.Serializer.STREAM_CODEC.encode(buffer, recipe);
      buffer.writeVarInt(recipe.partCount);
      EXTRA_MATERIALS_STREAM_CODEC.encode(buffer, recipe.extraMaterials);
    }
  }
}

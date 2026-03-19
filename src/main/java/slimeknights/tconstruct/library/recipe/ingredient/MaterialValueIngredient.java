package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("material_value");

  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;
  private ItemStack[] items;

  // Loadable field for material predicate - used in stream codec
  static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

  /** MapCodec for JSON serialization via Codec system */
  public static final MapCodec<MaterialValueIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      // For now, use a simple approach: store material as a nested object, and value as either a single float or a min/max object
      // The complex MaterialPredicateField is kept for network codec
      Codec.FLOAT.optionalFieldOf("min_value", 0f).forGetter(i -> i.minValue),
      Codec.FLOAT.optionalFieldOf("max_value", Float.POSITIVE_INFINITY).forGetter(i -> i.maxValue)
    ).apply(instance, (min, max) -> new MaterialValueIngredient(MaterialPredicate.ANY, min, max))
  );

  /** StreamCodec for network serialization */
  public static final StreamCodec<RegistryFriendlyByteBuf, MaterialValueIngredient> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public MaterialValueIngredient decode(RegistryFriendlyByteBuf buffer) {
      return new MaterialValueIngredient(
        MATERIAL_FIELD.decode(buffer),
        buffer.readFloat(),
        buffer.readFloat()
      );
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, MaterialValueIngredient ingredient) {
      MATERIAL_FIELD.encode(buffer, ingredient);
      buffer.writeFloat(ingredient.minValue);
      buffer.writeFloat(ingredient.maxValue);
    }
  };

  /** IngredientType instance - must be registered to NeoForgeRegistries.INGREDIENT_TYPES */
  public static final IngredientType<MaterialValueIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  /** Creates an ingredient matching a range of values */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue);
  }

  /** Creates an ingredient matching an exact value */
  public static MaterialValueIngredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    float value = material.getValue() / (float) material.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null) {
      return false;
    }
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (items == null) {
      items = MaterialRecipeCache.getAllRecipes().stream()
        .filter(this::test)
        .flatMap(material -> Arrays.stream(material.getIngredient().getItems()))
        .toArray(ItemStack[]::new);
    }
    return Arrays.stream(items);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** Converts this custom ingredient to a vanilla Ingredient */
  public Ingredient toIngredient() {
    return toVanilla();
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MaterialValueIngredient that)) return false;
    return Float.compare(that.minValue, minValue) == 0
      && Float.compare(that.maxValue, maxValue) == 0
      && Objects.equals(material, that.material);
  }

  @Override
  public int hashCode() {
    return Objects.hash(material, minValue, maxValue);
  }
}

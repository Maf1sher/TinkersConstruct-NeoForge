package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.JsonMapCodec;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Custom ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(exclude = "items")
public class MaterialValueIngredient implements ICustomIngredient {
  private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("material_value");

  /** Codec keeping the legacy JSON layout: {@code {"type": "tconstruct:material_value", "material": ..., "value": ...}} */
  public static final MapCodec<MaterialValueIngredient> CODEC = new JsonMapCodec<>() {
    @Override
    protected MaterialValueIngredient deserialize(JsonObject json, DynamicOps<?> ops) {
      IJsonPredicate<MaterialVariantId> material = MATERIAL_FIELD.get(json, TypedMap.EMPTY);
      JsonElement value = json.get("value");
      if (value == null) {
        throw new JsonSyntaxException("Missing value field on material value ingredient");
      }
      float minValue, maxValue;
      if (value.isJsonPrimitive()) {
        minValue = maxValue = value.getAsJsonPrimitive().getAsFloat();
      } else {
        JsonObject object = GsonHelper.convertToJsonObject(value, "value");
        minValue = GsonHelper.getAsFloat(object, "min", 0);
        maxValue = GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY);
      }
      return new MaterialValueIngredient(material, minValue, maxValue);
    }

    @Override
    protected JsonObject serialize(MaterialValueIngredient ingredient, DynamicOps<?> ops) {
      JsonObject json = new JsonObject();
      MATERIAL_FIELD.serializeInto(ingredient, json, TypedMap.EMPTY);
      if (ingredient.minValue == ingredient.maxValue) {
        json.addProperty("value", ingredient.minValue);
      } else {
        JsonObject value = new JsonObject();
        if (ingredient.minValue > 0) {
          value.addProperty("min", ingredient.minValue);
        }
        if (Float.isFinite(ingredient.maxValue)) {
          value.addProperty("max", ingredient.maxValue);
        }
        json.add("value", value);
      }
      return json;
    }
  };

  /** Network codec: material predicate followed by the min and max values, matching the old byte layout */
  public static final StreamCodec<RegistryFriendlyByteBuf, MaterialValueIngredient> STREAM_CODEC = StreamCodec.of(
    (buffer, ingredient) -> {
      MaterialPredicate.LOADER.encode(buffer, ingredient.material, TypedMap.EMPTY);
      buffer.writeFloat(ingredient.minValue);
      buffer.writeFloat(ingredient.maxValue);
    },
    buffer -> new MaterialValueIngredient(MaterialPredicate.LOADER.decode(buffer, TypedMap.EMPTY), buffer.readFloat(), buffer.readFloat()));

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final IngredientType<MaterialValueIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final IJsonPredicate<MaterialVariantId> material;
  /** Minimum material value to match */
  private final float minValue;
  /** Maximum material value to match */
  private final float maxValue;
  @Nullable
  private ItemStack[] items;

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
      items = MaterialRecipeCache.getSortedRecipes().stream()
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
}

package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.JsonMapCodec;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Custom ingredient wrapping a vanilla ingredient to support matching by materials and displaying material variants.
 * JSON layout is the legacy Forge one: vanilla single-value ingredients stay flat, anything else nests under {@code "match"}.
 */
public class MaterialIngredient extends NestedIngredient {
  private static final LoadableField<IJsonPredicate<MaterialVariantId>,MaterialIngredient> MATERIAL_FIELD = new MaterialPredicateField<>("material", i -> i.material);

  /** Ingredient type, replace for the old Forge serializer; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("material");

  /** Codec keeping the legacy JSON layout: {@code {"type": "tconstruct:material", ..., "material": ...}} */
  public static final MapCodec<MaterialIngredient> CODEC = new JsonMapCodec<>() {
    @Override
    protected MaterialIngredient deserialize(JsonObject json, DynamicOps<?> ops) {
      Ingredient ingredient = decodeNestedJson(json);
      IJsonPredicate<MaterialVariantId> material = MATERIAL_FIELD.get(json, TypedMap.EMPTY);
      // deprecated tag field
      if (json.has("material_tag")) {
        TConstruct.LOG.warn("Using deprecated tag field on material ingredient");
        IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "material_tag"));
        if (material == MaterialPredicate.ANY) {
          material = tagPredicate;
        } else {
          material = MaterialPredicate.and(material, tagPredicate);
        }
      }
      return new MaterialIngredient(ingredient, material);
    }

    @Override
    protected JsonObject serialize(MaterialIngredient ingredient, DynamicOps<?> ops) {
      JsonObject result = new JsonObject();
      encodeNestedJson(ingredient.nested, result);
      MATERIAL_FIELD.serializeInto(ingredient, result, TypedMap.EMPTY);
      return result;
    }
  };

  /** Network codec: nested ingredient followed by the material predicate, matching the old byte layout */
  public static final StreamCodec<RegistryFriendlyByteBuf, MaterialIngredient> STREAM_CODEC = StreamCodec.composite(
    Ingredient.CONTENTS_STREAM_CODEC, MaterialIngredient::getNested,
    new StreamableCodec<IJsonPredicate<MaterialVariantId>>(MaterialPredicate.LOADER), MaterialIngredient::getMaterial,
    MaterialIngredient::new);

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final IngredientType<MaterialIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final IJsonPredicate<MaterialVariantId> material;
  @Nullable
  private ItemStack[] materialStacks;

  protected MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    super(nested);
    this.material = material;
  }

  /** @deprecated use {@link #MaterialIngredient(Ingredient, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  @SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
  protected MaterialIngredient(Ingredient nested, MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    this(nested, makePredicate(material, tag));
  }

  /** Converts the legacy material and tag into a predicate */
  private static IJsonPredicate<MaterialVariantId> makePredicate(MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    // UNKNOWN is the legacy way to express any material
    IJsonPredicate<MaterialVariantId> predicate = material.equals(IMaterial.UNKNOWN.getIdentifier()) ? MaterialPredicate.ANY : MaterialPredicate.variant(material);
    if (tag != null) {
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(tag);
      if (predicate == MaterialPredicate.ANY) {
        predicate = tagPredicate;
      } else {
        predicate = MaterialPredicate.and(predicate, tagPredicate);
      }
    }
    return predicate;
  }

  /** @return the material filter for this ingredient */
  public IJsonPredicate<MaterialVariantId> getMaterial() {
    return material;
  }

  /** Creates an ingredient matching the given materials */
  public static MaterialIngredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(ingredient, material);
  }

  /** Creates an ingredient matching the given materials */
  public static MaterialIngredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(Ingredient.of(item), material);
  }

  /** Creates an ingredient matching a specific material */
  public static MaterialIngredient of(Ingredient ingredient) {
    return new MaterialIngredient(ingredient, MaterialPredicate.ANY);
  }

  /** Creates an ingredient matching a single material */
  public static MaterialIngredient of(Ingredient ingredient, MaterialVariantId material) {
    return of(ingredient, MaterialPredicate.variant(material));
  }

  /** Creates an ingredient matching a material tag */
  public static MaterialIngredient of(Ingredient ingredient, TagKey<IMaterial> tag) {
    return of(ingredient, MaterialPredicate.tag(tag));
  }

  /**
   * Creates a new instance from an item with a fixed material
   * @param item      Material item
   * @param material  Material ID
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item, MaterialVariantId material) {
    return of(Ingredient.of(item), material);
  }

  /**
   * Creates a new instance from an item with a tagged material
   * @param item      Material item
   * @param tag   Material tag
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item, TagKey<IMaterial> tag) {
    return of(Ingredient.of(item), tag);
  }

  /**
   * Creates a new ingredient matching any material from items
   * @param item  Material item
   * @return  Material ingredient instance
   */
  public static MaterialIngredient of(ItemLike item) {
    return of(Ingredient.of(item));
  }

  /**
   * Creates a new ingredient from a tag
   * @param tag       Tag instance
   * @param material  Material value
   * @return  Material with tag
   */
  public static MaterialIngredient of(TagKey<Item> tag, MaterialVariantId material) {
    return of(Ingredient.of(tag), material);
  }

  /**
   * Creates a new ingredient matching any material from a tag
   * @param tag       Tag instance
   * @return  Material with tag
   */
  public static MaterialIngredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    // check nested first, should be faster
    if (stack == null || stack.isEmpty() || !nested.test(stack)) {
      return false;
    }
    // no need to read material data if the material is the any predicate
    if (material != MaterialPredicate.ANY) {
      return material.matches(IMaterialItem.getMaterialFromStack(stack));
    }
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (materialStacks == null) {
      if (!MaterialRegistry.isFullyLoaded()) {
        return Arrays.stream(nested.getItems());
      }
      // no material? apply all materials for variants
      Stream<ItemStack> items = Arrays.stream(nested.getItems());
      // find all materials matching the filter; note this only shows craftable material variants
      items = items.flatMap(stack -> MaterialRecipeCache.getAllVariants().stream()
        .filter(material::matches)
        .map(mat -> IMaterialItem.withMaterial(stack, mat))
        .filter(variant -> variant.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)));
      materialStacks = items.distinct().toArray(ItemStack[]::new);
    }
    return Arrays.stream(materialStacks);
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    MaterialIngredient that = (MaterialIngredient)other;
    return nested.equals(that.nested) && material.equals(that.material);
  }

  @Override
  public int hashCode() {
    return nested.hashCode() * 31 + material.hashCode();
  }
}

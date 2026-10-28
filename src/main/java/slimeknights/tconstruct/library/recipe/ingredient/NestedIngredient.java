package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Custom ingredient that contains another ingredient nested inside. Base class for implementations which
 * used to wrap another ingredient under the legacy Forge {@code "match"} key.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class NestedIngredient implements ICustomIngredient {
  protected final Ingredient nested;

  /** @return the wrapped ingredient, for codec composition */
  public Ingredient getNested() {
    return nested;
  }


  /* Defer to nested */

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return nested.test(stack);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(nested.getItems());
  }

  @Override
  public boolean isSimple() {
    return nested.isSimple();
  }


  /* Legacy JSON helpers for subclasses */

  /**
   * Serializes the nested ingredient into the given JSON object. Vanilla ingredients with a single value are
   * serialized flat ({@code "item"} / {@code "tag"} keys) to keep the legacy Forge JSON layout; anything else
   * is serialized under {@code "match"}.
   */
  protected static void encodeNestedJson(Ingredient nested, JsonObject json) {
    if (!nested.isCustom()) {
      Ingredient.Value[] values = nested.getValues();
      if (values.length == 1) {
        encodeVanillaValue(values[0], json);
        return;
      }
    }
    // multi-value vanilla or custom ingredient goes under "match"
    json.add("match", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, nested).getOrThrow(msg -> new JsonSyntaxException(msg)));
  }

  /** Serializes a single vanilla value flat, matching the old Forge vanilla ingredient JSON */
  private static void encodeVanillaValue(Ingredient.Value value, JsonObject json) {
    if (value instanceof Ingredient.ItemValue itemValue) {
      ItemStack stack = itemValue.item();
      json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
      if (stack.getCount() != 1) {
        json.addProperty("count", stack.getCount());
      }
      return;
    }
    if (value instanceof Ingredient.TagValue tagValue) {
      json.addProperty("tag", tagValue.tag().location().toString());
      return;
    }
    throw new IllegalArgumentException("Unexpected ingredient value type: " + value.getClass());
  }

  /**
   * Decodes the nested ingredient from the given JSON object: if it holds {@code "match"}, uses that;
   * otherwise parses {@code "item"} / {@code "tag"} fields directly. This reproduces the old Forge parser.
   */
  protected static Ingredient decodeNestedJson(JsonObject json) {
    if (json.has("match")) {
      return decodeIngredientJson(json.get("match"), "match");
    }
    // without "match", the object itself must be a vanilla value ingredient
    return Ingredient.fromValues(Stream.of(valueFromJson(json)));
  }

  /**
   * Legacy Forge {@code CraftingHelper.getIngredient} equivalent: accepts an item/tag string, an array of
   * such values, an object with a {@code type} key (any ingredient), or a vanilla item/tag object.
   */
  protected static Ingredient decodeIngredientJson(JsonElement element, String key) {
    if (element.isJsonPrimitive()) {
      // raw string form: item, or tag if it starts with #
      String id = GsonHelper.convertToString(element, key);
      if (id.startsWith("#")) {
        ResourceLocation location = ResourceLocation.parse(id.substring(1));
        return Ingredient.fromValues(Stream.of(new Ingredient.TagValue(TagKey.create(Registries.ITEM, location))));
      }
      return Ingredient.fromValues(Stream.of(new Ingredient.ItemValue(new ItemStack(Loadables.ITEM.convert(element, key, TypedMap.EMPTY)))));
    }
    if (element.isJsonArray()) {
      // array form: each entry is a vanilla style value object
      JsonArray array = element.getAsJsonArray();
      Ingredient.Value[] values = new Ingredient.Value[array.size()];
      for (int i = 0; i < array.size(); i++) {
        values[i] = valueFromJson(GsonHelper.convertToJsonObject(array.get(i), key));
      }
      return Ingredient.fromValues(Arrays.stream(values));
    }
    JsonObject object = GsonHelper.convertToJsonObject(element, key);
    if (object.has("type")) {
      // any ingredient type
      return Ingredient.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(msg -> new JsonSyntaxException(msg));
    }
    return Ingredient.fromValues(Stream.of(valueFromJson(object)));
  }

  /** Decodes a stack from an object with {@code item} (string or stack JSON) and optional {@code count} fields, matching old Forge stack parsing */
  private static ItemStack decodeStackJson(JsonObject json) {
    JsonElement itemElement = json.get("item");
    if (itemElement != null && itemElement.isJsonPrimitive()) {
      int count = GsonHelper.getAsInt(json, "count", 1);
      return new ItemStack(Loadables.ITEM.convert(itemElement, "item", TypedMap.EMPTY), count);
    }
    return ItemStack.CODEC.parse(JsonOps.INSTANCE, itemElement).getOrThrow(msg -> new JsonSyntaxException(msg));
  }

  /** Creates a vanilla ingredient value from {@code item} / {@code tag} fields on the given object */
  private static Ingredient.Value valueFromJson(JsonObject json) {
    if (json.has("item")) {
      return new Ingredient.ItemValue(decodeStackJson(json));
    }
    if (json.has("tag")) {
      TagKey<Item> tag = Loadables.ITEM_TAG.getIfPresent(json, "tag");
      if (tag == null) {
        throw new JsonSyntaxException("Invalid tag for ingredient: " + json.get("tag"));
      }
      return new Ingredient.TagValue(tag);
    }
    throw new JsonSyntaxException("Expected " + json + " to be an item or tag ingredient");
  }
}

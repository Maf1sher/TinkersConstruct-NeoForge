package slimeknights.tconstruct.library.json.field;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.tconstruct.library.json.LegacyRecipeJson;
import slimeknights.mantle.util.typed.TypedMap;

/** Compatibility wrapper for ingredient fields that still encounter legacy array shorthand. */
public record CompatIngredientField<P>(LoadableField<Ingredient,P> field) implements LoadableField<Ingredient,P> {
  @Override
  public String key() {
    return field.key();
  }

  @Override
  public Ingredient get(JsonObject json, String key, TypedMap context) {
    if (!json.has(key)) {
      return field.get(json, key, context);
    }
    JsonObject normalized = json.deepCopy();
    normalized.add(key, LegacyRecipeJson.normalizeIngredient(normalized.get(key)));
    try {
      return field.get(normalized, key, context);
    } catch (RuntimeException e) {
      if (!isMissingRegistryError(e)) {
        throw e;
      }
      // Registry-less callers (for example recipe viewers) cannot decode custom ingredient types.
      // Fall back to a vanilla approximation by stripping custom wrappers.
      JsonElement vanilla = toRegistrylessVanilla(normalized.get(key));
      return Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, vanilla).getOrThrow();
    }
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    field.serialize(parent, json);
    String key = key();
    if (json.has(key)) {
      json.add(key, LegacyRecipeJson.normalizeIngredient(json.get(key)));
    }
  }

  @Override
  public Ingredient decode(FriendlyByteBuf buffer, TypedMap context) {
    return field.decode(buffer, context);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, P parent) {
    field.encode(buffer, parent);
  }

  private static boolean isMissingRegistryError(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && message.contains("without registry")) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  /** Converts custom ingredient payloads into vanilla-compatible item/tag ingredient JSON. */
  private static JsonElement toRegistrylessVanilla(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return element;
    }

    if (element.isJsonArray()) {
      JsonArray array = new JsonArray();
      for (JsonElement child : element.getAsJsonArray()) {
        appendFlattened(array, toRegistrylessVanilla(child));
      }
      return array;
    }

    if (!element.isJsonObject()) {
      return element;
    }

    JsonObject object = element.getAsJsonObject();

    if (object.has("item") || object.has("tag")) {
      JsonObject vanilla = new JsonObject();
      if (object.has("item")) {
        vanilla.add("item", object.get("item"));
      }
      if (object.has("tag")) {
        vanilla.add("tag", object.get("tag"));
      }
      return vanilla;
    }

    if (object.has("items")) {
      JsonElement items = object.get("items");
      JsonArray array = new JsonArray();
      if (items.isJsonArray()) {
        for (JsonElement item : items.getAsJsonArray()) {
          array.add(wrapItem(item));
        }
      } else {
        array.add(wrapItem(items));
      }
      return array;
    }

    if (object.has("ingredients") && object.get("ingredients").isJsonArray()) {
      JsonArray array = new JsonArray();
      for (JsonElement child : object.getAsJsonArray("ingredients")) {
        appendFlattened(array, toRegistrylessVanilla(child));
      }
      return array;
    }

    if (object.has("children") && object.get("children").isJsonArray()) {
      JsonArray array = new JsonArray();
      for (JsonElement child : object.getAsJsonArray("children")) {
        appendFlattened(array, toRegistrylessVanilla(child));
      }
      return array;
    }

    if (object.has("base")) {
      return toRegistrylessVanilla(object.get("base"));
    }

    return element;
  }

  private static JsonElement wrapItem(JsonElement item) {
    if (item != null && item.isJsonObject()) {
      return toRegistrylessVanilla(item);
    }
    JsonObject wrapped = new JsonObject();
    wrapped.add("item", item);
    return wrapped;
  }

  private static void appendFlattened(JsonArray target, JsonElement element) {
    if (element != null && element.isJsonArray()) {
      for (JsonElement child : element.getAsJsonArray()) {
        target.add(child);
      }
      return;
    }
    target.add(element);
  }
}
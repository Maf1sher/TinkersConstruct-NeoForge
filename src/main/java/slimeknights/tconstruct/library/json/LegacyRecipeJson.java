package slimeknights.tconstruct.library.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Helpers for normalizing legacy recipe payloads before Mantle loadables parse them. */
public final class LegacyRecipeJson {
  private LegacyRecipeJson() {}

  /** Converts legacy ingredient aliases and shorthand into forms understood by current ingredient codecs. */
  public static JsonElement normalizeIngredient(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return element;
    }
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      JsonArray ingredients = new JsonArray();
      for (JsonElement child : array) {
        ingredients.add(normalizeIngredient(child));
      }
      return ingredients;
    }
    if (!element.isJsonObject()) {
      return element;
    }

    JsonObject object = element.getAsJsonObject().deepCopy();
    renameIdToItem(object);
    normalizeItemsArray(object);
    normalizeChild(object, "ingredient");
    normalizeChild(object, "base");
    normalizeChild(object, "subtracted");
    normalizeArrayChildren(object, "ingredients");
    normalizeArrayChildren(object, "children");
    return object;
  }

  /** Converts legacy stack outputs using {@code id} into the current {@code item} form. */
  public static JsonElement normalizeItemOutput(JsonElement element) {
    if (element == null || element.isJsonNull() || !element.isJsonObject()) {
      return element;
    }
    JsonObject object = element.getAsJsonObject().deepCopy();
    renameIdToItem(object);
    return object;
  }

  /** Converts deprecated fluid key {@code name} into the current {@code fluid} field. */
  public static JsonElement normalizeFluid(JsonElement element) {
    if (element == null || element.isJsonNull() || !element.isJsonObject()) {
      return element;
    }
    JsonObject object = element.getAsJsonObject().deepCopy();
    if (!object.has("fluid") && !object.has("tag") && object.has("name")) {
      object.add("fluid", object.remove("name"));
    }
    return object;
  }

  private static void renameIdToItem(JsonObject object) {
    if (!object.has("item") && !object.has("tag") && object.has("id")) {
      object.add("item", object.remove("id"));
    }
  }

  private static void normalizeItemsArray(JsonObject object) {
    if (!object.has("items") || object.get("items").isJsonArray()) {
      return;
    }
    JsonArray items = new JsonArray();
    items.add(object.get("items"));
    object.add("items", items);
  }

  private static void normalizeChild(JsonObject object, String key) {
    if (object.has(key)) {
      object.add(key, normalizeObjectIngredient(object.get(key)));
    }
  }

  private static void normalizeArrayChildren(JsonObject object, String key) {
    if (!object.has(key) || !object.get(key).isJsonArray()) {
      return;
    }

    JsonArray normalized = new JsonArray();
    for (JsonElement child : object.getAsJsonArray(key)) {
      normalized.add(normalizeObjectIngredient(child));
    }
    object.add(key, normalized);
  }

  /** In object-only contexts (like intersection children), legacy arrays mean an OR ingredient. */
  private static JsonElement normalizeObjectIngredient(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return element;
    }
    if (element.isJsonArray()) {
      JsonArray ingredients = new JsonArray();
      for (JsonElement child : element.getAsJsonArray()) {
        ingredients.add(normalizeObjectIngredient(child));
      }
      JsonObject compound = new JsonObject();
      compound.addProperty("type", "neoforge:compound");
      compound.add("ingredients", ingredients);
      return compound;
    }
    return normalizeIngredient(element);
  }
}
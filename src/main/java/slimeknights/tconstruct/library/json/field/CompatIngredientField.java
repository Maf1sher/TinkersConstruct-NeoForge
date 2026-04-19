package slimeknights.tconstruct.library.json.field;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.field.LoadableField;
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
    normalized.add(key, normalizeIngredient(normalized.get(key)));
    return field.get(normalized, key, context);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    field.serialize(parent, json);
    String key = key();
    if (json.has(key)) {
      json.add(key, normalizeIngredient(json.get(key)));
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

  /** Converts legacy ingredient arrays into explicit neoforge compound ingredients. */
  private static JsonElement normalizeIngredient(JsonElement element) {
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      JsonArray ingredients = new JsonArray();
      for (JsonElement child : array) {
        ingredients.add(normalizeIngredient(child));
      }
      JsonObject compound = new JsonObject();
      compound.addProperty("type", "neoforge:compound");
      compound.add("ingredients", ingredients);
      return compound;
    }
    if (!element.isJsonObject()) {
      return element;
    }

    JsonObject object = element.getAsJsonObject().deepCopy();
    normalizeChild(object, "ingredient");
    normalizeChild(object, "base");
    normalizeChild(object, "subtracted");
    normalizeArrayChildren(object, "ingredients");
    normalizeArrayChildren(object, "children");
    return object;
  }

  private static void normalizeChild(JsonObject object, String key) {
    if (object.has(key)) {
      object.add(key, normalizeIngredient(object.get(key)));
    }
  }

  private static void normalizeArrayChildren(JsonObject object, String key) {
    if (!object.has(key) || !object.get(key).isJsonArray()) {
      return;
    }

    JsonArray normalized = new JsonArray();
    for (JsonElement child : object.getAsJsonArray(key)) {
      normalized.add(normalizeIngredient(child));
    }
    object.add(key, normalized);
  }
}
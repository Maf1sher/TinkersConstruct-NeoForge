package slimeknights.tconstruct.library.json.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.json.LegacyRecipeJson;
import slimeknights.mantle.util.typed.TypedMap;

/** Compatibility wrapper for item output fields that still encounter legacy stack aliases. */
public record CompatItemOutputField<P>(LoadableField<ItemOutput,P> field) implements LoadableField<ItemOutput,P> {
  @Override
  public String key() {
    return field.key();
  }

  @Override
  public ItemOutput get(JsonObject json, String key, TypedMap context) {
    if (!json.has(key)) {
      return field.get(json, key, context);
    }
    JsonObject normalized = json.deepCopy();
    normalized.add(key, LegacyRecipeJson.normalizeItemOutput(normalized.get(key)));
    return field.get(normalized, key, context);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    field.serialize(parent, json);
    String key = key();
    if (json.has(key)) {
      json.add(key, LegacyRecipeJson.normalizeItemOutput(json.get(key)));
    }
  }

  @Override
  public ItemOutput decode(FriendlyByteBuf buffer, TypedMap context) {
    return field.decode(buffer, context);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, P parent) {
    field.encode(buffer, parent);
  }
}
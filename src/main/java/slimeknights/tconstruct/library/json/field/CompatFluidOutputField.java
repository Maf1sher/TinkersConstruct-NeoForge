package slimeknights.tconstruct.library.json.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.json.LegacyRecipeJson;

/** Compatibility wrapper for fluid output fields using deprecated fluid keys. */
public record CompatFluidOutputField<P>(LoadableField<FluidOutput,P> field) implements LoadableField<FluidOutput,P> {
  @Override
  public String key() {
    return field.key();
  }

  @Override
  public FluidOutput get(JsonObject json, String key, TypedMap context) {
    if (!json.has(key)) {
      return field.get(json, key, context);
    }
    JsonObject normalized = json.deepCopy();
    normalized.add(key, LegacyRecipeJson.normalizeFluid(normalized.get(key)));
    return field.get(normalized, key, context);
  }

  @Override
  public void serialize(P parent, JsonObject json) {
    field.serialize(parent, json);
    String key = key();
    if (json.has(key)) {
      json.add(key, LegacyRecipeJson.normalizeFluid(json.get(key)));
    }
  }

  @Override
  public FluidOutput decode(FriendlyByteBuf buffer, TypedMap context) {
    return field.decode(buffer, context);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, P parent) {
    field.encode(buffer, parent);
  }
}
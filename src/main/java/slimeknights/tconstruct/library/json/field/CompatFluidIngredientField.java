package slimeknights.tconstruct.library.json.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.json.LegacyRecipeJson;

/** Compatibility wrapper for fluid ingredient fields using deprecated fluid keys. */
public record CompatFluidIngredientField<P>(LoadableField<FluidIngredient,P> field) implements LoadableField<FluidIngredient,P> {
  @Override
  public String key() {
    return field.key();
  }

  @Override
  public FluidIngredient get(JsonObject json, String key, TypedMap context) {
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
  public FluidIngredient decode(FriendlyByteBuf buffer, TypedMap context) {
    return field.decode(buffer, context);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, P parent) {
    field.encode(buffer, parent);
  }
}
package slimeknights.tconstruct.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.mantle.data.loadable.field.ContextKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Passes the recipe ID to the recipe codec. Vanilla 1.21.1 still keys the recipe JSON by ID, but
 * {@link RecipeManager#apply} does not thread that ID into the codec context, so Tinkers' loadable recipes
 * cannot fetch {@link ContextKey#ID}.
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
  /** Temporary JSON key used to carry the file ID into the codec decode call */
  private static final String RECIPE_ID_KEY = "tconstruct:recipe_id";

  /** Adds the recipe ID as a temporary field to each recipe JSON before decoding */
  @ModifyVariable(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), index = 1)
  private Map<ResourceLocation, JsonElement> tconstruct$injectRecipeIds(Map<ResourceLocation, JsonElement> recipes) {
    Map<ResourceLocation, JsonElement> copy = new HashMap<>(recipes.size());
    for (Map.Entry<ResourceLocation, JsonElement> entry : recipes.entrySet()) {
      JsonElement json = entry.getValue();
      if (json.isJsonObject()) {
        JsonObject object = json.getAsJsonObject().deepCopy();
        object.addProperty(RECIPE_ID_KEY, entry.getKey().toString());
        copy.put(entry.getKey(), object);
      } else {
        copy.put(entry.getKey(), json);
      }
    }
    return copy;
  }

  /** Sets the recipe ID for the duration of the codec decode */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Redirect(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
  private DataResult<?> tconstruct$parseRecipeWithId(Codec<?> codec, DynamicOps<?> ops, Object input) {
    ResourceLocation id = null;
    if (input instanceof JsonObject object) {
      JsonElement idElement = object.remove(RECIPE_ID_KEY);
      if (idElement != null && idElement.isJsonPrimitive()) {
        id = ResourceLocation.tryParse(idElement.getAsString());
      }
    }
    ResourceLocation previous = ContextKey.CURRENT_RECIPE_ID.get();
    ContextKey.CURRENT_RECIPE_ID.set(id);
    try {
      return ((Codec) codec).parse((DynamicOps) ops, input);
    } finally {
      ContextKey.CURRENT_RECIPE_ID.set(previous);
    }
  }
}

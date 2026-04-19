package slimeknights.tconstruct.library.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.TConstruct;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** TCon wrapper around Mantle's loadable serializer with a fallback ID for external codec callers. */
public class TConstructLoadableRecipeSerializer<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
  protected TConstructLoadableRecipeSerializer(RecordLoadable<T> loadable) {
    super(loadable);
  }

  /** Creates a standard serializer from a loadable. */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new TConstructLoadableRecipeSerializer<>(loadable);
  }

  /** Creates a type aware serializer from a loadable. */
  public static <T extends R, R extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  /** Creates a serializer that is deprecated, logging a warning when used. */
  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return new Deprecated<>(loadable, replacement);
  }

  /**
   * Some external recipe parsers invoke the codec without the path-based recipe ID.
   * Generate a deterministic synthetic ID so TCon's loadables still deserialize.
   */
  protected ResourceLocation getContextId(JsonObject json) {
    if (json.has("id")) {
      ResourceLocation id = ResourceLocation.tryParse(json.get("id").getAsString());
      if (id != null) {
        return id;
      }
    }

    ResourceLocation serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(this);
    String prefix = serializerId != null ? serializerId.getNamespace() + "/" + serializerId.getPath() : "unknown";
    String hash = Integer.toUnsignedString(json.toString().hashCode(), 36);
    return TConstruct.getResource("synthetic/" + prefix + "/" + hash);
  }

  @Override
  public MapCodec<T> codec() {
    return new MapCodec<>() {
      @Override
      public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
        try {
          JsonObject json = new JsonObject();
          input.entries().forEach(pair -> {
            String key = ops.getStringValue(pair.getFirst()).getOrThrow();
            O value = pair.getSecond();
            json.add(key, convertToJson(ops, value));
          });
          return DataResult.success(loadable.deserialize(json, buildContext(getContextId(json)).build()));
        } catch (Exception e) {
          return DataResult.error(e::getMessage);
        }
      }

      @Override
      public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
        try {
          JsonObject json = new JsonObject();
          loadable.serialize(input, json);
          for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
            prefix.add(entry.getKey(), com.mojang.serialization.JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
          }
          return prefix;
        } catch (Exception e) {
          return prefix.withErrorsFrom(DataResult.error(e::getMessage));
        }
      }

      @Override
      public <O> Stream<O> keys(DynamicOps<O> ops) {
        return Stream.empty();
      }
    };
  }

  /**
   * Converts a dynamic element to JSON, with a fallback for registry reference values that can appear
   * when external parsers invoke codecs through registry-aware ops.
   */
  private static <O> com.google.gson.JsonElement convertToJson(DynamicOps<O> ops, O value) {
    try {
      return ops.convertTo(com.mojang.serialization.JsonOps.INSTANCE, value);
    } catch (RuntimeException ignored) {
      // Fallback below
    }

    Optional<com.mojang.serialization.MapLike<O>> map = ops.getMap(value).result();
    if (map.isPresent()) {
      JsonObject json = new JsonObject();
      map.get().entries().forEach(entry -> {
        String key = ops.getStringValue(entry.getFirst()).getOrThrow();
        json.add(key, convertToJson(ops, entry.getSecond()));
      });
      return json;
    }

    Optional<Stream<O>> stream = ops.getStream(value).result();
    if (stream.isPresent()) {
      JsonArray json = new JsonArray();
      stream.get().forEach(child -> json.add(convertToJson(ops, child)));
      return json;
    }

    Optional<String> stringValue = ops.getStringValue(value).result();
    if (stringValue.isPresent()) {
      return new JsonPrimitive(stringValue.get());
    }

    Optional<Number> numberValue = ops.getNumberValue(value).result();
    if (numberValue.isPresent()) {
      return new JsonPrimitive(numberValue.get());
    }

    Optional<Boolean> booleanValue = ops.getBooleanValue(value).result();
    if (booleanValue.isPresent()) {
      return new JsonPrimitive(booleanValue.get());
    }

    String debug = String.valueOf(value);
    int equalsIndex = debug.lastIndexOf('=');
    if (debug.startsWith("Reference{") && equalsIndex >= 0 && debug.endsWith("}")) {
      return new JsonPrimitive(debug.substring(equalsIndex + 1, debug.length() - 1));
    }

    throw new IllegalArgumentException("Failed to convert dynamic value to JSON: " + debug);
  }

  public static class TypeAware<T extends Recipe<?>> extends TConstructLoadableRecipeSerializer<T> implements TypeAwareRecipeSerializer<T> {
    private final Supplier<? extends RecipeType<?>> type;

    protected TypeAware(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    protected slimeknights.mantle.util.typed.TypedMapBuilder buildContext() {
      return super.buildContext().put(TYPE, getType()).put(TYPED_SERIALIZER, this);
    }

    @Override
    public RecipeType<?> getType() {
      return type.get();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
      return StreamCodec.of(
        (buffer, recipe) -> {
          try {
            buffer.writeResourceLocation(getRecipeId(recipe));
            loadable.encode(buffer, recipe);
          } catch (RuntimeException e) {
            Mantle.logger.error("{}: Error writing recipe of type {} to packet using loadable {}", TypeAware.this.getClass().getSimpleName(), getType(), loadable, e);
            throw e;
          }
        },
        buffer -> {
          try {
            ResourceLocation id = buffer.readResourceLocation();
            return loadable.decode(buffer, buildContext(id).build());
          } catch (RuntimeException e) {
            Mantle.logger.error("{}: Error reading recipe of type {} from packet using loadable {}", TypeAware.this.getClass().getSimpleName(), getType(), loadable, e);
            throw e;
          }
        }
      );
    }
  }

  /** Helper class that logs a warning on recipe parse about planned removal. */
  private static class Deprecated<T extends Recipe<?>> extends TConstructLoadableRecipeSerializer<T> {
    private final String replacement;

    protected Deprecated(RecordLoadable<T> loadable, String replacement) {
      super(loadable);
      this.replacement = replacement;
    }

    @Override
    public MapCodec<T> codec() {
      MapCodec<T> original = super.codec();
      return original.xmap(
        recipe -> {
          Mantle.logger.warn("Using deprecated recipe serializer {}, {}", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this), replacement);
          return recipe;
        },
        recipe -> recipe
      );
    }
  }
}
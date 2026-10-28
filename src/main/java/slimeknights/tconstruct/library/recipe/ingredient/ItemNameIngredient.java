package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import lombok.EqualsAndHashCode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.JsonMapCodec;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient matching a single or multiple items by ID without allowing NBT components, item may be registered by another mod or not exist.
 * Replaces the removed Mantle datagen helper of the same name on top of a custom ingredient type.
 * JSON layout is the legacy Mantle one, nested under {@code "item"} or {@code "items"} beside the {@code "type"} key.
 */
@EqualsAndHashCode(of = "names")
public class ItemNameIngredient implements ICustomIngredient {
  /** Ingredient type, replaces the removed Mantle helper; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("item_name");

  /** Codec keeping the legacy JSON layout: single {@code {"item": id}} or multi {@code {"items": [id, ...]}} */
  public static final MapCodec<ItemNameIngredient> CODEC = new JsonMapCodec<>() {
    @Override
    protected ItemNameIngredient deserialize(JsonObject json, DynamicOps<?> ops) {
      if (json.has("item")) {
        JsonElement item = json.get("item");
        if (item.isJsonObject()) {
          throw new IllegalStateException("Unexpected object for 'item' in name ingredient");
        }
        return ItemNameIngredient.of(ResourceLocation.parse(item.getAsString()));
      }
      if (json.has("items")) {
        JsonElement items = json.get("items");
        if (!items.isJsonArray()) {
          throw new IllegalStateException("Unexpected value for 'items' in name ingredient");
        }
        return ItemNameIngredient.of(items.getAsJsonArray().asList().stream().map(element -> ResourceLocation.parse(element.getAsString())).toArray(ResourceLocation[]::new));
      }
      throw new IllegalStateException("Missing 'item' or 'items' in name ingredient");
    }

    @Override
    protected JsonObject serialize(ItemNameIngredient ingredient, DynamicOps<?> ops) {
      JsonObject result = new JsonObject();
      List<ResourceLocation> names = ingredient.names;
      if (names.size() == 1) {
        result.addProperty("item", names.get(0).toString());
      } else {
        JsonArray array = new JsonArray();
        for (ResourceLocation name : names) {
          array.add(name.toString());
        }
        result.add("items", array);
      }
      return result;
    }
  };

  /** Network codec: list of item names, symmetric on either side */
  public static final StreamCodec<RegistryFriendlyByteBuf, ItemNameIngredient> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.fromCodec(ResourceLocation.CODEC.listOf()), ingredient -> ingredient.names,
    ItemNameIngredient::new);

  /** Ingredient type, replaces the removed Mantle helper; registered in TinkerCommons */
  public static final IngredientType<ItemNameIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final List<ResourceLocation> names;

  private ItemNameIngredient(List<ResourceLocation> names) {
    this.names = names;
    if (names.isEmpty()) {
      throw new IllegalArgumentException("Empty name ingredient");
    }
  }

  private static ItemNameIngredient of(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient of(ResourceLocation... names) {
    return of(Arrays.asList(names));
  }

  /**
   * Creates a new ingredient from a list of names, wrapped for use in builders.
   * Multiple names produce a compound ingredient to keep the legacy list JSON shape.
   */
  public static Ingredient from(ResourceLocation... names) {
    if (names.length == 0) {
      throw new IllegalArgumentException("At least one item name required");
    }
    if (names.length == 1) {
      return new Ingredient(ItemNameIngredient.of(names[0]));
    }
    return CompoundIngredient.of(Arrays.stream(names).map(name -> new Ingredient(ItemNameIngredient.of(name))).toArray(Ingredient[]::new));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && Arrays.stream(names.toArray(ResourceLocation[]::new)).anyMatch(name -> name.equals(BuiltInRegistries.ITEM.getKey(stack.getItem())));
  }

  @Override
  public Stream<ItemStack> getItems() {
    // only display registered items; matches the legacy behavior of not exposing items that do not exist
    return Arrays.stream(names.toArray(ResourceLocation[]::new))
                 .filter(BuiltInRegistries.ITEM::containsKey)
                 .map(name -> new ItemStack(BuiltInRegistries.ITEM.get(name)));
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public String toString() {
    return "ItemNameIngredient" + Arrays.toString(names.toArray());
  }
}

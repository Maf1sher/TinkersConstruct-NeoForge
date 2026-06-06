package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("no_container");

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && super.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** MapCodec for JSON serialization */
  public static final MapCodec<NoContainerIngredient> CODEC = new MapCodec<>() {
    private static final String MATCH = "match";
    private static final String TYPE = "type";
    private static final String CHILDREN = "children";

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.concat(Stream.of(ops.createString(MATCH)), Ingredient.MAP_CODEC_NONEMPTY.keys(ops));
    }

    @Override
    public <T> DataResult<NoContainerIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
      T match = input.get(MATCH);
      if (match != null) {
        return Ingredient.CODEC_NONEMPTY.parse(ops, match).map(NoContainerIngredient::new);
      }
      // handle "children" key used by TCon's compound ingredient format
      T children = input.get(CHILDREN);
      if (children != null) {
        return Ingredient.CODEC_NONEMPTY.parse(ops, children).map(NoContainerIngredient::new);
      }
      return Ingredient.CODEC_NONEMPTY.parse(ops, ops.createMap(input.entries().filter(entry -> {
        DataResult<String> key = ops.getStringValue(entry.getFirst());
        return key.result().filter(name -> name.equals(MATCH) || name.equals(TYPE)).isEmpty();
      })))
        .map(NoContainerIngredient::new);
    }

    @Override
    public <T> RecordBuilder<T> encode(NoContainerIngredient ingredient, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      return Ingredient.MAP_CODEC_NONEMPTY.encode(ingredient.nested, ops, prefix);
    }
  };

  /** StreamCodec for network serialization */
  public static final StreamCodec<RegistryFriendlyByteBuf, NoContainerIngredient> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public NoContainerIngredient decode(RegistryFriendlyByteBuf buffer) {
      return new NoContainerIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, NoContainerIngredient ingredient) {
      Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.nested);
    }
  };

  /** IngredientType instance - must be registered to NeoForgeRegistries.INGREDIENT_TYPES */
  public static final IngredientType<NoContainerIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NoContainerIngredient that)) return false;
    return nested.equals(that.nested);
  }

  @Override
  public int hashCode() {
    return nested.hashCode() * 31 + 1;
  }


  /* Static constructors */

  /** Creates an instance from the given nested ingredient */
  public static NoContainerIngredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient);
  }

  /** Creates an instance from the given items */
  public static NoContainerIngredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  /** Creates an instance from the given stacks */
  public static NoContainerIngredient of(ItemStack... stacks) {
    return of(Ingredient.of(stacks));
  }

  /** Creates an instance from the given tag */
  public static NoContainerIngredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }
}

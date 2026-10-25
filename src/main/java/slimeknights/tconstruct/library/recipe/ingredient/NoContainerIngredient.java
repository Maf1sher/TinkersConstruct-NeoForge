package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.JsonMapCodec;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;

/** Custom ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("no_container");

  /** Codec keeping the legacy JSON layout: nested vanilla ingredients stay flat, anything else nests under {@code "match"} */
  public static final MapCodec<NoContainerIngredient> CODEC = new JsonMapCodec<>() {
    @Override
    protected NoContainerIngredient deserialize(JsonObject json, DynamicOps<?> ops) {
      return new NoContainerIngredient(decodeNestedJson(json));
    }

    @Override
    protected JsonObject serialize(NoContainerIngredient ingredient, DynamicOps<?> ops) {
      JsonObject json = new JsonObject();
      encodeNestedJson(ingredient.nested, json);
      return json;
    }
  };

  /** Network codec: just the nested ingredient */
  public static final StreamCodec<RegistryFriendlyByteBuf, NoContainerIngredient> STREAM_CODEC = StreamCodec.of(
    (buffer, ingredient) -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.nested),
    buffer -> new NoContainerIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)));

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final IngredientType<NoContainerIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && nested.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof NoContainerIngredient that && nested.equals(that.nested);
  }

  @Override
  public int hashCode() {
    return nested.hashCode();
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


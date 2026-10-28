package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Helpers to build {@link SizedIngredient}s.
 * NeoForge 1.21.1 replaced the old varargs factories and the {@code EMPTY} constant with {@code of(...)} overloads,
 * so these wrappers keep the recipe builders readable.
 */
public final class SizedIngredients {
  /** Empty ingredient with a size of 1 */
  public static final SizedIngredient EMPTY = new SizedIngredient(Ingredient.EMPTY, 1);

  /** Creates a sized ingredient of size 1 */
  public static SizedIngredient of(Ingredient ingredient) {
    return new SizedIngredient(ingredient, 1);
  }

  /** Creates a sized ingredient of size 1 */
  public static SizedIngredient of(ItemLike item) {
    return new SizedIngredient(Ingredient.of(item), 1);
  }

  /** Creates a sized ingredient */
  public static SizedIngredient of(ItemLike item, int count) {
    return new SizedIngredient(Ingredient.of(item), count);
  }

  /** Creates a sized ingredient from any of the given items */
  public static SizedIngredient of(int count, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), count);
  }

  /** Creates a sized ingredient of size 1 from any of the given items */
  public static SizedIngredient of(ItemLike... items) {
    return of(1, items);
  }

  /** Creates a sized ingredient from a tag */
  public static SizedIngredient of(TagKey<Item> tag, int count) {
    return new SizedIngredient(Ingredient.of(tag), count);
  }

  /** Creates a sized ingredient of size 1 from a tag */
  public static SizedIngredient of(TagKey<Item> tag) {
    return of(tag, 1);
  }

  /** Creates a sized ingredient of size 1 from a custom ingredient */
  public static SizedIngredient of(ICustomIngredient ingredient) {
    return new SizedIngredient(ingredient.toVanilla(), 1);
  }

  /** Creates a sized ingredient from a custom ingredient */
  public static SizedIngredient of(ICustomIngredient ingredient, int count) {
    return new SizedIngredient(ingredient.toVanilla(), count);
  }

  private SizedIngredients() {}
}

package slimeknights.tconstruct.library.recipe.molding;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Inventory for molding recipes
 */
public interface IMoldingContainer extends RecipeInput {
  /**
   * Gets the material being molded, typically sand
   * @return  Material item
   */
  ItemStack getMaterial();

  /**
   * Gets the item whose shape makes the pattern, typically in hand, often a tool part
   * @return  Pattern item
   */
  ItemStack getPattern();


  /* Required methods */

  /** Gets the item at the given index: index 0 is the material, index 1 is the pattern */
  @Override
  default ItemStack getItem(int index) {
    return switch (index) {
      case 0 -> getMaterial();
      case 1 -> getPattern();
      default -> ItemStack.EMPTY;
    };
  }

  @Override
  default int size() {
    return 2;
  }

  @Override
  default boolean isEmpty() {
    return getPattern().isEmpty() && getMaterial().isEmpty();
  }
}

package slimeknights.tconstruct.library.recipe.partbuilder;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.input.SingleItemInput;
import slimeknights.tconstruct.library.recipe.material.IMaterialValue;

import javax.annotation.Nullable;

/**
 * Inventory interface for the part builder
 */
public interface IPartBuilderContainer extends SingleItemInput {
  /**
   * Gets the material recipe based on the current slot contents
   * @return  Material recipe, or null if the slot contents are not a valid material
   */
  @Nullable
  IMaterialValue getMaterial();

  /**
   * Gets the stack in the pattern slot
   * @return  Pattern slot stack
   */
  ItemStack getPatternStack();

  /*
   * Gets the currently selected pattern item
   * @return
   */
  //ResourceLocation getPattern();

  /** Gets the stack in the material slot */
  ItemStack getStack();

  /** Gets the material slot's stack via the single item input API */
  @Override
  default ItemStack getItem() {
    return getStack();
  }

  /** Gets the item at the given index: index 0 is the material, index 1 is the pattern */
  @Override
  default ItemStack getItem(int index) {
    return switch (index) {
      case 0 -> getStack();
      case 1 -> getPatternStack();
      default -> ItemStack.EMPTY;
    };
  }

  @Override
  default boolean isEmpty() {
    return getStack().isEmpty() && getPatternStack().isEmpty();
  }

  @Override
  default int size() {
    return 2;
  }
}

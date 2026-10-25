package slimeknights.tconstruct.library.recipe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.input.SingleItemInput;

/** Simple class for an inventory containing just one item */
public class SingleItemContainer implements SingleItemInput {
  @Getter @Setter
  private ItemStack stack = ItemStack.EMPTY;

  @Override
  public ItemStack getItem() {
    return this.stack;
  }
}

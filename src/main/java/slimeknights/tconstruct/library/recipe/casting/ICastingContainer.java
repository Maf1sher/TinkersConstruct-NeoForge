package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.fluid.FluidTagHelper;
import slimeknights.mantle.recipe.input.SingleItemInput;

import javax.annotation.Nullable;

/**
 * Inventory containing a single item and a fluid
 */
public interface ICastingContainer extends SingleItemInput {
  /** Gets the stack in the casting container */
  ItemStack getStack();

  /** Gets the single input's stack from the casting container */
  @Override
  default ItemStack getItem() {
    return getStack();
  }

  /**
   * Gets the contained fluid in this inventory
   * @return  Contained fluid
   */
  Fluid getFluid();

  /**
   * Gets the NBT for the contained fluid
   * @return  Fluid's NBT
   */
  @Nullable
  default CompoundTag getFluidTag() {
    return null;
  }

  /**
   * Gets a copy of the contained fluid stack, preserving components such as potion contents.
   * @return  Contained fluid stack
   */
  default FluidStack getFluidStack() {
    return FluidTagHelper.withTag(getFluid(), 1, getFluidTag());
  }

  /**
   * The casting container is only empty when both the item and the fluid slots are empty. The recipe manager skips the
   * lookup entirely for empty inputs, which would otherwise hide cast-less recipes such as block casting.
   */
  @Override
  default boolean isEmpty() {
    return getStack().isEmpty() && getFluidStack().isEmpty();
  }
}

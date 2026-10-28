package slimeknights.tconstruct.library.fluid;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
 * Helpers for fluid stacks that still carry a legacy fluid tag.
 * <p>
 * NeoForge 1.21.1 replaced {@code FluidStack} NBT with data components, so the legacy tag is stored as custom data.
 * Used by recipes and containers that move potion data between stacks.
 */
public final class FluidTagHelper {
  /** Creates a fluid stack from a fluid, copying the given legacy tag into custom data */
  public static FluidStack withTag(Fluid fluid, int amount, @Nullable CompoundTag tag) {
    return applyTag(new FluidStack(fluid, amount), tag);
  }

  /** Creates a fluid stack from an existing stack with a new amount, copying the given legacy tag into custom data */
  public static FluidStack withTag(FluidStack base, int amount, @Nullable CompoundTag tag) {
    return applyTag(base.copyWithAmount(amount), tag);
  }

  /** Applies the given legacy tag to the stack as custom data */
  public static FluidStack applyTag(FluidStack stack, @Nullable CompoundTag tag) {
    if (tag != null && !tag.isEmpty()) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
    }
    return stack;
  }

  /** Gets the legacy tag from the stack, or null if absent */
  @Nullable
  public static CompoundTag getTag(FluidStack stack) {
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data == null ? null : data.copyTag();
  }

  private FluidTagHelper() {}
}

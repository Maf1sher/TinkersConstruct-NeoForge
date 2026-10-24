package slimeknights.tconstruct.fluids.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.tconstruct.TConstruct;

/**
 * Legacy Tinkers transfer type that empties a potion item, copying its potion component to the fluid.
 * @deprecated use {@link slimeknights.mantle.fluid.transfer.EmptyPotionTransfer}
 */
@Deprecated(forRemoval = true)
public class EmptyPotionTransfer extends EmptyFluidContainerTransfer {
  public static final ResourceLocation ID = TConstruct.getResource("empty_potion");

  private static final LoadableField<Ingredient,EmptyPotionTransfer> INPUT_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("input", transfer -> transfer.input);
  private static final LoadableField<ItemOutput,EmptyPotionTransfer> RESULT_FIELD = ItemOutput.OPTIONAL_ITEM_LOADABLE.defaultField("result", ItemOutput.EMPTY, false, transfer -> transfer.result);
  private static final LoadableField<FluidOutput,EmptyPotionTransfer> FLUID_FIELD = FluidOutput.REQUIRED_LOADABLE.requiredField("fluid", transfer -> transfer.fluid);
  public static final RecordLoadable<EmptyPotionTransfer> LOADER = RecordLoadable.create(INPUT_FIELD, RESULT_FIELD, FLUID_FIELD, EmptyPotionTransfer::new);

  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid) {
    super(input, filled, fluid);
  }

  @Override
  public RecordLoadable<? extends EmptyPotionTransfer> getLoader() {
    return LOADER;
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    if (stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
      return new FluidStack(Fluids.WATER, fluid.getAmount());
    }
    return DataComponentHelper.copy(fluid.get().copy(), stack, DataComponents.POTION_CONTENTS);
  }
}

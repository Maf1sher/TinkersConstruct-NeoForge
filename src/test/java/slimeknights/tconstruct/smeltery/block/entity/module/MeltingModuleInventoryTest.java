package slimeknights.tconstruct.smeltery.block.entity.module;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.IOreRate;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Regression tests for melting inventory NBT, shared by smelteries, foundries, and melters. */
class MeltingModuleInventoryTest extends BaseMcTest {
  private static final IOreRate NO_ORE_RATE = (rate, amount) -> amount;
  private static final HolderLookup.Provider REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  @Test
  void writeToTag_roundTripsItems() {
    MeltingModuleInventory inventory = createInventory();
    inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));

    CompoundTag tag = inventory.writeToTag(REGISTRIES);
    MeltingModuleInventory loaded = createInventory();
    loaded.readFromTag(REGISTRIES, tag);

    assertThat(loaded.getStackInSlot(0).getItem()).isEqualTo(Items.IRON_INGOT);
  }

  @Test
  void writeToTag_writesItemId() {
    MeltingModuleInventory inventory = createInventory();
    inventory.setStackInSlot(0, new ItemStack(Items.IRON_INGOT));

    CompoundTag itemTag = inventory.writeToTag(REGISTRIES).getList("items", Tag.TAG_COMPOUND).getCompound(0);
    assertThat(itemTag.contains("id", Tag.TAG_STRING)).isTrue();
  }

  private static MeltingModuleInventory createInventory() {
    return new MeltingModuleInventory(mock(MantleBlockEntity.class), new FluidTank(1000), NO_ORE_RATE, 1);
  }
}

package slimeknights.tconstruct.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.mantle.inventory.SingleItemHandler;
import slimeknights.tconstruct.library.tools.capability.inventory.InventoryModule;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Regression tests for item stack NBT helpers: 1.21 copies the prefix tag instead of mutating it. */
class ItemStackNbtPersistenceTest extends BaseMcTest {
  private static final HolderLookup.Provider REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  @Test
  void singleItemHandler_roundTripsStack() {
    SingleItemHandler<MantleBlockEntity> handler = createHandler();
    handler.setStack(new ItemStack(Items.IRON_INGOT));

    CompoundTag tag = handler.writeToNBT(REGISTRIES);
    SingleItemHandler<MantleBlockEntity> loaded = createHandler();
    loaded.readFromNBT(REGISTRIES, tag);

    assertThat(loaded.getStack().getItem()).isEqualTo(Items.IRON_INGOT);
  }

  @Test
  void inventoryModuleWriteStack_roundTripsStackAndSlot() {
    CompoundTag tag = InventoryModule.writeStack(new ItemStack(Items.IRON_INGOT), 3, new CompoundTag(), REGISTRIES);

    assertThat(ItemStack.parseOptional(REGISTRIES, tag).getItem()).isEqualTo(Items.IRON_INGOT);
    assertThat(tag.getInt(InventoryModule.TAG_SLOT)).isEqualTo(3);
  }

  private static SingleItemHandler<MantleBlockEntity> createHandler() {
    return new SingleItemHandler<>(mock(MantleBlockEntity.class), 64) {
      @Override
      protected boolean isItemValid(ItemStack stack) {
        return true;
      }
    };
  }
}

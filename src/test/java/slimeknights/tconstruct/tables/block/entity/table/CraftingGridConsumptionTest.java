package slimeknights.tconstruct.tables.block.entity.table;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The 1.21 crafting input is cropped to the bounding box of filled slots, so recipe remaining items are indexed relative
 * to that crop. Consumption must remap them onto the real crafting grid slots, otherwise items vanish from the top rows.
 */
class CraftingGridConsumptionTest extends BaseMcTest {

  /** Creates a crafting grid with the given slot indices filled with the given count of planks */
  private static SimpleContainer grid(int count, int... slots) {
    SimpleContainer grid = new SimpleContainer(CraftingStationBlockEntity.GRID_SIZE);
    for (int slot : slots) {
      grid.setItem(slot, new ItemStack(Items.OAK_PLANKS, count));
    }
    return grid;
  }

  /** Runs consumption for the grid using an all-empty remaining list, as a regular crafting recipe would return */
  private static List<ItemStack> consume(SimpleContainer grid) {
    CraftingContainerWrapper wrapper = wrapper(grid);
    CraftingInput.Positioned positioned = wrapper.asPositionedCraftInput();
    return consume(grid, positioned, NonNullList.withSize(positioned.input().size(), ItemStack.EMPTY));
  }

  private static List<ItemStack> consume(SimpleContainer grid, CraftingInput.Positioned positioned, NonNullList<ItemStack> remaining) {
    List<ItemStack> leftover = new ArrayList<>();
    CraftingStationBlockEntity.consumeCraftingInput(grid, positioned, remaining, leftover::add);
    return leftover;
  }

  private static CraftingContainerWrapper wrapper(SimpleContainer grid) {
    return new CraftingContainerWrapper(grid, CraftingStationBlockEntity.GRID_WIDTH, CraftingStationBlockEntity.GRID_HEIGHT);
  }

  private static int count(SimpleContainer grid, int slot) {
    return grid.getItem(slot).getCount();
  }

  @Test
  void singlePlankConsumesItsOwnSlot() {
    // button style recipe: only the bottom right slot is used, the old code always consumed slot 0 instead
    SimpleContainer grid = grid(2, 8);
    consume(grid);
    assertThat(count(grid, 8)).isEqualTo(1);
    for (int slot = 0; slot < 8; slot++) {
      assertThat(grid.getItem(slot).isEmpty()).as("slot %s", slot).isTrue();
    }
  }

  @Test
  void singlePlankInTopLeftStillConsumes() {
    SimpleContainer grid = grid(1, 0);
    consume(grid);
    assertThat(grid.getItem(0).isEmpty()).isTrue();
  }

  @Test
  void verticalStickConsumesColumnSlots() {
    // stick style recipe: 1x2 pattern in the left column, cropped input starts at row 1
    SimpleContainer grid = grid(2, 3, 6);
    consume(grid);
    assertThat(count(grid, 3)).isEqualTo(1);
    assertThat(count(grid, 6)).isEqualTo(1);
    assertThat(grid.getItem(0).isEmpty()).isTrue();
    assertThat(grid.getItem(1).isEmpty()).isTrue();
  }

  @Test
  void offsetSquareConsumesItsOwnSlots() {
    SimpleContainer grid = grid(2, 4, 5, 7, 8);
    consume(grid);
    for (int slot : new int[] {4, 5, 7, 8}) {
      assertThat(count(grid, slot)).as("slot %s", slot).isEqualTo(1);
    }
    for (int slot : new int[] {0, 1, 2, 3, 6}) {
      assertThat(grid.getItem(slot).isEmpty()).as("slot %s", slot).isTrue();
    }
  }

  @Test
  void topRightSquareConsumesItsOwnSlots() {
    SimpleContainer grid = grid(2, 1, 2, 4, 5);
    consume(grid);
    for (int slot : new int[] {1, 2, 4, 5}) {
      assertThat(count(grid, slot)).as("slot %s", slot).isEqualTo(1);
    }
    assertThat(grid.getItem(0).isEmpty()).isTrue();
    assertThat(grid.getItem(3).isEmpty()).isTrue();
  }

  @Test
  void fullGridConsumesOneOfEachSlot() {
    SimpleContainer grid = grid(2, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    consume(grid);
    for (int slot = 0; slot < CraftingStationBlockEntity.GRID_SIZE; slot++) {
      assertThat(count(grid, slot)).as("slot %s", slot).isEqualTo(1);
    }
  }

  @Test
  void containerRemainderReplacesItemInMappedSlot() {
    // bucket style recipe: the remaining item must land in the slot that was used, not in slot 0
    SimpleContainer grid = new SimpleContainer(CraftingStationBlockEntity.GRID_SIZE);
    grid.setItem(8, new ItemStack(Items.WATER_BUCKET));
    CraftingContainerWrapper wrapper = wrapper(grid);
    CraftingInput.Positioned positioned = wrapper.asPositionedCraftInput();
    NonNullList<ItemStack> remaining = NonNullList.withSize(1, ItemStack.EMPTY);
    remaining.set(0, new ItemStack(Items.BUCKET));

    consume(grid, positioned, remaining);

    assertThat(grid.getItem(8).getItem()).isEqualTo(Items.BUCKET);
    assertThat(grid.getItem(0).isEmpty()).isTrue();
  }

  @Test
  void unmergeableRemainderIsGivenToThePlayer() {
    SimpleContainer grid = grid(2, 8);
    CraftingContainerWrapper wrapper = wrapper(grid);
    CraftingInput.Positioned positioned = wrapper.asPositionedCraftInput();
    NonNullList<ItemStack> remaining = NonNullList.withSize(1, ItemStack.EMPTY);
    remaining.set(0, new ItemStack(Items.GLOWSTONE_DUST));

    List<ItemStack> leftover = consume(grid, positioned, remaining);

    assertThat(count(grid, 8)).isEqualTo(1);
    assertThat(leftover).hasSize(1);
    assertThat(leftover.get(0).getItem()).isEqualTo(Items.GLOWSTONE_DUST);
  }

  @Test
  void wrapperExposesGridShapeAndCopiesItems() {
    SimpleContainer grid = grid(3, 0);
    CraftingContainerWrapper wrapper = wrapper(grid);
    assertThat(wrapper.getWidth()).isEqualTo(3);
    assertThat(wrapper.getHeight()).isEqualTo(3);
    assertThat(wrapper.getItems()).hasSize(CraftingStationBlockEntity.GRID_SIZE);
    assertThat(wrapper.getItems().get(0)).isEqualTo(grid.getItem(0));

    // the list must be a copy, mutating it cannot bypass Container#setItem
    wrapper.getItems().set(0, new ItemStack(Items.STONE));
    assertThat(grid.getItem(0).getItem()).isEqualTo(Items.OAK_PLANKS);
  }

  @Test
  void wrapperFeedsStackedContents() {
    SimpleContainer grid = grid(1, 0, 1, 2, 3, 4, 5, 6, 7, 8);
    StackedContents contents = new StackedContents();
    wrapper(grid).fillStackedContents(contents);
    assertThat(contents.contents.get(BuiltInRegistries.ITEM.getId(Items.OAK_PLANKS))).isEqualTo(9);
  }
}

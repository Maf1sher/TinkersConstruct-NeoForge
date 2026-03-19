package slimeknights.tconstruct.tables.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.tables.block.entity.chest.TinkersChestBlockEntity;

import javax.annotation.Nullable;

/** Dyeable chest block item - implements color methods directly since DyeableLeatherItem was removed in 1.21.1 */
public class TinkersChestBlockItem extends BlockItem {
  public TinkersChestBlockItem(Block blockIn, Properties builder) {
    super(blockIn, builder);
  }

  /** Gets the display sub-tag from custom data, or null if not present */
  @Nullable
  private static CompoundTag getDisplayTag(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData != null) {
      CompoundTag tag = customData.copyTag();
      if (tag.contains("display", Tag.TAG_COMPOUND)) {
        return tag.getCompound("display");
      }
    }
    return null;
  }

  /** Gets the color of this chest item */
  public int getColor(ItemStack stack) {
    CompoundTag display = getDisplayTag(stack);
    return display != null && display.contains("color", Tag.TAG_ANY_NUMERIC) ? display.getInt("color") : TinkersChestBlockEntity.DEFAULT_COLOR;
  }

  /** Checks if this stack has a custom color set */
  public boolean hasCustomColor(ItemStack stack) {
    CompoundTag display = getDisplayTag(stack);
    return display != null && display.contains("color", Tag.TAG_ANY_NUMERIC);
  }

  /** Sets the color on the given stack */
  public void setColor(ItemStack stack, int color) {
    CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    CompoundTag display = tag.contains("display", Tag.TAG_COMPOUND) ? tag.getCompound("display") : new CompoundTag();
    display.putInt("color", color);
    tag.put("display", display);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  }

  @Override
  protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
    boolean result = super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
    if (hasCustomColor(stack)) {
      int color = getColor(stack);
      BlockEntityHelper.get(TinkersChestBlockEntity.class, worldIn, pos).ifPresent(te -> te.setColor(color));
    }
    return result;
  }
}

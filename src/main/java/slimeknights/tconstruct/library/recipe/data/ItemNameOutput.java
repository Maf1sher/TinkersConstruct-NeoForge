package slimeknights.tconstruct.library.recipe.data;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.helper.ItemOutput;

import javax.annotation.Nullable;

/**
 * Extension of {@link ItemOutput} for datagen of recipes for compat. Should never be used in an actual recipe.
 * Replaces the removed Mantle helper of the same name.
 */
public class ItemNameOutput extends ItemOutput {
  private final ResourceLocation name;
  private final int count;

  private ItemNameOutput(ResourceLocation name, int count) {
    this.name = name;
    this.count = count;
  }

  /**
   * Creates an output for the given item
   * @param name   Item name
   * @param count  Count
   * @return  Output
   */
  public static ItemNameOutput fromName(ResourceLocation name, int count) {
    return new ItemNameOutput(name, count);
  }

  /**
   * Creates an output for the given item with a count of 1
   * @param name  Item name
   * @return  Output
   */
  public static ItemNameOutput fromName(ResourceLocation name) {
    return fromName(name, 1);
  }

  @Override
  public ItemStack get() {
    throw new UnsupportedOperationException("Cannot get the item stack from a item name output");
  }

  @Override
  public Either<Item, TagKey<Item>> getValueOrTag() {
    throw new UnsupportedOperationException("Cannot resolve the item from an item name output");
  }

  @Override
  public ResourceLocation getName() {
    return name;
  }

  @Override
  public DataComponentPatch getComponents() {
    return DataComponentPatch.EMPTY;
  }

  @Override
  public int getCount() {
    return count;
  }

  /** Gets the item name */
  public ResourceLocation getId() {
    return name;
  }

  /** @deprecated not applicable, name outputs have no tag */
  @Nullable
  @Deprecated
  public TagKey<Item> getTag() {
    return null;
  }
}

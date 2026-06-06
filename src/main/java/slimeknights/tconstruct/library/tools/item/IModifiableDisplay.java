package slimeknights.tconstruct.library.tools.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tools.item.CreativeSlotItem;

/**
 * Interface for tools to display in books and other similar contexts
 */
public interface IModifiableDisplay extends IModifiable, ITinkerStationDisplay {
  /**
   * Gets a tool meant for rendering in a screen, can (and should) return the same stack on multiple calls
   *
   * @return the tool to use for rendering
   */
  ItemStack getRenderTool();

  /** Helper method to convert an item into its display tool, if it uses this interface */
  static ItemStack getDisplayStack(Item item) {
    if (item instanceof IModifiableDisplay display) {
      return display.getRenderTool();
    }
    return getDisplayStack(new ItemStack(item));
  }

  /** Helper method to convert a stack into its display tool, if it uses this interface */
  static ItemStack getDisplayStack(ItemStack stack) {
    Item item = stack.getItem();
    if (item instanceof IModifiableDisplay display) {
      ItemStack tool = display.getRenderTool();
      return stack.getCount() > 1 ? tool.copyWithCount(stack.getCount()) : tool;
    }
    // Handle creative slots
    if (item instanceof CreativeSlotItem && CreativeSlotItem.getSlot(stack) == null) {
      ItemStack result = CreativeSlotItem.withSlot(stack.copy(), SlotType.UPGRADE);
      return stack.getCount() > 1 ? result.copyWithCount(stack.getCount()) : result;
    }
    // Handle parts and other material items
    if (item instanceof IMaterialItem materialItem && materialItem.getMaterial(stack).equals(IMaterial.UNKNOWN_ID)) {
      if (MaterialRegistry.isFullyLoaded()) {
        MaterialStatsId statId = (materialItem instanceof IToolPart toolPart) ? toolPart.getStatType() : null;
        IMaterial material = statId != null ? MaterialRegistry.firstWithStatType(statId) : MaterialRegistry.getInstance().getVisibleMaterials().stream().findFirst().orElse(null);
        if (material != null) {
          ItemStack result = materialItem.withMaterial(material.getIdentifier());
          return stack.getCount() > 1 ? result.copyWithCount(stack.getCount()) : result;
        }
      }
    }
    return stack;
  }
}

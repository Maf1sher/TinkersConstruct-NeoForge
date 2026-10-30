package slimeknights.tconstruct.tables.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import slimeknights.mantle.client.render.InventoryBlockEntityRenderer;
import slimeknights.tconstruct.shared.block.entity.TableBlockEntity;

/**
 * Renderer for tables. Keeps the full two block height visible because displayed items float above the block,
 * so a one block render box culls them near the screen edges.
 */
public class TableBlockEntityRenderer extends InventoryBlockEntityRenderer<TableBlockEntity> {
  public TableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public AABB getRenderBoundingBox(TableBlockEntity table) {
    // one block plus the block above, matching the 1.20.1 table bounds
    return new AABB(table.getBlockPos()).expandTowards(0, 1, 0);
  }
}

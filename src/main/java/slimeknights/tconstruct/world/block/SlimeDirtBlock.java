package slimeknights.tconstruct.world.block;

import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * Slime dirt block. In 1.21+, canSustainPlant/IPlantable/PlantType are removed.
 * Plant sustainability is now handled via block tags instead.
 */
public class SlimeDirtBlock extends Block {

  public SlimeDirtBlock(Properties properties) {
    super(properties);
  }
}

package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientInvertedFluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.tconstruct.common.TinkerTags;

import java.util.function.Consumer;

/** Fluid Type that does not affect slimes */
public class SlimeFluidType extends FluidType {
  public SlimeFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean canDrownIn(LivingEntity entity) {
    return !entity.getType().is(TinkerTags.EntityTypes.SLIMES);
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this));
  }

  public static class Inverted extends SlimeFluidType {
    public Inverted(Properties properties) {
      super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
      consumer.accept(new ClientInvertedFluidType(this));
    }
  }
}

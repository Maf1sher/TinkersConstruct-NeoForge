package slimeknights.tconstruct.fluids;

import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

/** Fluid-related game-bus event handlers. */
public class FluidEvents {
  public static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
    if (event.getItemStack().getItem() == TinkerFluids.blazingBlood.asItem()) {
      // 150% efficiency compared to lava bucket, compare to casting blaze rods, which cast into 120%
      event.setBurnTime(30000);
    }
  }

}

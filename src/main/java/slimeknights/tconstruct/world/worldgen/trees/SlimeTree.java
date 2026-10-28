package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.world.level.block.grower.TreeGrower;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.block.FoliageType;

import java.util.Optional;

/** Creates the tree grower used by slime saplings. {@link TreeGrower} is final in 1.21.1, so each foliage type gets its own instance. */
public final class SlimeTree {
  private SlimeTree() {}

  /** Creates the grower for the given foliage type */
  public static TreeGrower create(FoliageType foliageType) {
    return switch (foliageType) {
      case EARTH -> new TreeGrower("tconstruct:earth_slime", Optional.empty(), Optional.of(TinkerStructures.earthSlimeTree), Optional.empty());
      case SKY -> new TreeGrower("tconstruct:sky_slime", Optional.empty(), Optional.of(TinkerStructures.skySlimeTree), Optional.empty());
      // ender slimes mostly grow the tall variant, matching the old 85% chance
      case ENDER -> new TreeGrower("tconstruct:ender_slime", 0.85F, Optional.empty(), Optional.empty(), Optional.of(TinkerStructures.enderSlimeTree), Optional.of(TinkerStructures.enderSlimeTreeTall), Optional.empty(), Optional.empty());
      case BLOOD -> new TreeGrower("tconstruct:blood_slime", Optional.empty(), Optional.of(TinkerStructures.bloodSlimeFungus), Optional.empty());
      case ICHOR -> new TreeGrower("tconstruct:ichor_slime", Optional.empty(), Optional.of(TinkerStructures.ichorSlimeFungus), Optional.empty());
    };
  }
}

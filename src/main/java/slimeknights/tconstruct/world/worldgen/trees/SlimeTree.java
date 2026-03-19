package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.world.level.block.grower.TreeGrower;
import slimeknights.tconstruct.world.TinkerStructures;

import java.util.Optional;

/** Holds TreeGrower instances for each slime tree type */
public class SlimeTree {
  public static final TreeGrower EARTH = new TreeGrower(
    "tconstruct:earth_slime",
    Optional.empty(),
    Optional.of(TinkerStructures.earthSlimeTree),
    Optional.empty()
  );

  public static final TreeGrower SKY = new TreeGrower(
    "tconstruct:sky_slime",
    Optional.empty(),
    Optional.of(TinkerStructures.skySlimeTree),
    Optional.empty()
  );

  public static final TreeGrower ENDER = new TreeGrower(
    "tconstruct:ender_slime",
    0.15f,
    Optional.empty(),
    Optional.empty(),
    Optional.of(TinkerStructures.enderSlimeTreeTall),
    Optional.of(TinkerStructures.enderSlimeTree),
    Optional.empty(),
    Optional.empty()
  );

  private SlimeTree() {}
}

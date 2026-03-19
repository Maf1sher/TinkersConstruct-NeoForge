package slimeknights.tconstruct.library;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * Custom transform types used for tinkers item rendering.
 * In NeoForge 1.21, ItemDisplayContext is an extensible enum.
 * Custom values are defined via EnumProxy fields + META-INF/enumextensions.json.
 */
public class TinkerItemDisplays {
  private TinkerItemDisplays() {}

  /** Called during mod construction to trigger class loading and ensure enum proxies are resolved */
  public static void init() {
    // Class loading of this class ensures the EnumProxy fields are accessible.
    // The actual enum extension happens at class load time via enumextensions.json.
  }

  // EnumProxy fields - these are populated by the NeoForge enum extension system at class load time.
  // Each proxy references an ItemDisplayContext enum value defined in enumextensions.json.
  // The constructor params in the proxy must match: (int id, String serializedName)

  /** Proxy for MELTER display context */
  public static final EnumProxy<ItemDisplayContext> MELTER_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:melter"
  );
  /** Proxy for TABLE display context */
  public static final EnumProxy<ItemDisplayContext> TABLE_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:table"
  );
  /** Proxy for CASTING_TABLE display context */
  public static final EnumProxy<ItemDisplayContext> CASTING_TABLE_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:casting_table"
  );
  /** Proxy for CASTING_BASIN display context */
  public static final EnumProxy<ItemDisplayContext> CASTING_BASIN_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:casting_basin"
  );
  /** Proxy for FLUID_CANNON display context */
  public static final EnumProxy<ItemDisplayContext> FLUID_CANNON_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:fluid_cannon"
  );
  /** Proxy for THROWN display context */
  public static final EnumProxy<ItemDisplayContext> THROWN_PROXY = new EnumProxy<>(
    ItemDisplayContext.class, -1, "tconstruct:thrown"
  );

  /** Used by the melter and smeltery for display of items its melting */
  public static final ItemDisplayContext MELTER = MELTER_PROXY.getValue();
  /** Used by the part builder, crafting station, tinkers station, and tinker anvil */
  public static final ItemDisplayContext TABLE = TABLE_PROXY.getValue();
  /** Used by the casting table for item rendering */
  public static final ItemDisplayContext CASTING_TABLE = CASTING_TABLE_PROXY.getValue();
  /** Used by the casting basin for item rendering */
  public static final ItemDisplayContext CASTING_BASIN = CASTING_BASIN_PROXY.getValue();
  /** Used by the fluid cannon for display of the item in front */
  public static final ItemDisplayContext FLUID_CANNON = FLUID_CANNON_PROXY.getValue();
  /** Used by throwing to allow adjusting the tool position */
  public static final ItemDisplayContext THROWN = THROWN_PROXY.getValue();
}

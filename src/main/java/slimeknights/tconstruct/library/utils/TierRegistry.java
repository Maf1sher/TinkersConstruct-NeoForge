package slimeknights.tconstruct.library.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom tier registry to replace the removed TierSortingRegistry in NeoForge 1.21.
 * Provides name-to-tier and tier-to-name mapping, a sorted tier list, and comparison helpers.
 */
public class TierRegistry {
  private TierRegistry() {}

  /** Bidirectional map between ResourceLocation names and Tier instances */
  private static final BiMap<ResourceLocation, Tier> TIERS = HashBiMap.create();
  /** Sorted list of tiers from lowest to highest, built lazily */
  private static List<Tier> sortedTiers = null;

  static {
    // Register vanilla tiers in order from lowest to highest
    register(ResourceLocation.withDefaultNamespace("wood"), Tiers.WOOD);
    register(ResourceLocation.withDefaultNamespace("stone"), Tiers.STONE);
    register(ResourceLocation.withDefaultNamespace("iron"), Tiers.IRON);
    register(ResourceLocation.withDefaultNamespace("diamond"), Tiers.DIAMOND);
    register(ResourceLocation.withDefaultNamespace("gold"), Tiers.GOLD);
    register(ResourceLocation.withDefaultNamespace("netherite"), Tiers.NETHERITE);
  }

  /**
   * Registers a tier with a name. Can be called by addons to register custom tiers.
   * @param name  The resource location name for the tier
   * @param tier  The tier instance
   */
  public static void register(ResourceLocation name, Tier tier) {
    TIERS.put(name, tier);
    sortedTiers = null; // invalidate cached sorted list
  }

  /**
   * Gets the name for a tier
   * @param tier  The tier
   * @return  The resource location name, or null if not registered
   */
  @Nullable
  public static ResourceLocation getName(Tier tier) {
    return TIERS.inverse().get(tier);
  }

  /**
   * Gets a tier by name
   * @param name  The resource location name
   * @return  The tier, or null if not registered
   */
  @Nullable
  public static Tier byName(ResourceLocation name) {
    return TIERS.get(name);
  }

  /**
   * Gets a sorted list of tiers. Sorted by the tag level of their incorrectBlocksForDrops:
   * WOOD (no tag) < STONE < IRON < DIAMOND < NETHERITE, with GOLD sorted next to WOOD (same tag level).
   * Custom tiers are sorted based on their tag hierarchy.
   */
  public static List<Tier> getSortedTiers() {
    if (sortedTiers == null) {
      List<Tier> list = new ArrayList<>(TIERS.values());
      // Sort by the level implied by their incorrect blocks tag
      // Higher level tiers have more restrictive incorrect block tags
      list.sort((a, b) -> Integer.compare(getTierLevel(a), getTierLevel(b)));
      sortedTiers = Collections.unmodifiableList(list);
    }
    return sortedTiers;
  }

  /**
   * Computes a numeric level for a tier based on known vanilla tiers.
   * This is used for sorting and comparison.
   */
  private static int getTierLevel(Tier tier) {
    // For vanilla tiers, use known ordering
    if (tier == Tiers.WOOD || tier == Tiers.GOLD) return 0;
    if (tier == Tiers.STONE) return 1;
    if (tier == Tiers.IRON) return 2;
    if (tier == Tiers.DIAMOND) return 3;
    if (tier == Tiers.NETHERITE) return 4;
    // For custom tiers, try to determine based on tag key name
    // Default to a level based on the tag name
    var tag = tier.getIncorrectBlocksForDrops();
    String path = tag.location().getPath();
    if (path.contains("netherite")) return 4;
    if (path.contains("diamond")) return 3;
    if (path.contains("iron")) return 2;
    if (path.contains("stone")) return 1;
    // Unknown tier, default above netherite
    return 5;
  }
}

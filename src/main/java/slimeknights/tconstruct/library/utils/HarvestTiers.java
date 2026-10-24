package slimeknights.tconstruct.library.utils;

import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

/**
 * Harvest level display names
 */
public class HarvestTiers {
  private HarvestTiers() {}

  /**
   * 1.21.1 removed Forge's TierSortingRegistry; tier ordering now derives from the vanilla {@link Tiers} enum order.
   * IDs and ordering below are an approximation for that API: only vanilla {@link Tiers} instances have an ID or an order,
   * any other tier instance behaves like an unregistered tier did before (index of -1, no ID).
   */

  /** Cache of name for each tier */
  private static final Map<Tier, Component> harvestLevelNames = Maps.newHashMap();
  /** Listener to clear name cache so we get new colors */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = manager -> harvestLevelNames.clear();

  /** Makes a translation key for the given name */
  private static MutableComponent makeLevelKey(Tier tier) {
    String key = Util.makeTranslationKey("harvest_tier", getId(tier));
    TextColor color = ResourceColorManager.getTextColor(key);
    return TConstruct.makeTranslation("stat", key).withStyle(style -> style.withColor(color));
  }

  /**
   * Gets the harvest level name for the given level number
   * @param tier  Tier
   * @return  Level name
   */
  public static Component getName(Tier tier) {
    return harvestLevelNames.computeIfAbsent(tier, n ->  makeLevelKey(tier));
  }

  /**
   * Gets the ID of the given tier, comparable to the pre-1.21 TierSortingRegistry names.
   * @param tier  tier to look up
   * @return  the tier ID, or null for non-vanilla tiers
   */
  @Nullable
  public static ResourceLocation getId(Tier tier) {
    if (tier instanceof Tiers builtIn) {
      return ResourceLocation.fromNamespaceAndPath("minecraft", builtIn.name().toLowerCase(Locale.ROOT));
    }
    return null;
  }

  /**
   * Looks up a tier by its ID, comparable to the pre-1.21 TierSortingRegistry lookup.
   * @param name  tier ID
   * @return  the tier, or null if unknown
   */
  @Nullable
  public static Tier byName(ResourceLocation name) {
    if (name.getNamespace().equals("minecraft")) {
      try {
        return Tiers.valueOf(name.getPath().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException ex) {
        // fall through to null
      }
    }
    return null;
  }

  /** Ordering index of a tier, or -1 if it has no defined order in the 1.21.1 approximation */
  private static int getIndex(Tier tier) {
    return tier instanceof Tiers builtIn ? builtIn.ordinal() : -1;
  }

  /** Gets the larger of two tiers */
  public static Tier max(Tier a, Tier b) {
    // note the compare returns -1 if the tier is missing, so the larger of an unsorted tier and a sorted one is the sorted one
    if (getIndex(b) > getIndex(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smaller of two tiers */
  public static Tier min(Tier a, Tier b) {
    // note the compare returns -1 if the tier is missing, so the smaller of an unsorted tier and a sorted one is the unsorted one
    if (getIndex(b) < getIndex(a)) {
      return b;
    }
    return a;
  }

  /** Gets the smallest known tier */
  public static Tier minTier() {
    return Tiers.WOOD;
  }
}

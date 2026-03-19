package slimeknights.tconstruct.test;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.ModLoadingContext;
import org.junit.jupiter.api.BeforeAll;

public class BaseMcTest {

  @SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
  @BeforeAll
  static void setUpRegistries() {
    // In NeoForge 1.21.1 test environment, SharedConstants and Bootstrap are already
    // initialized by the mod loading framework. Only set them if not already done.
    try {
      SharedConstants.getCurrentVersion();
      // Version is already set, skip initialization
    } catch (NullPointerException e) {
      SharedConstants.setVersion(TestWorldVersion.INSTANCE);
    }
    Bootstrap.bootStrap();
    ModLoadingContext.get().setActiveContainer(new TestModContainer(TestModInfo.INSTANCE));
  }

  /** No need to set it up multiple times */
  private static boolean setupTiers = false;

  /** Sets up the tier sorting registry */
  public static void setupTierSorting() {
    if (setupTiers) {
      return;
    }
    setupTiers = true;
    // TierSortingRegistry was removed in NeoForge 1.21.1, tiers are handled by vanilla now
  }
}

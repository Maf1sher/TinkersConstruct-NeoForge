package slimeknights.tconstruct.test;

import org.junit.jupiter.api.BeforeAll;

public class BaseMcTest {
  static {
    // bootstrap in a static initializer so it runs before subclasses initialize any Minecraft classes
    TestBootstrap.init();
  }

  @BeforeAll
  static void setUpRegistries() {
    // no-op: the static initializer already bootstrapped the game, kept as an anchor for subclasses
    TestBootstrap.init();
  }

  /** Tier sorting no longer exists in 1.21.1, kept so existing tests do not need changes */
  public static void setupTierSorting() {
  }
}

package slimeknights.tconstruct.test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.tconstruct.library.utils.JsonUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Shared bootstrap for tests that touch Minecraft or NeoForge classes.
 * <p>
 * Bootstrap must happen in a static initializer rather than {@code @BeforeAll}: any test class that references a
 * Minecraft class while being loaded initializes the vanilla registries, which permanently fails (and poisons the JVM)
 * when bootstrap has not run yet.
 */
public final class TestBootstrap {
  private static boolean initialized = false;

  private TestBootstrap() {}

  /** Prepares the vanilla registries and NeoForge loading state; safe to call repeatedly */
  public static synchronized void init() {
    if (initialized) {
      return;
    }
    initialized = true;
    // Mockito 4.2 ships ByteBuddy 1.12, which refuses Java 21 class files unless experimental support is enabled
    System.setProperty("net.bytebuddy.experimental", "true");
    // NeoForge's feature flag loader reads the FML mod list while bootstrapping, which is absent in unit tests
    installEmptyLoadingModList();
    SharedConstants.setVersion(TestWorldVersion.INSTANCE);
    Bootstrap.bootStrap();
    registerBuiltinConditions();
    markVanillaRegistriesSynced();
    // cannot read config during tests, and no need to log these values
    JsonUtils.forceDisableDebugLog();
  }

  /** Registers the built-in NeoForge conditions, which are normally added during mod loading */
  private static void registerBuiltinConditions() {
    Registry.register(NeoForgeRegistries.CONDITION_SERIALIZERS, ResourceLocation.fromNamespaceAndPath("neoforge", "true"), TrueCondition.CODEC);
    Registry.register(NeoForgeRegistries.CONDITION_SERIALIZERS, ResourceLocation.fromNamespaceAndPath("neoforge", "false"), FalseCondition.CODEC);
  }

  /**
   * NeoForge marks vanilla registries as synced during mod loading. Stream codecs such as {@code ItemStack.STREAM_CODEC}
   * refuse to encode without that flag, so mirror the NeoForge registry setup here.
   */
  private static void markVanillaRegistriesSynced() {
    try {
      Field syncField = Class.forName("net.neoforged.neoforge.registries.BaseMappedRegistry").getDeclaredField("sync");
      syncField.setAccessible(true);
      Field syncRegistries = Class.forName("net.neoforged.neoforge.registries.NeoForgeRegistriesSetup").getDeclaredField("VANILLA_SYNC_REGISTRIES");
      syncRegistries.setAccessible(true);
      for (Object registry : (Iterable<?>) syncRegistries.get(null)) {
        syncField.setBoolean(registry, true);
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to mark vanilla registries as synced", e);
    }
  }

  /** Installs an empty mod list so NeoForge bootstrap code that queries FML has something to read */
  private static void installEmptyLoadingModList() {
    try {
      Class<?> loadingModList = Class.forName("net.neoforged.fml.loading.LoadingModList");
      Object empty = loadingModList
        .getMethod("of", List.class, List.class, List.class, List.class, Map.class)
        .invoke(null, List.of(), List.of(), List.of(), List.of(), Map.of());
      Field instance = loadingModList.getDeclaredField("INSTANCE");
      instance.setAccessible(true);
      instance.set(null, empty);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to install the empty FML loading mod list", e);
    }
  }
}

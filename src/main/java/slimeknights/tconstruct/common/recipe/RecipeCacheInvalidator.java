package slimeknights.tconstruct.common.recipe;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import slimeknights.mantle.data.listener.IEarlySafeManagerReloadListener;
import slimeknights.tconstruct.TConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class that handles notifying recipe caches that they need to invalidate
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RecipeCacheInvalidator implements IEarlySafeManagerReloadListener {
  private static final RecipeCacheInvalidator INSTANCE = new RecipeCacheInvalidator();
  private static final List<BooleanConsumer> listeners = new ArrayList<>();

  /**
   * Adds a new listener that runs every time the recipes are reloaded
   * @param runnable  Runnable accepting a boolean representing if this is client side
   */
  public static void addReloadListener(BooleanConsumer runnable) {
    listeners.add(runnable);
  }

  /**
   * Registers a listener that properly responds to the client side
   * @param runnable  Runnable to clear cache
   * @return  Object that can clear cache as needed
   */
  public static DuelSidedListener addDuelSidedListener(Runnable runnable) {
    DuelSidedListener listener = new DuelSidedListener(runnable);
    addReloadListener(listener);
    return listener;
  }

  /**
   * Reloads all listeners, used client side
   */
  public static void reload(boolean client) {
    for (BooleanConsumer runnable : listeners) {
      runnable.accept(client);
    }
  }

  @Override
  public void onReloadSafe(ResourceManager resourceManager) {
    logRecipeResourceState(resourceManager);
    reload(false);
  }

  /** Logs basic datapack visibility for debugging recipe loading issues */
  private static void logRecipeResourceState(ResourceManager resourceManager) {
    ResourceLocation legacySample = TConstruct.getResource("recipes/world/wood/skyroot/planks.json");
    ResourceLocation modernSample = TConstruct.getResource("recipe/world/wood/skyroot/planks.json");
    Optional<Resource> legacyRecipe = resourceManager.getResource(legacySample);
    Optional<Resource> modernRecipe = resourceManager.getResource(modernSample);
    Map<ResourceLocation,Resource> tconstructRecipes = resourceManager.listResources("recipes", location -> location.getNamespace().equals(TConstruct.MOD_ID) && location.getPath().endsWith(".json"));
    Map<ResourceLocation,Resource> tconstructModernRecipes = resourceManager.listResources("recipe", location -> location.getNamespace().equals(TConstruct.MOD_ID) && location.getPath().endsWith(".json"));
    List<String> samples = tconstructModernRecipes.keySet().stream().limit(5).map(ResourceLocation::toString).toList();
    TConstruct.LOG.info("TConstruct datapack visibility: legacy_sample_present={}, modern_sample_present={}, legacy_recipe_files={}, modern_recipe_files={}, sample_ids={}", legacyRecipe.isPresent(), modernRecipe.isPresent(), tconstructRecipes.size(), tconstructModernRecipes.size(), samples.isEmpty() ? "<none>" : samples);
  }

  /**
   * Called when resource managers reload
   * @param event  Reload event
   */
  public static void onReloadListenerReload(AddReloadListenerEvent event) {
    event.addListener(INSTANCE);
  }

  /** Logic to respond properly to late running of the client */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class DuelSidedListener implements BooleanConsumer {
    private final Runnable clearCache;
    private boolean clearQueued = false;

    @Override
    public void accept(boolean client) {
      // client side event runs at the end of recipe loading
      // server side runs at the start
      // so queue client side to run at the beginning of the next recipe list
      if (client) {
        clearQueued = true;
      } else {
        clearCache();
      }
    }

    /**
     * Clears the cache based on the runnable
     */
    public void clearCache() {
      clearQueued = false;
      clearCache.run();
    }

    /**
     * Clears the cache if a clear is queued. Intended to be called during add
     */
    public void checkClear() {
      if (clearQueued) {
        clearCache();
      }
    }
  }
}

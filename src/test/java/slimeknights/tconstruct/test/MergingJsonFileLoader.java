package slimeknights.tconstruct.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.data.listener.MergingJsonDataLoader;
import slimeknights.tconstruct.library.utils.ResourceId;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static org.mockito.Mockito.mock;

/**
 * Extension of {@link JsonFileLoader} with extra functionality to mock multiple data packs.
 * Uses reflection to access protected fields/methods on MergingJsonDataLoader since they are
 * in a different module (mantle) and protected access across modules requires subclassing.
 * @param <B>  Builder type
 */
public class MergingJsonFileLoader<B> extends JsonFileLoader {
  private final MergingJsonDataLoader<B> dataLoader;
  private final Gson loaderGson;
  private final String loaderFolder;
  private final Function<ResourceLocation, B> loaderBuilderConstructor;
  private final Method parseMethod;
  private final Method finishLoadMethod;

  @SuppressWarnings("unchecked")
  public MergingJsonFileLoader(MergingJsonDataLoader<B> dataLoader) {
    super(getFieldValue(dataLoader, "gson", Gson.class), getFieldValue(dataLoader, "folder", String.class));
    this.dataLoader = dataLoader;
    this.loaderGson = getFieldValue(dataLoader, "gson", Gson.class);
    this.loaderFolder = getFieldValue(dataLoader, "folder", String.class);
    this.loaderBuilderConstructor = getFieldValue(dataLoader, "builderConstructor", Function.class);
    try {
      this.parseMethod = MergingJsonDataLoader.class.getDeclaredMethod("parse", Object.class, ResourceLocation.class, JsonElement.class);
      this.parseMethod.setAccessible(true);
      this.finishLoadMethod = MergingJsonDataLoader.class.getDeclaredMethod("finishLoad", Map.class, ResourceManager.class);
      this.finishLoadMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Failed to find MergingJsonDataLoader methods", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T getFieldValue(Object obj, String fieldName, Class<T> type) {
    try {
      Field field = MergingJsonDataLoader.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(obj);
    } catch (Exception e) {
      throw new RuntimeException("Failed to access field " + fieldName, e);
    }
  }

  private void invokeParse(B builder, ResourceLocation id, JsonElement element) {
    try {
      parseMethod.invoke(dataLoader, builder, id, element);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke parse", e);
    }
  }

  private void invokeFinishLoad(Map<ResourceLocation, B> map, ResourceManager manager) {
    try {
      finishLoadMethod.invoke(dataLoader, map, manager);
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke finishLoad", e);
    }
  }

  /**
   * Loads and parses the relevant files into the data loader, accepting ResourceId arguments (e.g. MaterialId).
   * Converts ResourceId to ResourceLocation automatically.
   */
  public void loadAndParseFiles(@Nullable String mergeFolder, ResourceId... files) {
    loadAndParseFiles(mergeFolder, Arrays.stream(files).map(ResourceId::location).toArray(ResourceLocation[]::new));
  }

  /**
   * Loads and parses the relevant files into the data loader
   * @param mergeFolder  If nonnull, subfolder to load as a "second datapack", for testing merging behavior. If null, skips the merging
   * @param files  List of files
   */
  public void loadAndParseFiles(@Nullable String mergeFolder, ResourceLocation... files) {
    Map<ResourceLocation,B> parsedMap = new HashMap<>();
    for (Entry<ResourceLocation, JsonElement> entry : loadFilesAsSplashlist(files).entrySet()) {
      ResourceLocation id = entry.getKey();
      parsedMap.computeIfAbsent(id, loaderBuilderConstructor);
      invokeParse(parsedMap.get(id), id, entry.getValue());
    }
    if (mergeFolder != null) {
      JsonFileLoader fakeSecondDataPack = new JsonFileLoader(loaderGson, loaderFolder + "/" + mergeFolder);
      for (Entry<ResourceLocation, JsonElement> entry : fakeSecondDataPack.loadFilesAsSplashlist(files).entrySet()) {
        ResourceLocation id = entry.getKey();
        parsedMap.computeIfAbsent(id, loaderBuilderConstructor);
        invokeParse(parsedMap.get(id), id, entry.getValue());
      }
    }
    invokeFinishLoad(parsedMap, mock(ResourceManager.class));
  }
}

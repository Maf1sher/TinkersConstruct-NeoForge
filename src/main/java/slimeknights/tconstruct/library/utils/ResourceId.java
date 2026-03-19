package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Helper for type safety in IDs. Uses composition instead of inheritance since ResourceLocation is final in 1.21.1.
 * @see IdParser
 */
public abstract class ResourceId {
  private final ResourceLocation location;

  protected ResourceId(ResourceLocation location) {
    this.location = location;
  }

  protected ResourceId(String namespace, String path) {
    this(ResourceLocation.fromNamespaceAndPath(namespace, path));
  }

  protected ResourceId(String value) {
    this(ResourceLocation.parse(value));
  }

  /** Gets the wrapped ResourceLocation */
  public ResourceLocation location() {
    return location;
  }

  public String getNamespace() {
    return location.getNamespace();
  }

  public String getPath() {
    return location.getPath();
  }

  /** Creates a ResourceLocation with a suffix appended to the path */
  public ResourceLocation withSuffix(String suffix) {
    return location.withSuffix(suffix);
  }

  /** Creates a ResourceLocation with a prefix prepended to the path */
  public ResourceLocation withPrefix(String prefix) {
    return location.withPrefix(prefix);
  }

  /** Creates a ResourceLocation with a different path */
  public ResourceLocation withPath(String path) {
    return location.withPath(path);
  }

  @Override
  public String toString() {
    return location.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof ResourceId rid) return location.equals(rid.location);
    if (o instanceof ResourceLocation rl) return location.equals(rl);
    return false;
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  /** Compare with another ResourceLocation */
  public int compareTo(ResourceLocation other) {
    return location.compareTo(other);
  }

  /** Compare with another ResourceId */
  public int compareTo(ResourceId other) {
    return location.compareTo(other.location);
  }


  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T> T tryParse(String string, BiFunction<String,String,T> constructor) {
    int i = string.indexOf(':');
    String namespace = i >= 0 ? string.substring(0, i) : "minecraft";
    String path = i >= 0 ? string.substring(i + 1) : string;
    return tryBuild(namespace, path, constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (ResourceLocation.isValidNamespace(namespace) && ResourceLocation.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}

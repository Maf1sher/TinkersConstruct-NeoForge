package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.registration.object.IdAwareObject;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Helper for use with our extensions of resource location for some type safety in IDs.
 * Wraps a {@link ResourceLocation} rather than extending it, as {@code ResourceLocation} is final in 1.21.1.
 * Equality, ordering, and string representation match {@code ResourceLocation} so these IDs stay drop-in compatible
 * with code and data treating them as plain resource locations.
 * @see IdParser
 */
public abstract class ResourceId implements Comparable<ResourceId>, IdAwareObject {
  /** Wrapped resource location */
  private final ResourceLocation location;

  protected ResourceId(String namespace, String path) {
    this.location = ResourceLocation.fromNamespaceAndPath(namespace, path);
  }

  public ResourceId(ResourceLocation location) {
    this.location = location;
  }

  public ResourceId(String location) {
    this.location = ResourceLocation.parse(location);
  }

  /** {@return the wrapped resource location} */
  public ResourceLocation getLocation() {
    return location;
  }

  /** {@return the wrapped resource location, for compatibility with {@link IdAwareObject}} */
  @Override
  public ResourceLocation id() {
    return location;
  }

  /** {@return namespace, equivalent to the wrapped location's namespace} */
  public String getNamespace() {
    return location.getNamespace();
  }

  /** {@return path, equivalent to the wrapped location's path} */
  public String getPath() {
    return location.getPath();
  }

  /** {@return the translation key for this ID, of the form {@code namespace.path}} */
  public String toLanguageKey() {
    return toLanguageKey(location);
  }

  /** {@return the translation key for the given location, of the form {@code namespace.path}} */
  public static String toLanguageKey(ResourceLocation location) {
    return location.getNamespace() + "." + location.getPath();
  }

  /** {@return a resource location with the given suffix appended to this ID's path} */
  public ResourceLocation withSuffix(String suffix) {
    return location.withSuffix(suffix);
  }

  /** {@return a resource location with the given prefix prepended to this ID's path} */
  public ResourceLocation withPrefix(String prefix) {
    return location.withPrefix(prefix);
  }

  @Override
  public String toString() {
    return location.toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof ResourceId otherId) {
      return location.equals(otherId.location);
    }
    return location.equals(other);
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public int compareTo(ResourceId other) {
    return location.compareTo(other.location);
  }

  /** Compares against a plain resource location, matching the old ordering when these classes extended it */
  public int compareTo(ResourceLocation other) {
    return location.compareTo(other);
  }


  /* Helpers for static constructors */

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String,String,T> constructor) {
    String[] parts = IdParser.decompose("minecraft", string, ':');
    return tryBuild(parts[0], parts[1], constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String,String,T> constructor) {
    if (ResourceLocation.isValidNamespace(namespace) && ResourceLocation.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}

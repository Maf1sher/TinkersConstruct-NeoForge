package slimeknights.tconstruct.library.modifiers;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.utils.IdParser;
import slimeknights.tconstruct.library.utils.ResourceId;

import javax.annotation.Nullable;

/**
 * This is just a copy of ResourceLocation for type safety in modifier JSON.
 */
public class ModifierId extends ResourceId {
  public static final IdParser<ModifierId> PARSER = new IdParser<>(ModifierId::new, "Modifier");
  /** ID of the default modifier. Used in a few contexts to indicate "no modifier" instead of using null. */
  public static final ModifierId EMPTY = new ModifierId(TConstruct.MOD_ID, "empty");

  public ModifierId(String resourceName) {
    super(resourceName);
  }

  public ModifierId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public ModifierId(ResourceLocation location) {
    super(location);
  }

  /** {@return Modifier ID, or null if invalid} */
  @Nullable
  public static ModifierId tryParse(String string) {
    return ResourceId.tryParse(string, ModifierId::new);
  }

  /** {@return Modifier ID, or null if invalid} */
  @Nullable
  public static ModifierId tryBuild(String namespace, String path) {
    return ResourceId.tryBuild(namespace, path, ModifierId::new);
  }
}

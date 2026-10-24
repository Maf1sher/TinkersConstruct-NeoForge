package slimeknights.tconstruct.library.tools.capability;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Capability to allow an entity to store modifiers, used on projectiles fired from modifiable items */
public class EntityModifierCapability {
  /** Default instance to use with orElse */
  public static final EntityModifiers EMPTY = new EntityModifiers() {
    @Override
    public ModifierNBT getModifiers() {
      return ModifierNBT.EMPTY;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {}

    @Override
    public void addModifiers(ModifierNBT nbt) {}
  };

  private EntityModifierCapability() {}

  /* Static helpers */

  /** List of predicates to check if the entity supports this capability */
  private static final List<Predicate<Entity>> ENTITY_PREDICATES = new ArrayList<>();

  /** Attachment storing projectile modifiers, serialized so entity state round-trips like the old capability */
  public static final DeferredHolder<?, AttachmentType<Attachment>> DATA = TinkerModule.ATTACHMENTS.register(
    "entity_modifiers", () -> AttachmentType.serializable(Attachment::new).build());

  /** Gets the capability for the entity. Always returns a mutable instance, unlike the old capability that could be missing. */
  public static EntityModifiers getCapability(Entity entity) {
    return entity.getData(DATA);
  }

  /** Gets the data or an empty instance if missing */
  public static ModifierNBT getOrEmpty(Entity entity) {
    return entity.getData(DATA).getModifiers();
  }

  /** Checks if the given entity supports this capability */
  public static boolean supportCapability(Entity entity) {
    for (Predicate<Entity> entityPredicate : ENTITY_PREDICATES) {
      if (entityPredicate.test(entity)) {
        return true;
      }
    }
    return false;
  }

  /** Registers a predicate of entites that need this capability */
  public static void registerEntityPredicate(Predicate<Entity> predicate) {
    ENTITY_PREDICATES.add(predicate);
  }

  /** Attachment instance holding modifiers */
  private static class Attachment implements EntityModifiers, INBTSerializable<ListTag> {
    @Getter @Setter
    private ModifierNBT modifiers = ModifierNBT.EMPTY;

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
      return modifiers.serializeToNBT();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
      modifiers = ModifierNBT.readFromNBT(nbt);
    }
  }

  /** Interface for callers to use */
  public interface EntityModifiers {
    /** Gets the stored modifiers */
    ModifierNBT getModifiers();

    /** Sets the stored modifiers */
    void setModifiers(ModifierNBT nbt);

    /** Adds additional modifiers to the stored modifiers */
    default void addModifiers(ModifierNBT nbt) {
      ModifierNBT existing = getModifiers();
      if (existing.isEmpty()) {
        setModifiers(nbt);
      } else {
        setModifiers(ModifierNBT.builder().add(existing).add(nbt).build());
      }
    }
  }
}

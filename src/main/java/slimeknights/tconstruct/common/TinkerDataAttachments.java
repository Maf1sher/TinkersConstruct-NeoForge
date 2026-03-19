package slimeknights.tconstruct.common;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability.EntityModifiers;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.function.Supplier;

/** Central registration of data attachment types for TConstruct */
public class TinkerDataAttachments {
  public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TConstruct.MOD_ID);

  /** Persistent modifier data stored on living entities and projectiles. Persists on death. */
  public static final Supplier<AttachmentType<ModDataNBT>> PERSISTENT_DATA =
      ATTACHMENT_TYPES.register("persistent_data", () ->
          AttachmentType.builder(ModDataNBT::new)
              .serialize(new IAttachmentSerializer<CompoundTag, ModDataNBT>() {
                @Override
                public ModDataNBT read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                  return ModDataNBT.readFromNBT(tag);
                }

                @Nullable
                @Override
                public CompoundTag write(ModDataNBT data, HolderLookup.Provider provider) {
                  CompoundTag tag = data.getCopy();
                  return tag.isEmpty() ? null : tag;
                }
              })
              .copyOnDeath()
              .build());

  /** Modifier data stored on projectile entities */
  public static final Supplier<AttachmentType<EntityModifiers>> ENTITY_MODIFIERS =
      ATTACHMENT_TYPES.register("entity_modifiers", () ->
          AttachmentType.<EntityModifiers>builder(() -> new SimpleEntityModifiers())
              .serialize(new IAttachmentSerializer<ListTag, EntityModifiers>() {
                @Override
                public EntityModifiers read(IAttachmentHolder holder, ListTag tag, HolderLookup.Provider provider) {
                  SimpleEntityModifiers result = new SimpleEntityModifiers();
                  result.setModifiers(ModifierNBT.readFromNBT(tag));
                  return result;
                }

                @Nullable
                @Override
                public ListTag write(EntityModifiers data, HolderLookup.Provider provider) {
                  ModifierNBT modifiers = data.getModifiers();
                  return modifiers.isEmpty() ? null : modifiers.serializeToNBT();
                }
              })
              .build());

  /** Simple mutable implementation of EntityModifiers for data attachments */
  private static class SimpleEntityModifiers implements EntityModifiers {
    private ModifierNBT modifiers = ModifierNBT.EMPTY;

    @Override
    public ModifierNBT getModifiers() {
      return modifiers;
    }

    @Override
    public void setModifiers(ModifierNBT nbt) {
      this.modifiers = nbt;
    }
  }
}

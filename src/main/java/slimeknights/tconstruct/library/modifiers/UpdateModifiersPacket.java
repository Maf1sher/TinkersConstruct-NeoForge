package slimeknights.tconstruct.library.modifiers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.DecoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.library.modifiers.impl.ComposableModifier;
import slimeknights.tconstruct.library.utils.GenericTagUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

/** Packet to sync modifiers */
@RequiredArgsConstructor
public class UpdateModifiersPacket implements TinkerPayload {
  public static final Type<UpdateModifiersPacket> TYPE = TinkerPayload.type("update_modifiers_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateModifiersPacket> CODEC = TinkerPayload.codec(UpdateModifiersPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateModifiersPacket> type() { return TYPE; }

  /** Collection of all modifiers */
  private final Map<ModifierId,Modifier> allModifiers;
  /** Map of all modifier tags */
  private final Map<TagKey<Modifier>,List<Modifier>> tags;
  /** Collection of non-redirect modifiers */
  private Collection<ComposableModifier> modifiers;
  /** Map of modifier redirect ID pairs */
  private Map<ModifierId,ModifierId> redirects;
  /** Map of enchantment to modifier pair */
  private final Map<Enchantment,Modifier> enchantmentMap;
  /** Collection of all enchantment tag mappings */
  private final Map<TagKey<Enchantment>, Modifier> enchantmentTagMappings;
  private static final StreamCodec<RegistryFriendlyByteBuf,Enchantment> ENCHANTMENT_CODEC = ByteBufCodecs.registry(Registries.ENCHANTMENT);

  /** Ensures both the modifiers and redirects lists are calculated, allows one packet to be used multiple times without redundant work */
  private void ensureCalculated() {
    if (this.modifiers == null || this.redirects == null) {
      ImmutableList.Builder<ComposableModifier> modifiers = ImmutableList.builder();
      ImmutableMap.Builder<ModifierId,ModifierId> redirects = ImmutableMap.builder();
      for (Entry<ModifierId,Modifier> entry : allModifiers.entrySet()) {
        ModifierId id = entry.getKey();
        Modifier value = entry.getValue();
        ModifierId actual = value.getId();
        if (id.equals(actual)) {
          // we can't sync anything that is not composable
          if (value instanceof ComposableModifier composable) {
            modifiers.add(composable);
          } else {
            TConstruct.LOG.warn("Unable to sync modifier {} as its not ComposableModifier; got class {}", id, value.getClass().getName());
          }
        } else {
          redirects.put(id, actual);
        }
      }
      this.modifiers = modifiers.build();
      this.redirects = redirects.build();
    }
  }

  /** Gets a modifier by the given ID, falling back to the map if needed */
  private static Modifier getModifier(Map<ModifierId,Modifier> modifiers, ModifierId id) {
    Modifier modifier = ModifierManager.INSTANCE.getStatic(id);
    if (modifier == ModifierManager.INSTANCE.getDefaultValue()) {
      modifier = modifiers.get(id);
      if (modifier == null) {
        throw new DecoderException("Unknown modifier " + id);
      }
    }
    return modifier;
  }

  public UpdateModifiersPacket(RegistryFriendlyByteBuf buffer) {
    // read in modifiers
    int size = buffer.readVarInt();
    Map<ModifierId,Modifier> modifiers = new HashMap<>();
    for (int i = 0; i < size; i++) {
      ModifierId id = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
      try {
        Modifier modifier = ComposableModifier.LOADER.decode(buffer, ModifierManager.contextBuilder(id.getLocation()).build());
        modifier.setId(id);
        modifiers.put(id, modifier);
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Failed to decode modifier with ID {}", id, e);
        throw e;
      }
    }
    // read in redirects
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      ModifierId from = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
      modifiers.put(from, getModifier(modifiers, new ModifierId(buffer.readUtf(Short.MAX_VALUE))));
    }
    this.allModifiers = modifiers;
    this.tags = GenericTagUtil.decodeTags(buffer, ModifierManager.REGISTRY_KEY, id -> getModifier(modifiers, new ModifierId(id)));

    // read in enchantment to modifier mapping
    ImmutableMap.Builder<Enchantment,Modifier> enchantmentBuilder = ImmutableMap.builder();
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      enchantmentBuilder.put(
          ENCHANTMENT_CODEC.decode(buffer),
        getModifier(modifiers, new ModifierId(buffer.readResourceLocation())));
    }
    enchantmentMap = enchantmentBuilder.build();
    ImmutableMap.Builder<TagKey<Enchantment>, Modifier> enchantmentTagBuilder = ImmutableMap.builder();
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      enchantmentTagBuilder.put(
        TagKey.create(Registries.ENCHANTMENT, buffer.readResourceLocation()),
        getModifier(modifiers, new ModifierId(buffer.readResourceLocation())));
    }
    enchantmentTagMappings = enchantmentTagBuilder.build();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    ensureCalculated();
    // write modifiers
    buffer.writeVarInt(modifiers.size());
    for (ComposableModifier modifier : modifiers) {
      ResourceLocation id = modifier.getId().getLocation();
      buffer.writeResourceLocation(id);
      try {
        ComposableModifier.LOADER.encode(buffer, modifier);
      } catch (RuntimeException e) {
        // improve error logging
        TConstruct.LOG.error("Failed to encode modifier with ID {}", id, e);
        throw e;
      }
    }
    // write redirects
    buffer.writeVarInt(redirects.size());
    for (Entry<ModifierId,ModifierId> entry : redirects.entrySet()) {
      buffer.writeResourceLocation(entry.getKey().getLocation());
      buffer.writeResourceLocation(entry.getValue().getLocation());
    }
    GenericTagUtil.encodeTags(buffer, Modifier::id, this.tags);

    // enchantment mapping
    buffer.writeVarInt(enchantmentMap.size());
    for (Entry<Enchantment,Modifier> entry : enchantmentMap.entrySet()) {
      ENCHANTMENT_CODEC.encode(buffer, entry.getKey());
      buffer.writeResourceLocation(entry.getValue().getId().getLocation());
    }
    buffer.writeVarInt(enchantmentTagMappings.size());
    for (Entry<TagKey<Enchantment>, Modifier> entry : enchantmentTagMappings.entrySet()) {
      buffer.writeResourceLocation(entry.getKey().location());
      buffer.writeResourceLocation(entry.getValue().getId().getLocation());
    }
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      ModifierManager.INSTANCE.updateModifiersFromServer(allModifiers, tags, enchantmentMap, enchantmentTagMappings);
    });
  }
}

package slimeknights.tconstruct.library.materials.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.utils.GenericTagUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialsPacket implements TinkerPayload {
  public static final Type<UpdateMaterialsPacket> TYPE = TinkerPayload.type("update_materials_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateMaterialsPacket> CODEC = TinkerPayload.codec(UpdateMaterialsPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateMaterialsPacket> type() { return TYPE; }

  private final Map<MaterialId,IMaterial> materials;
  private final Map<MaterialId,MaterialId> redirects;
  private final Map<TagKey<IMaterial>,List<IMaterial>> tags;

  public UpdateMaterialsPacket(RegistryFriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    ImmutableMap.Builder<MaterialId,IMaterial> materials = ImmutableMap.builder();

    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      int tier = buffer.readVarInt();
      int sortOrder = buffer.readVarInt();
      Rarity rarity = buffer.readEnum(Rarity.class);
      boolean craftable = buffer.readBoolean();
      boolean hidden = buffer.readBoolean();
      materials.put(id, new Material(id.getLocation(), tier, sortOrder, rarity, craftable, hidden));
    }
    this.materials = materials.build();
    // process redirects
    int redirectCount = buffer.readVarInt();
    if (redirectCount == 0) {
      this.redirects = Collections.emptyMap();
    } else {
      this.redirects = new HashMap<>(redirectCount);
      for (int i = 0; i < redirectCount; i++) {
        this.redirects.put(new MaterialId(buffer.readUtf()), new MaterialId(buffer.readUtf()));
      }
    }
    this.tags = GenericTagUtil.decodeTags(buffer, MaterialManager.REGISTRY_KEY, id -> this.materials.get(new MaterialId(id)));
  }

  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeInt(this.materials.size());
    for (IMaterial material : this.materials.values()) {
      buffer.writeResourceLocation(material.getIdentifier().getLocation());
      buffer.writeVarInt(material.getTier());
      buffer.writeVarInt(material.getSortOrder());
      buffer.writeEnum(material.getRarity());
      buffer.writeBoolean(material.isCraftable());
      buffer.writeBoolean(material.isHidden());
    }
    buffer.writeVarInt(this.redirects.size());
    for (Entry<MaterialId,MaterialId> entry : this.redirects.entrySet()) {
      buffer.writeUtf(entry.getKey().toString());
      buffer.writeUtf(entry.getValue().toString());
    }
    GenericTagUtil.encodeTags(buffer, IMaterial::getIdentifier, this.tags);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      MaterialRegistry.updateMaterialsFromServer(this);
    });
  }
}

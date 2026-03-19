package slimeknights.tconstruct.library.materials.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.utils.GenericTagUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialsPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateMaterialsPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_materials"));
  public static final StreamCodec<FriendlyByteBuf, UpdateMaterialsPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateMaterialsPacket::encode, UpdateMaterialsPacket::new);

  private final Map<MaterialId,IMaterial> materials;
  private final Map<MaterialId,MaterialId> redirects;
  private final Map<TagKey<IMaterial>,List<IMaterial>> tags;

  public UpdateMaterialsPacket(FriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    ImmutableMap.Builder<MaterialId,IMaterial> materials = ImmutableMap.builder();

    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      int tier = buffer.readVarInt();
      int sortOrder = buffer.readVarInt();
      boolean craftable = buffer.readBoolean();
      boolean hidden = buffer.readBoolean();
      materials.put(id, new Material(id, tier, sortOrder, craftable, hidden));
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

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(this.materials.size());
    this.materials.values().forEach(material -> {
      buffer.writeResourceLocation(material.getIdentifier().location());
      buffer.writeVarInt(material.getTier());
      buffer.writeVarInt(material.getSortOrder());
      buffer.writeBoolean(material.isCraftable());
      buffer.writeBoolean(material.isHidden());
    });
    buffer.writeVarInt(this.redirects.size());
    this.redirects.forEach((key, value) -> {
      buffer.writeUtf(key.toString());
      buffer.writeUtf(value.toString());
    });
    GenericTagUtil.encodeTags(buffer, material -> material.getIdentifier().location(), this.tags);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateMaterialsPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> MaterialRegistry.updateMaterialsFromServer(payload));
  }
}

package slimeknights.tconstruct.library.materials.traits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialTraitsPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateMaterialTraitsPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_material_traits"));
  public static final StreamCodec<FriendlyByteBuf, UpdateMaterialTraitsPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateMaterialTraitsPacket::encode, UpdateMaterialTraitsPacket::new);

  protected final Map<MaterialId,MaterialTraits> materialToTraits;

  public UpdateMaterialTraitsPacket(FriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    materialToTraits = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      MaterialTraits traits = MaterialTraits.read(buffer);
      materialToTraits.put(id, traits);
    }
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(materialToTraits.size());
    materialToTraits.forEach((materialId, traits) -> {
      buffer.writeResourceLocation(materialId.location());
      traits.write(buffer);
    });
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateMaterialTraitsPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> MaterialRegistry.updateMaterialTraitsFromServer(payload));
  }
}

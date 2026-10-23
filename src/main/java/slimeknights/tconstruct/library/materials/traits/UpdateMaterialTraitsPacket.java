package slimeknights.tconstruct.library.materials.traits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialTraitsPacket implements TinkerPayload {
  public static final Type<UpdateMaterialTraitsPacket> TYPE = TinkerPayload.type("update_material_traits_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateMaterialTraitsPacket> CODEC = TinkerPayload.codec(UpdateMaterialTraitsPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateMaterialTraitsPacket> type() { return TYPE; }

  protected final Map<MaterialId,MaterialTraits> materialToTraits;

  public UpdateMaterialTraitsPacket(RegistryFriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    materialToTraits = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      MaterialTraits traits = MaterialTraits.read(buffer);
      materialToTraits.put(id, traits);
    }
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeInt(materialToTraits.size());
    materialToTraits.forEach((materialId, traits) -> {
      buffer.writeResourceLocation(materialId);
      traits.write(buffer);
    });
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      MaterialRegistry.updateMaterialTraitsFromServer(this);
    });
  }
}

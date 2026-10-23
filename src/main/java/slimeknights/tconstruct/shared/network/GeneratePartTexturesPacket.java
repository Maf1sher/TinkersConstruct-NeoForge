package slimeknights.tconstruct.shared.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.shared.client.ClientGeneratePartTexturesCommand;

/** Packet to tell the client to generate tool textures */
@RequiredArgsConstructor
public class GeneratePartTexturesPacket implements TinkerPayload {
  public static final Type<GeneratePartTexturesPacket> TYPE = TinkerPayload.type("generate_part_textures_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,GeneratePartTexturesPacket> CODEC = TinkerPayload.codec(GeneratePartTexturesPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<GeneratePartTexturesPacket> type() { return TYPE; }

  private final Operation operation;
  private final String modId;
  private final String materialPath;

  public GeneratePartTexturesPacket(RegistryFriendlyByteBuf buffer) {
    operation = buffer.readEnum(Operation.class);
    modId = buffer.readUtf(Short.MAX_VALUE);
    materialPath = buffer.readUtf(Short.MAX_VALUE);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeEnum(operation);
    buffer.writeUtf(modId);
    buffer.writeUtf(materialPath);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      context.enqueueWork(() -> ClientGeneratePartTexturesCommand.generateTextures(operation, modId, materialPath));
    });
  }

  public enum Operation { ALL, MISSING }
}

package slimeknights.tconstruct.shared.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.client.ClientGeneratePartTexturesCommand;

/** Packet to tell the client to generate tool textures */
@RequiredArgsConstructor
public class GeneratePartTexturesPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<GeneratePartTexturesPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("generate_part_textures"));
  public static final StreamCodec<FriendlyByteBuf, GeneratePartTexturesPacket> STREAM_CODEC = StreamCodec.ofMember(GeneratePartTexturesPacket::encode, GeneratePartTexturesPacket::new);

  private final Operation operation;
  private final String modId;
  private final String materialPath;

  public GeneratePartTexturesPacket(FriendlyByteBuf buffer) {
    operation = buffer.readEnum(Operation.class);
    modId = buffer.readUtf(Short.MAX_VALUE);
    materialPath = buffer.readUtf(Short.MAX_VALUE);
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(operation);
    buffer.writeUtf(modId);
    buffer.writeUtf(materialPath);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(GeneratePartTexturesPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> ClientGeneratePartTexturesCommand.generateTextures(payload.operation, payload.modId, payload.materialPath));
  }

  public enum Operation { ALL, MISSING }
}

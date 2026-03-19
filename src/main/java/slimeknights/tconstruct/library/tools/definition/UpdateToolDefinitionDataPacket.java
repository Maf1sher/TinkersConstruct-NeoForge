package slimeknights.tconstruct.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;

import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync tool definitions to the client */
@RequiredArgsConstructor
public class UpdateToolDefinitionDataPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateToolDefinitionDataPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_tool_definition_data"));
  public static final StreamCodec<FriendlyByteBuf, UpdateToolDefinitionDataPacket> STREAM_CODEC = StreamCodec.ofMember(UpdateToolDefinitionDataPacket::encode, UpdateToolDefinitionDataPacket::new);

  @Getter(AccessLevel.PROTECTED)
  private final Map<ResourceLocation, ToolDefinitionData> dataMap;

  public UpdateToolDefinitionDataPacket(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableMap.Builder<ResourceLocation, ToolDefinitionData> builder = ImmutableMap.builder();
    for (int i = 0; i < size; i++) {
      ResourceLocation name = buffer.readResourceLocation();
      ToolDefinitionData data = ToolDefinitionData.LOADABLE.decode(buffer, ToolDefinitionLoader.contextBuilder(name).build());
      builder.put(name, data);
    }
    dataMap = builder.build();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(dataMap.size());
    for (Entry<ResourceLocation, ToolDefinitionData> entry : dataMap.entrySet()) {
      buffer.writeResourceLocation(entry.getKey());
      ToolDefinitionData.LOADABLE.encode(buffer, entry.getValue());
    }
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateToolDefinitionDataPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> ToolDefinitionLoader.getInstance().updateDataFromServer(payload.dataMap));
  }
}

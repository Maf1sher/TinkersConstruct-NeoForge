package slimeknights.tconstruct.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerPayload;

import java.util.Map.Entry;
import java.util.Map;

/** Packet to sync tool definitions to the client */
@RequiredArgsConstructor
public class UpdateToolDefinitionDataPacket implements TinkerPayload {
  public static final Type<UpdateToolDefinitionDataPacket> TYPE = TinkerPayload.type("update_tool_definition_data_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,UpdateToolDefinitionDataPacket> CODEC = TinkerPayload.codec(UpdateToolDefinitionDataPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<UpdateToolDefinitionDataPacket> type() { return TYPE; }

  @Getter(AccessLevel.PROTECTED)
  private final Map<ResourceLocation, ToolDefinitionData> dataMap;

  public UpdateToolDefinitionDataPacket(RegistryFriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableMap.Builder<ResourceLocation, ToolDefinitionData> builder = ImmutableMap.builder();
    for (int i = 0; i < size; i++) {
      ResourceLocation name = buffer.readResourceLocation();
      try {
        ToolDefinitionData data = ToolDefinitionData.LOADABLE.decode(buffer, ToolDefinitionLoader.contextBuilder(name).build());
        builder.put(name, data);
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Failed to decode Tool Definition for {}", name, e);
        throw e;
      }
    }
    dataMap = builder.build();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeVarInt(dataMap.size());
    for (Entry<ResourceLocation, ToolDefinitionData> entry : dataMap.entrySet()) {
      ResourceLocation name = entry.getKey();
      buffer.writeResourceLocation(name);
      try {
        ToolDefinitionData.LOADABLE.encode(buffer, entry.getValue());
      } catch (RuntimeException e) {
        TConstruct.LOG.error("Failed to encode Tool Definition for {}", name, e);
        throw e;
      }
    }
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      ToolDefinitionLoader.getInstance().updateDataFromServer(dataMap);
    });
  }
}

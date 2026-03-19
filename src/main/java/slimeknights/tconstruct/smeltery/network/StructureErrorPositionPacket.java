package slimeknights.tconstruct.smeltery.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import javax.annotation.Nullable;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error
 */
@RequiredArgsConstructor
public class StructureErrorPositionPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<StructureErrorPositionPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("structure_error_position"));
  public static final StreamCodec<FriendlyByteBuf, StructureErrorPositionPacket> STREAM_CODEC = StreamCodec.ofMember(StructureErrorPositionPacket::encode, StructureErrorPositionPacket::new);

  private final BlockPos controllerPos;
  @Nullable
  private final BlockPos errorPos;

  public StructureErrorPositionPacket(FriendlyByteBuf buffer) {
    this.controllerPos = buffer.readBlockPos();
    if (buffer.readBoolean()) {
      this.errorPos = buffer.readBlockPos();
    } else {
      this.errorPos = null;
    }
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(controllerPos);
    if (errorPos != null) {
      buffer.writeBoolean(true);
      buffer.writeBlockPos(errorPos);
    } else {
      buffer.writeBoolean(false);
    }
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(StructureErrorPositionPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(payload));
  }

  private static class HandleClient {
    private static void handle(StructureErrorPositionPacket packet) {
      BlockEntityHelper.get(HeatingStructureBlockEntity.class, Minecraft.getInstance().level, packet.controllerPos)
                       .ifPresent(te -> te.setErrorPos(packet.errorPos));
    }
  }
}

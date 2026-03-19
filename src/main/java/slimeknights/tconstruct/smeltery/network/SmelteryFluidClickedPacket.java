package slimeknights.tconstruct.smeltery.network;

import lombok.AllArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
@AllArgsConstructor
public class SmelteryFluidClickedPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<SmelteryFluidClickedPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("smeltery_fluid_clicked"));
  public static final StreamCodec<FriendlyByteBuf, SmelteryFluidClickedPacket> STREAM_CODEC = StreamCodec.ofMember(SmelteryFluidClickedPacket::encode, SmelteryFluidClickedPacket::new);

  private final int index;

  public SmelteryFluidClickedPacket(FriendlyByteBuf buffer) {
    index = buffer.readVarInt();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(index);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(SmelteryFluidClickedPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      ServerPlayer sender = (ServerPlayer) context.player();
      if (!sender.isSpectator()) {
        AbstractContainerMenu container = sender.containerMenu;
        if (container instanceof BaseContainerMenu<?> base && base.getTile() instanceof ISmelteryTankHandler tank) {
          tank.getTank().moveFluidToBottom(payload.index);
        }
      }
    });
  }
}

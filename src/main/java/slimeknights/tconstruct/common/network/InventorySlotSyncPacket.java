package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.TConstruct;

public class InventorySlotSyncPacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<InventorySlotSyncPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("inventory_slot_sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf, InventorySlotSyncPacket> STREAM_CODEC = StreamCodec.ofMember(InventorySlotSyncPacket::encode, InventorySlotSyncPacket::new);

  public final ItemStack itemStack;
  public final int slot;
  public final BlockPos pos;

  public InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.pos = pos;
  }

  public InventorySlotSyncPacket(RegistryFriendlyByteBuf buffer) {
    this.itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    this.slot = buffer.readShort();
    this.pos = buffer.readBlockPos();
  }

  public void encode(RegistryFriendlyByteBuf packetBuffer) {
    ItemStack.OPTIONAL_STREAM_CODEC.encode(packetBuffer, this.itemStack);
    packetBuffer.writeShort(this.slot);
    packetBuffer.writeBlockPos(this.pos);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(InventorySlotSyncPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(payload));
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(InventorySlotSyncPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntity blockEntity = world.getBlockEntity(packet.pos);
        if (blockEntity instanceof Container container && packet.slot >= 0 && packet.slot < container.getContainerSize()) {
          container.setItem(packet.slot, packet.itemStack);
          //noinspection ConstantConditions
          Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
          return;
        }

        IItemHandlerModifiable cap = world.getCapability(Capabilities.ItemHandler.BLOCK, packet.pos, null) instanceof IItemHandlerModifiable modifiable ? modifiable : null;
        if (cap != null && packet.slot >= 0 && packet.slot < cap.getSlots()) {
          cap.setStackInSlot(packet.slot, packet.itemStack);
          //noinspection ConstantConditions
          Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
        }
      }
    }
  }
}

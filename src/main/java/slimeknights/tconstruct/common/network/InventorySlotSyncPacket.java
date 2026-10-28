package slimeknights.tconstruct.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class InventorySlotSyncPacket implements TinkerPayload {
  public static final Type<InventorySlotSyncPacket> TYPE = TinkerPayload.type("inventory_slot_sync_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,InventorySlotSyncPacket> CODEC = TinkerPayload.codec(InventorySlotSyncPacket::new, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<InventorySlotSyncPacket> type() { return TYPE; }


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

  @Override
  public void encode(RegistryFriendlyByteBuf packetBuffer) {
    ItemStack.OPTIONAL_STREAM_CODEC.encode(packetBuffer, this.itemStack);
    packetBuffer.writeShort(this.slot);
    packetBuffer.writeBlockPos(this.pos);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      HandleClient.handle(this);
    });
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(InventorySlotSyncPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        IItemHandler cap = world.getCapability(Capabilities.ItemHandler.BLOCK, packet.pos, null);
        if (cap instanceof IItemHandlerModifiable modifiable) {
          modifiable.setStackInSlot(packet.slot, packet.itemStack);
          //noinspection ConstantConditions
          Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
        }
      }
    }
  }
}

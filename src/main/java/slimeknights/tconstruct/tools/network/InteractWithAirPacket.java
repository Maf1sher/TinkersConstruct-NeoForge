package slimeknights.tconstruct.tools.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.network.TinkerPayload;
import slimeknights.tconstruct.tools.logic.InteractionHandler;

/** Packet sent client to server when an empty hand interaction */
@RequiredArgsConstructor
public enum InteractWithAirPacket implements TinkerPayload {
  /** Right click with an empty main hand and a chestplate */
  MAINHAND(InteractionHand.MAIN_HAND),
  /** Right click with an empty off hand and a chestplate */
  OFFHAND(InteractionHand.OFF_HAND),
  /** Left click with a supported tool */
  LEFT_CLICK(InteractionHand.MAIN_HAND);
  public static final Type<InteractWithAirPacket> TYPE = TinkerPayload.type("interact_with_air_packet");
  public static final StreamCodec<RegistryFriendlyByteBuf,InteractWithAirPacket> CODEC = TinkerPayload.codec(InteractWithAirPacket::read, (buffer, packet) -> packet.encode(buffer));
  @Override public Type<InteractWithAirPacket> type() { return TYPE; }


  private final InteractionHand hand;

  /** Gets the packet for the given hand */
  public static InteractWithAirPacket fromChestplate(InteractionHand hand) {
    return hand == InteractionHand.OFF_HAND ? OFFHAND : MAINHAND;
  }

  /** Gets the packet from the packet buffer */
  public static InteractWithAirPacket read(RegistryFriendlyByteBuf buffer) {
    return buffer.readEnum(InteractWithAirPacket.class);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer) {
    buffer.writeEnum(this);
  }

  @Override
  public void handle(IPayloadContext context) {
    enqueue(context, () -> {
      if (context.player() instanceof ServerPlayer player && !player.isSpectator()) {
        if (this == LEFT_CLICK) {
          ItemStack held = player.getItemInHand(hand);
          if (held.is(TinkerTags.Items.INTERACTABLE_LEFT)) {
            InteractionResult result = InteractionHandler.onLeftClickInteraction(player, held, hand);
            if (result.shouldSwing()) {
              player.swing(hand, true);
            }
          }
        } else {
          ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
          if (chestplate.is(TinkerTags.Items.INTERACTABLE_ARMOR) && player.getItemInHand(hand).isEmpty()) {
            InteractionResult result = InteractionHandler.onChestplateUse(player, chestplate, hand);
            if (result.shouldSwing()) {
              player.swing(hand, true);
            }
          }
        }
      }
    });
  }
}

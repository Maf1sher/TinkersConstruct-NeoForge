package slimeknights.tconstruct.tables.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

/**
 * Packet to send the current crafting recipe to a player who opens the crafting station
 */
public class UpdateCraftingRecipePacket implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<UpdateCraftingRecipePacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("update_crafting_recipe"));
  public static final StreamCodec<FriendlyByteBuf, UpdateCraftingRecipePacket> STREAM_CODEC = StreamCodec.ofMember(UpdateCraftingRecipePacket::encode, UpdateCraftingRecipePacket::new);

  private final BlockPos pos;
  private final ResourceLocation recipe;
  public UpdateCraftingRecipePacket(BlockPos pos, ResourceLocation recipe) {
    this.pos = pos;
    this.recipe = recipe;
  }

  public UpdateCraftingRecipePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.recipe = buffer.readResourceLocation();
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    buffer.writeResourceLocation(recipe);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(UpdateCraftingRecipePacket payload, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(payload));
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    @SuppressWarnings("unchecked")
    private static void handle(UpdateCraftingRecipePacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        BlockEntityHelper.get(CraftingStationBlockEntity.class, world, packet.pos).ifPresent(te ->
          world.getRecipeManager().byKey(packet.recipe)
            .filter(holder -> holder.value() instanceof CraftingRecipe)
            .map(holder -> (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) holder)
            .ifPresent(te::updateRecipe));
      }
    }
  }
}

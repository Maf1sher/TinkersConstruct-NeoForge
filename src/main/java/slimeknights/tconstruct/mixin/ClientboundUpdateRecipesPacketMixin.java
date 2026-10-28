package slimeknights.tconstruct.mixin;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.mantle.data.loadable.field.ContextKey;

/**
 * Passes the recipe ID to the recipe stream codec when recipes are synced to the client. The ID is written by the
 * {@link RecipeHolder} codec, but never reaches {@link slimeknights.mantle.network.codec.StreamableCodec}.
 */
@Mixin(ClientboundUpdateRecipesPacket.class)
public class ClientboundUpdateRecipesPacketMixin {
  /** Replaces the holder codec with one that exposes the recipe ID while the recipe payload is decoded */
  @Redirect(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/RecipeHolder;STREAM_CODEC:Lnet/minecraft/network/codec/StreamCodec;"))
  private static StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<?>> tconstruct$wrapRecipeCodec() {
    return new StreamCodec<>() {
      @Override
      public RecipeHolder<?> decode(RegistryFriendlyByteBuf buffer) {
        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
        ResourceLocation previous = ContextKey.CURRENT_RECIPE_ID.get();
        ContextKey.CURRENT_RECIPE_ID.set(id);
        try {
          return new RecipeHolder<>(id, Recipe.STREAM_CODEC.decode(buffer));
        } finally {
          ContextKey.CURRENT_RECIPE_ID.set(previous);
        }
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buffer, RecipeHolder<?> holder) {
        ResourceLocation.STREAM_CODEC.encode(buffer, holder.id());
        ResourceLocation previous = ContextKey.CURRENT_RECIPE_ID.get();
        ContextKey.CURRENT_RECIPE_ID.set(holder.id());
        try {
          Recipe.STREAM_CODEC.encode(buffer, holder.value());
        } finally {
          ContextKey.CURRENT_RECIPE_ID.set(previous);
        }
      }
    };
  }
}

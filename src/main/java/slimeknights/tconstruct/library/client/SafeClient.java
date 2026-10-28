package slimeknights.tconstruct.library.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;

/**
 * This class contains various methods that are safe to call on both sides, which internally call client only code.
 */
public class SafeClient {
  /**
   * Gets the registry access of the running game for code paths that have no level or player in scope.
   * Item capabilities use a {@code Void} context, so they cannot receive the lookup through their arguments.
   * @return  Registry access of the current server, else of the client level, or null if no game is loaded
   */
  @Nullable
  public static HolderLookup.Provider registryAccess() {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server != null) {
      return server.registryAccess();
    }
    if (FMLEnvironment.dist == Dist.CLIENT) {
      return ClientOnly.registryAccess();
    }
    return null;
  }

  /** Variant of {@link #registryAccess()} for code that cannot proceed without a lookup */
  public static HolderLookup.Provider registryAccessOrThrow() {
    HolderLookup.Provider access = registryAccess();
    if (access == null) {
      throw new IllegalStateException("No registry access available, cannot parse tool data");
    }
    return access;
  }
  /**
   * Triggers a model update if needed for this tank block
   * @param be          Block entity instance
   * @param tank        Fluid tank instance
   * @param oldAmount   Old fluid amount
   * @param newAmount   New fluid amount
   */
  public static void updateFluidModel(BlockEntity be, FluidTank tank, int oldAmount, int newAmount) {
    if (FMLEnvironment.dist == Dist.CLIENT) {
      ClientOnly.updateFluidModel(be, tank, oldAmount, newAmount);
    }
  }

  /** This class is only ever loaded client side */
  private static class ClientOnly {
    /** @see SafeClient#registryAccess() */
    @Nullable
    public static HolderLookup.Provider registryAccess() {
      Level level = Minecraft.getInstance().level;
      return level == null ? null : level.registryAccess();
    }

    /** @see SafeClient#updateFluidModel(BlockEntity, FluidTank, int, int)  */
    public static void updateFluidModel(BlockEntity be, FluidTank tank, int oldAmount, int newAmount) {
      Level level = be.getLevel();
      if (level != null && level.isClientSide) {
        // if the amount change is bigger than a single increment, or we changed whether we have a fluid, update the world renderer
        BlockState state = be.getBlockState();
        if (oldAmount != newAmount) {
          be.requestModelDataUpdate();
          Minecraft.getInstance().levelRenderer.blockChanged(level, be.getBlockPos(), state, state, 3);
        }
      }
    }
  }
}

package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/**
 * Capability to store persistent NBT data on an entity. For players, this is automatically synced to the client on load, but not during gameplay.
 * Persists after death, will reassess if we need some data to not persist death
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Persistent entity data used by modifiers, including projectile state. */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModDataNBT>> DATA = TinkerModule.ATTACHMENTS.register(
    "persistent_data", () -> AttachmentType.serializable(ModDataNBT::new).copyOnDeath().build());

  /** Gets the data or warns if its missing */
  public static ModDataNBT getOrWarn(Entity entity) {
    return entity.getData(DATA);
  }

  /** Registers this capability */
  public static void register() {
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, PersistentDataCapability::playerRespawn);
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, PersistentDataCapability::playerChangeDimension);
    NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, PersistentDataCapability::playerLoggedIn);
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(player.getData(DATA).getCopy()), player);
  }

  /** sync caps when the player respawns/returns from the end */
  private static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player changes dimensions */
  private static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    sync(event.getEntity());
  }

  /** sync caps when the player logs in */
  private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    sync(event.getEntity());
  }

}

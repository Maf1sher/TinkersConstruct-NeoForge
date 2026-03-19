package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerDataAttachments;
import slimeknights.tconstruct.common.network.SyncPersistentDataPacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/**
 * Persistent NBT data stored on entities via data attachments.
 * For players, automatically synced to the client on load, but not during gameplay.
 * Persists after death via AttachmentType copyOnDeath.
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Gets the persistent data from the entity */
  public static ModDataNBT getOrWarn(Entity entity) {
    return entity.getData(TinkerDataAttachments.PERSISTENT_DATA.get());
  }

  /** Registers event listeners for syncing */
  public static void register() {
    // Data attachment handles serialization and copyOnDeath automatically.
    // We only need sync events for players.
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, PersistentDataCapability::playerRespawn);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerChangedDimensionEvent.class, PersistentDataCapability::playerChangeDimension);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedInEvent.class, PersistentDataCapability::playerLoggedIn);
  }

  /** Syncs the data to the given player */
  private static void sync(Player player) {
    if (player instanceof ServerPlayer serverPlayer) {
      ModDataNBT data = player.getData(TinkerDataAttachments.PERSISTENT_DATA.get());
      TinkerNetwork.getInstance().sendTo(new SyncPersistentDataPacket(data.getCopy()), serverPlayer);
    }
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

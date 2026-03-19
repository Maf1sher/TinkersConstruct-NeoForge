package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.WeakHashMap;

/** Capability logic for piggyback handler */
public class PiggybackCapability {
  private static final ResourceLocation ID = TConstruct.getResource("piggyback");
  public static final EntityCapability<PiggybackHandler, Void> CAPABILITY = EntityCapability.createVoid(ID, PiggybackHandler.class);

  /** Instances storage */
  private static final WeakHashMap<Player, PiggybackHandler> INSTANCES = new WeakHashMap<>();

  private PiggybackCapability() {}

  /** Registers entity capability provider */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerEntity(CAPABILITY, EntityType.PLAYER, (player, ctx) ->
        INSTANCES.computeIfAbsent((Player) player, p -> new PiggybackHandler(p)));
  }

  /** Gets the handler for the given player, or null */
  @Nullable
  public static PiggybackHandler get(Player player) {
    return player.getCapability(CAPABILITY);
  }
}

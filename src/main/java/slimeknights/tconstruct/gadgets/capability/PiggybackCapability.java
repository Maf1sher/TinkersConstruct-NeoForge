package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;

/** Capability logic */
public class PiggybackCapability {
  /** Capability queried on players */
  public static final EntityCapability<PiggybackHandler, @Nullable Void> PIGGYBACK =
    EntityCapability.createVoid(TConstruct.getResource("piggyback"), PiggybackHandler.class);

  /** Attachment storing each player's handler, lazily created so the sync cache persists between queries */
  public static final DeferredHolder<AttachmentType<?>, AttachmentType<PiggybackHandler>> DATA = TinkerModule.ATTACHMENTS.register(
    "piggyback", () -> AttachmentType.builder(holder -> new PiggybackHandler((Player) holder)).build());

  private PiggybackCapability() {}

  /** Registers the capability provider so players can answer piggyback queries */
  public static void register(RegisterCapabilitiesEvent event) {
    event.registerEntity(PIGGYBACK, EntityType.PLAYER, (entity, context) -> entity.getData(DATA));
  }
}

package slimeknights.tconstruct.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.tconstruct.library.materials.definition.UpdateMaterialsPacket;
import slimeknights.tconstruct.library.materials.stats.UpdateMaterialStatsPacket;
import slimeknights.tconstruct.library.materials.traits.UpdateMaterialTraitsPacket;
import slimeknights.tconstruct.library.modifiers.UpdateModifiersPacket;
import slimeknights.tconstruct.library.modifiers.fluid.UpdateFluidEffectsPacket;
import slimeknights.tconstruct.library.tools.definition.UpdateToolDefinitionDataPacket;
import slimeknights.tconstruct.library.tools.layout.UpdateTinkerSlotLayoutsPacket;
import slimeknights.tconstruct.shared.network.GeneratePartTexturesPacket;
import slimeknights.tconstruct.smeltery.network.ChannelFlowPacket;
import slimeknights.tconstruct.smeltery.network.FaucetActivationPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.network.SmelteryFluidClickedPacket;
import slimeknights.tconstruct.smeltery.network.SmelteryTankUpdatePacket;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.smeltery.network.StructureUpdatePacket;
import slimeknights.tconstruct.tables.network.StationTabPacket;
import slimeknights.tconstruct.tables.network.TinkerStationRenamePacket;
import slimeknights.tconstruct.tables.network.TinkerStationSelectionPacket;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;
import slimeknights.tconstruct.tables.network.UpdateStationScreenPacket;
import slimeknights.tconstruct.tables.network.UpdateTinkerStationRecipePacket;
import slimeknights.tconstruct.tools.network.EntityMovementChangePacket;
import slimeknights.tconstruct.tools.network.InteractWithAirPacket;
import slimeknights.tconstruct.tools.network.PushBlockRowPacket;
import slimeknights.tconstruct.tools.network.SyncProjectileModifiersPacket;
import slimeknights.tconstruct.tools.network.TinkerControlPacket;
import slimeknights.tconstruct.tools.network.ToolContainerFluidUpdatePacket;

import javax.annotation.Nullable;

/**
 * Base network class for all tinkers logic
 * <p>
 * In general, if you need to send packets you should use your own network class
 */
public class TinkerNetwork {
  private static final TinkerNetwork instance = new TinkerNetwork();

  /*
   * Network versions:
   * 1: 3.10.1 and before
   * 2: 3.10.2 - new material stat type; item removal
   * 3: 3.11.2+ - lost track of how much changed but its a lot
   * 4: 1.21.1 - NeoForge CustomPacketPayload migration
   */
  private static final String VERSION = "4";

  private TinkerNetwork() {}

  /** Gets the instance of the network */
  public static TinkerNetwork getInstance() {
    return instance;
  }

  /**
   * Called during mod construction to setup the network.
   * Kept for backward compatibility, but payload registration is now event-driven.
   */
  public static void setup() {
    // no-op: payload registration now happens via RegisterPayloadHandlersEvent
  }

  /**
   * Registers all network payloads for Tinkers' Construct
   */
  @SubscribeEvent
  public static void registerPayloads(RegisterPayloadHandlersEvent event) {
    final PayloadRegistrar registrar = event.registrar(VERSION);

    // shared - client
    registrar.playToClient(InventorySlotSyncPacket.TYPE, InventorySlotSyncPacket.STREAM_CODEC, InventorySlotSyncPacket::handle);
    registrar.playToClient(UpdateNeighborsPacket.TYPE, UpdateNeighborsPacket.STREAM_CODEC, UpdateNeighborsPacket::handle);
    registrar.playToClient(GeneratePartTexturesPacket.TYPE, GeneratePartTexturesPacket.STREAM_CODEC, GeneratePartTexturesPacket::handle);
    registrar.playToClient(SyncPersistentDataPacket.TYPE, SyncPersistentDataPacket.STREAM_CODEC, SyncPersistentDataPacket::handle);

    // gadgets - client
    registrar.playToClient(EntityMovementChangePacket.TYPE, EntityMovementChangePacket.STREAM_CODEC, EntityMovementChangePacket::handle);

    // tables - server
    registrar.playToServer(StationTabPacket.TYPE, StationTabPacket.STREAM_CODEC, StationTabPacket::handle);
    registrar.playToServer(TinkerStationRenamePacket.TYPE, TinkerStationRenamePacket.STREAM_CODEC, TinkerStationRenamePacket::handle);
    registrar.playToServer(TinkerStationSelectionPacket.TYPE, TinkerStationSelectionPacket.STREAM_CODEC, TinkerStationSelectionPacket::handle);

    // tables - client
    registrar.playToClient(UpdateCraftingRecipePacket.TYPE, UpdateCraftingRecipePacket.STREAM_CODEC, UpdateCraftingRecipePacket::handle);
    registrar.playToClient(UpdateTinkerSlotLayoutsPacket.TYPE, UpdateTinkerSlotLayoutsPacket.STREAM_CODEC, UpdateTinkerSlotLayoutsPacket::handle);
    registrar.playToClient(UpdateStationScreenPacket.TYPE, UpdateStationScreenPacket.STREAM_CODEC, UpdateStationScreenPacket::handle);
    registrar.playToClient(UpdateTinkerStationRecipePacket.TYPE, UpdateTinkerStationRecipePacket.STREAM_CODEC, UpdateTinkerStationRecipePacket::handle);

    // tools - client
    registrar.playToClient(UpdateMaterialsPacket.TYPE, UpdateMaterialsPacket.STREAM_CODEC, UpdateMaterialsPacket::handle);
    registrar.playToClient(UpdateMaterialStatsPacket.TYPE, UpdateMaterialStatsPacket.STREAM_CODEC, UpdateMaterialStatsPacket::handle);
    registrar.playToClient(UpdateMaterialTraitsPacket.TYPE, UpdateMaterialTraitsPacket.STREAM_CODEC, UpdateMaterialTraitsPacket::handle);
    registrar.playToClient(UpdateToolDefinitionDataPacket.TYPE, UpdateToolDefinitionDataPacket.STREAM_CODEC, UpdateToolDefinitionDataPacket::handle);
    registrar.playToClient(ToolContainerFluidUpdatePacket.TYPE, ToolContainerFluidUpdatePacket.STREAM_CODEC, ToolContainerFluidUpdatePacket::handle);
    registrar.playToClient(SyncProjectileModifiersPacket.TYPE, SyncProjectileModifiersPacket.STREAM_CODEC, SyncProjectileModifiersPacket::handle);

    // modifiers - server
    registrar.playToServer(TinkerControlPacket.TYPE, TinkerControlPacket.STREAM_CODEC, TinkerControlPacket::handle);
    registrar.playToServer(InteractWithAirPacket.TYPE, InteractWithAirPacket.STREAM_CODEC, InteractWithAirPacket::handle);

    // modifiers - client
    registrar.playToClient(UpdateModifiersPacket.TYPE, UpdateModifiersPacket.STREAM_CODEC, UpdateModifiersPacket::handle);
    registrar.playToClient(UpdateFluidEffectsPacket.TYPE, UpdateFluidEffectsPacket.STREAM_CODEC, UpdateFluidEffectsPacket::handle);
    registrar.playToClient(PushBlockRowPacket.TYPE, PushBlockRowPacket.STREAM_CODEC, PushBlockRowPacket::handle);

    // smeltery - client
    registrar.playToClient(FluidUpdatePacket.TYPE, FluidUpdatePacket.STREAM_CODEC, FluidUpdatePacket::handle);
    registrar.playToClient(FaucetActivationPacket.TYPE, FaucetActivationPacket.STREAM_CODEC, FaucetActivationPacket::handle);
    registrar.playToClient(ChannelFlowPacket.TYPE, ChannelFlowPacket.STREAM_CODEC, ChannelFlowPacket::handle);
    registrar.playToClient(SmelteryTankUpdatePacket.TYPE, SmelteryTankUpdatePacket.STREAM_CODEC, SmelteryTankUpdatePacket::handle);
    registrar.playToClient(StructureUpdatePacket.TYPE, StructureUpdatePacket.STREAM_CODEC, StructureUpdatePacket::handle);
    registrar.playToClient(StructureErrorPositionPacket.TYPE, StructureErrorPositionPacket.STREAM_CODEC, StructureErrorPositionPacket::handle);

    // smeltery - server
    registrar.playToServer(SmelteryFluidClickedPacket.TYPE, SmelteryFluidClickedPacket.STREAM_CODEC, SmelteryFluidClickedPacket::handle);
  }


  /* Sending helpers */

  /**
   * Sends a payload to the given player
   */
  public void sendTo(CustomPacketPayload payload, ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, payload);
  }

  /**
   * Sends a payload to the server (client-side only)
   */
  public void sendToServer(CustomPacketPayload payload) {
    PacketDistributor.sendToServer(payload);
  }

  /**
   * Sends a payload to all clients near the given position in the given server level
   */
  public void sendToClientsAround(CustomPacketPayload payload, ServerLevel level, BlockPos position) {
    PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(position), payload);
  }

  /**
   * Same as {@link #sendToClientsAround(CustomPacketPayload, ServerLevel, BlockPos)}, but checks that the world is a server level
   */
  public void sendToClientsAround(CustomPacketPayload payload, @Nullable LevelAccessor world, BlockPos position) {
    if (world instanceof ServerLevel server) {
      sendToClientsAround(payload, server, position);
    }
  }

  /**
   * Sends a payload to all entities tracking the given entity and the entity itself
   */
  public void sendToTrackingAndSelf(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
  }

  /**
   * Sends a payload to all entities tracking the given entity
   */
  public void sendToTracking(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
  }

  /**
   * Sends a vanilla packet to the given player
   */
  public void sendVanillaPacket(Entity player, Packet<?> packet) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(packet);
    }
  }

  /**
   * Sends a payload to the whole player list
   * @param targetedPlayer  Main player to target, if null uses whole list
   * @param playerList      Player list to use if main player is null
   * @param payload         Payload to send
   */
  public void sendToPlayerList(@Nullable ServerPlayer targetedPlayer, PlayerList playerList, CustomPacketPayload payload) {
    if (targetedPlayer != null) {
      sendTo(payload, targetedPlayer);
    } else {
      for (ServerPlayer player : playerList.getPlayers()) {
        sendTo(payload, player);
      }
    }
  }
}

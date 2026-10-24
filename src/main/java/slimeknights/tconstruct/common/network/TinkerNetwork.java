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
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.PacketHelper;
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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/** Registers and sends Tinkers' Construct payloads. */
public class TinkerNetwork {
  private static final String PROTOCOL_VERSION = "1";
  @Nullable
  private static TinkerNetwork instance;

  private TinkerNetwork() {}

  /** Gets the network helper instance. */
  public static TinkerNetwork getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Attempt to call network getInstance before network is set up");
    }
    return instance;
  }

  /** Initializes the send helper before feature modules are constructed. */
  public static void setup() {
    if (instance == null) {
      instance = new TinkerNetwork();
    }
  }

  /** Registers the mod's custom payloads. The Forge protocol version 5 is not wire-compatible. */
  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    setup();
    PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

    // Shared
    registrar.playToClient(InventorySlotSyncPacket.TYPE, InventorySlotSyncPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateNeighborsPacket.TYPE, UpdateNeighborsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(GeneratePartTexturesPacket.TYPE, GeneratePartTexturesPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SyncPersistentDataPacket.TYPE, SyncPersistentDataPacket.CODEC, ISimplePacket.handle());

    // Gadgets
    registrar.playToClient(EntityMovementChangePacket.TYPE, EntityMovementChangePacket.CODEC, ISimplePacket.handle());

    // Tables
    registrar.playToServer(StationTabPacket.TYPE, StationTabPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationRenamePacket.TYPE, TinkerStationRenamePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateCraftingRecipePacket.TYPE, UpdateCraftingRecipePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationSelectionPacket.TYPE, TinkerStationSelectionPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerSlotLayoutsPacket.TYPE, UpdateTinkerSlotLayoutsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateStationScreenPacket.TYPE, UpdateStationScreenPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerStationRecipePacket.TYPE, UpdateTinkerStationRecipePacket.CODEC, ISimplePacket.handle());

    // Tools and materials
    registrar.playToClient(UpdateMaterialsPacket.TYPE, UpdateMaterialsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialStatsPacket.TYPE, UpdateMaterialStatsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialTraitsPacket.TYPE, UpdateMaterialTraitsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateToolDefinitionDataPacket.TYPE, UpdateToolDefinitionDataPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ToolContainerFluidUpdatePacket.TYPE, ToolContainerFluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SyncProjectileModifiersPacket.TYPE, SyncProjectileModifiersPacket.CODEC, ISimplePacket.handle());

    // Modifiers
    registrar.playToServer(TinkerControlPacket.TYPE, TinkerControlPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(InteractWithAirPacket.TYPE, InteractWithAirPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateModifiersPacket.TYPE, UpdateModifiersPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateFluidEffectsPacket.TYPE, UpdateFluidEffectsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(PushBlockRowPacket.TYPE, PushBlockRowPacket.CODEC, ISimplePacket.handle());

    // Smeltery
    registrar.playToClient(FluidUpdatePacket.TYPE, FluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(FaucetActivationPacket.TYPE, FaucetActivationPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ChannelFlowPacket.TYPE, ChannelFlowPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SmelteryTankUpdatePacket.TYPE, SmelteryTankUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureUpdatePacket.TYPE, StructureUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(SmelteryFluidClickedPacket.TYPE, SmelteryFluidClickedPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureErrorPositionPacket.TYPE, StructureErrorPositionPacket.CODEC, ISimplePacket.handle());
  }

  /** Sends a vanilla packet to the given player. */
  public void sendVanillaPacket(Entity player, Packet<?> packet) {
    PacketHelper.sendVanillaPacket(player, packet);
  }

  /** Sends a vanilla packet to the given player, matching the previous inherited overload. */
  public void sendVanillaPacket(Packet<?> packet, Entity player) {
    PacketHelper.sendVanillaPacket(player, packet);
  }

  /** Sends a payload to the server. */
  public void sendToServer(CustomPacketPayload payload) {
    PacketDistributor.sendToServer(payload);
  }

  /** Sends a payload to a player, skipping fake players as before. */
  public void sendTo(CustomPacketPayload payload, ServerPlayer player) {
    if (!(player instanceof FakePlayer)) {
      PacketDistributor.sendToPlayer(player, payload);
    }
  }

  /** Sends a payload to players tracking a block position. */
  public void sendToClientsAround(CustomPacketPayload payload, @Nullable LevelAccessor world, BlockPos position) {
    if (world instanceof ServerLevel server) {
      sendToClientsAround(payload, server, position);
    }
  }

  /** Sends a payload to players tracking a block position. */
  public void sendToClientsAround(CustomPacketPayload payload, ServerLevel world, BlockPos position) {
    PacketDistributor.sendToPlayersTrackingChunk(world, new ChunkPos(position), payload);
  }

  /** Sends a payload to all players tracking an entity and to the entity itself when it is a player. */
  public void sendToTrackingAndSelf(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
  }

  /** Sends a payload to all players tracking an entity. */
  public void sendToTracking(CustomPacketPayload payload, Entity entity) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
  }

  /** Sends a payload to one target player or to the complete player list. */
  public void sendToPlayerList(@Nullable ServerPlayer targetedPlayer, PlayerList playerList, CustomPacketPayload payload) {
    if (targetedPlayer != null) {
      sendTo(payload, targetedPlayer);
    } else {
      for (ServerPlayer player : playerList.getPlayers()) {
        sendTo(payload, player);
      }
    }
  }

  /** Sends a collection of payloads to the player(s) covered by a datapack sync event. */
  public static void syncPackets(OnDatapackSyncEvent event, CustomPacketPayload... payloads) {
    syncPackets(event, payloads, TinkerNetwork::isSyncEligible, TinkerNetwork::sendSyncPayloads);
  }

  static void syncPackets(OnDatapackSyncEvent event, CustomPacketPayload[] payloads,
                          Predicate<ServerPlayer> isEligible, BiConsumer<ServerPlayer,CustomPacketPayload[]> sender) {
    if (payloads.length == 0) {
      return;
    }
    ServerPlayer targetedPlayer = event.getPlayer();
    if (targetedPlayer != null) {
      if (isEligible.test(targetedPlayer)) {
        sender.accept(targetedPlayer, payloads);
      }
    } else {
      for (ServerPlayer player : event.getPlayerList().getPlayers()) {
        if (isEligible.test(player)) {
          sender.accept(player, payloads);
        }
      }
    }
  }

  private static boolean isSyncEligible(ServerPlayer player) {
    return isSyncEligible(player.connection.getConnection().isMemoryConnection(), player instanceof FakePlayer);
  }

  static boolean isSyncEligible(boolean memoryConnection, boolean fakePlayer) {
    return !memoryConnection && !fakePlayer;
  }

  private static void sendSyncPayloads(ServerPlayer player, CustomPacketPayload[] payloads) {
    PacketDistributor.sendToPlayer(player, payloads[0], Arrays.copyOfRange(payloads, 1, payloads.length));
  }
}

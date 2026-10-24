package slimeknights.tconstruct.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TinkerNetworkSyncTest {
  @Test
  void syncsTargetedPlayerAndBatchesPayloads() {
    OnDatapackSyncEvent event = mock(OnDatapackSyncEvent.class);
    ServerPlayer targeted = mock(ServerPlayer.class);
    CustomPacketPayload first = mock(CustomPacketPayload.class);
    CustomPacketPayload second = mock(CustomPacketPayload.class);
    List<SentPayloads> sent = new ArrayList<>();
    when(event.getPlayer()).thenReturn(targeted);

    TinkerNetwork.syncPackets(event, new CustomPacketPayload[]{first, second}, player -> true,
      (player, payloads) -> sent.add(new SentPayloads(player, Arrays.asList(payloads))));

    assertThat(sent).containsExactly(new SentPayloads(targeted, List.of(first, second)));
  }

  @Test
  void syncsEligiblePlayersOnReloadOrLoginBroadcast() {
    OnDatapackSyncEvent event = mock(OnDatapackSyncEvent.class);
    PlayerList playerList = mock(PlayerList.class);
    ServerPlayer first = mock(ServerPlayer.class);
    ServerPlayer skipped = mock(ServerPlayer.class);
    ServerPlayer last = mock(ServerPlayer.class);
    CustomPacketPayload payload = mock(CustomPacketPayload.class);
    List<SentPayloads> sent = new ArrayList<>();
    when(event.getPlayer()).thenReturn(null);
    when(event.getPlayerList()).thenReturn(playerList);
    when(playerList.getPlayers()).thenReturn(List.of(first, skipped, last));
    Set<ServerPlayer> eligible = Set.of(first, last);

    TinkerNetwork.syncPackets(event, new CustomPacketPayload[]{payload}, eligible::contains,
      (player, payloads) -> sent.add(new SentPayloads(player, Arrays.asList(payloads))));

    assertThat(sent).containsExactly(
      new SentPayloads(first, List.of(payload)),
      new SentPayloads(last, List.of(payload)));
  }

  @Test
  void sendsNothingWhenThereAreNoPayloads() {
    OnDatapackSyncEvent event = mock(OnDatapackSyncEvent.class);
    List<SentPayloads> sent = new ArrayList<>();

    TinkerNetwork.syncPackets(event, new CustomPacketPayload[0], player -> true,
      (player, payloads) -> sent.add(new SentPayloads(player, Arrays.asList(payloads))));

    assertThat(sent).isEmpty();
  }

  @Test
  void skipsMemoryConnectionsAndFakePlayers() {
    assertThat(TinkerNetwork.isSyncEligible(false, false)).isTrue();
    assertThat(TinkerNetwork.isSyncEligible(true, false)).isFalse();
    assertThat(TinkerNetwork.isSyncEligible(false, true)).isFalse();
    assertThat(TinkerNetwork.isSyncEligible(true, true)).isFalse();
  }

  private record SentPayloads(ServerPlayer player, List<CustomPacketPayload> payloads) {}
}

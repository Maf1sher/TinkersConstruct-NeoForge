package slimeknights.tconstruct.tools.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import java.util.Objects;

public record SyncProjectileModifiersPacket(int entityId, ModifierNBT modifiers, CompoundTag persistentData) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<SyncProjectileModifiersPacket> TYPE = new CustomPacketPayload.Type<>(TConstruct.getResource("sync_projectile_modifiers"));
  public static final StreamCodec<FriendlyByteBuf, SyncProjectileModifiersPacket> STREAM_CODEC = StreamCodec.ofMember(SyncProjectileModifiersPacket::encode, SyncProjectileModifiersPacket::new);

  private static final Streamable<ModifierNBT> MODIFIER_LIST = ModifierEntry.LOADABLE.list(0).flatXmap(ModifierNBT::new, ModifierNBT::getModifiers);

  public SyncProjectileModifiersPacket(Entity entity) {
    this(entity.getId(), EntityModifierCapability.getOrEmpty(entity), PersistentDataCapability.getOrWarn(entity).getCopy());
  }

  public SyncProjectileModifiersPacket(FriendlyByteBuf buffer) {
    this(buffer.readVarInt(), MODIFIER_LIST.decode(buffer), Objects.requireNonNullElse(buffer.readNbt(), new CompoundTag()));
  }

  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(entityId);
    MODIFIER_LIST.encode(buffer, modifiers);
    buffer.writeNbt(persistentData);
  }

  @Override
  public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

  public static void handle(SyncProjectileModifiersPacket payload, IPayloadContext context) {
    context.enqueueWork(() -> {
      Level level = SafeClientAccess.getLevel();
      if (level != null) {
        Entity entity = level.getEntity(payload.entityId);
        if (entity != null) {
          EntityModifierCapability.getCapability(entity).setModifiers(payload.modifiers);
          PersistentDataCapability.getOrWarn(entity).copyFrom(payload.persistentData);
        }
      }
    });
  }
}

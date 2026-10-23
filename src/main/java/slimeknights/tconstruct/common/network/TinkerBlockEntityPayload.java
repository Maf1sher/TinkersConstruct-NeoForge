package slimeknights.tconstruct.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.TConstruct;

/** Payload helper for client updates to a loaded block entity. */
public interface TinkerBlockEntityPayload<T> extends TinkerPayload {
  BlockPos pos();

  Class<T> blockEntityType();

  void handleBlockEntity(IPayloadContext context, T blockEntity);

  @Override
  default void handle(IPayloadContext context) {
    enqueue(context, () -> {
      BlockPos pos = pos();
      BlockEntity blockEntity = getBlockEntity(context.player().level(), pos, this);
      if (blockEntity == null) {
        return;
      }
      Class<T> expected = blockEntityType();
      if (expected.isInstance(blockEntity)) {
        handleBlockEntity(context, expected.cast(blockEntity));
      } else {
        TConstruct.LOG.error("Failed to handle packet {}: Block entity type mismatch at {}, expected {}, found {}",
          this, pos, expected, blockEntity.getClass());
      }
    });
  }

  /** Gets a block entity only when its chunk is loaded. */
  @Nullable
  static BlockEntity getBlockEntity(@Nullable BlockGetter world, BlockPos pos, Object packet) {
    BlockEntity blockEntity = BlockEntityHelper.getLoaded(world, pos);
    if (blockEntity == null) {
      TConstruct.LOG.error("Failed to handle packet {}: No loaded block entity at {}", packet, pos);
    }
    return blockEntity;
  }
}

package slimeknights.tconstruct.smeltery.block.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.MultiblockResult;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Multiblock that displays the error from the tile entity on right click
 */
public abstract class HeatingControllerBlock extends ControllerBlock {
  protected HeatingControllerBlock(Properties builder) {
    super(builder);
  }

  @Override
  protected InteractionResult openGui(BlockState state, Level world, BlockPos pos, Player player) {
    InteractionResult result = super.openGui(state, world, pos, player);
    // only need to update if holding the proper items
    if (!world.isClientSide && player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof HeatingStructureBlockEntity te) {
      MultiblockResult structureResult = te.getStructureResult();
      if (!structureResult.isSuccess() && te.showDebugBlockBorder(player)) {
        TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, structureResult.getPos()), serverPlayer);
      }
    }
    return result;
  }

  @Override
  protected boolean displayStatus(Player player, Level world, BlockPos pos, BlockState state) {
    if (!world.isClientSide && world.getBlockEntity(pos) instanceof HeatingStructureBlockEntity te) {
      MultiblockResult result = te.getStructureResult();
      if (!result.isSuccess()) {
        player.displayClientMessage(result.getMessage(), true);
        if (player instanceof ServerPlayer serverPlayer) {
          TinkerNetwork.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), serverPlayer);
        }
      }
    }
    return true;
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    RetexturedHelper.addTooltip(stack, tooltip, flag);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    if (world.getBlockEntity(pos) instanceof HeatingStructureBlockEntity te) {
      // update controller texture
      te.updateTexture(RetexturedHelper.getTexture(stack));
      // check structure
      te.updateStructure();
    }
  }

  @Deprecated
  @Override
  public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!newState.is(this) && worldIn.getBlockEntity(pos) instanceof HeatingStructureBlockEntity te) {
      te.invalidateStructure();
    }
    super.onRemove(state, worldIn, pos, newState, isMoving);
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    return RetexturedBlock.getPickBlock(level, pos, state);
  }


  /* No rotation if in a structure  */

  @Deprecated
  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    if (state.getValue(IN_STRUCTURE)) {
      return state;
    }
    return super.rotate(state, rotation);
  }

  @Deprecated
  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    if (state.getValue(IN_STRUCTURE)) {
      return state;
    }
    return super.mirror(state, mirror);
  }
}

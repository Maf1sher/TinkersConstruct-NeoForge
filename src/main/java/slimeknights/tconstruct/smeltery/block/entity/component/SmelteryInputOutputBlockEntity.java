package slimeknights.tconstruct.smeltery.block.entity.component;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Shared logic between drains and ducts
 */
public abstract class SmelteryInputOutputBlockEntity<T> extends SmelteryComponentBlockEntity implements IRetexturedBlockEntity {
  /** Capability this TE watches */
  private final BlockCapability<T, Direction> capability;

  /* Retexturing */
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  protected SmelteryInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockCapability<T, Direction> capability) {
    super(type, pos, state);
    this.capability = capability;
  }

  /**
   * Gets the capability from the parent block entity. Resolved on demand, as capabilities are not cached in NeoForge.
   * @return  Parent handler, or null if the parent is missing or does not expose the capability
   */
  @Nullable
  protected T getParentHandler() {
    if (level == null || !validateMaster()) {
      return null;
    }
    BlockPos master = getMasterPos();
    if (master == null) {
      return null;
    }
    return level.getCapability(capability, master, null);
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    assert level != null;

    // if we have a new master, notify neighbors so they query the new handler
    boolean masterChanged = false;
    if (!Objects.equals(getMasterPos(), master)) {
      masterChanged = true;
    }
    super.setMaster(master, block);
    // notify neighbors of the change (state change skips the notify flag)
    if (masterChanged) {
      level.blockUpdated(worldPosition, getBlockState().getBlock());
    }
  }


  /* Retexturing */

  @Override
  @Nonnull
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(getTexture());
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(Block newTexture) {
    Block oldTexture = texture;
    texture = newTexture;
    if (oldTexture != texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /* NBT */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags, Provider registries) {
    super.saveSynced(tags, registries);
    if (texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  public void loadAdditional(CompoundTag tags, Provider registries) {
    super.loadAdditional(tags, registries);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      RetexturedHelper.onTextureUpdated(this);
    }
  }


  /** Fluid implementation of smeltery IO */
  public static abstract class SmelteryFluidIO extends SmelteryInputOutputBlockEntity<IFluidHandler> {
    protected SmelteryFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, Capabilities.FluidHandler.BLOCK);
    }

    /** Fluid handler from the parent smeltery, null when the structure is not formed */
    @Nullable
    public IFluidHandler getFluidHandler() {
      return getParentHandler();
    }
  }

  /** Item implementation of smeltery IO */
  public static class ChuteBlockEntity extends SmelteryInputOutputBlockEntity<IItemHandler> {
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
      this(TinkerSmeltery.chute.get(), pos, state);
    }

    protected ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, Capabilities.ItemHandler.BLOCK);
    }

    /** Item handler from the parent smeltery, null when the parent is missing */
    @Nullable
    public IItemHandler getItemHandler() {
      return getParentHandler();
    }
  }

}

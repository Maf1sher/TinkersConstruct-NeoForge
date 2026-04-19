package slimeknights.tconstruct.smeltery.block.entity.component;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.item.TankItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TankBlockEntity extends SmelteryComponentBlockEntity implements ITankBlockEntity {
  /** Max capacity for the tank */
  public static final int DEFAULT_CAPACITY = FluidType.BUCKET_VOLUME * 4;

  /**
   * Gets the capacity for the given block
   * @param block  block
   * @return  Capacity
   */
  public static int getCapacity(Block block) {
    if (block instanceof ITankBlock) {
      return ((ITankBlock) block).getCapacity();
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Gets the capacity for the given item
   * @param item  item
   * @return  Capacity
   */
  public static int getCapacity(Item item) {
    if (item instanceof BlockItem) {
      return getCapacity(((BlockItem)item).getBlock());
    }
    return DEFAULT_CAPACITY;
  }

  /** Internal fluid tank instance */
  @Getter
  protected final FluidTankAnimated tank;
  /** Last comparator strength to reduce block updates */
  @Getter @Setter
  private int lastStrength = -1;

  public TankBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, state.getBlock() instanceof ITankBlock tank
                     ? tank
                     : TinkerSmeltery.searedTank.get(TankType.FUEL_TANK));
  }

  /** Main constructor */
  public TankBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    this(TinkerSmeltery.tank.get(), pos, state, block);
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ITankBlock block) {
    super(type, pos, state);
    tank = new FluidTankAnimated(block.getCapacity(), this);
  }

  /** Gets the fluid handler for capability registration */
  @Nullable
  public IFluidHandler getFluidHandlerCapability(@Nullable net.minecraft.core.Direction direction) {
    return new StackedTankHandler(this);
  }

  /** Gets contiguous vertical tanks of the same block from bottom to top */
  private static List<TankBlockEntity> getTankColumn(TankBlockEntity start) {
    Level level = start.getLevel();
    if (level == null) {
      return Collections.singletonList(start);
    }
    Block block = start.getBlockState().getBlock();
    BlockPos min = start.getBlockPos();
    while (true) {
      BlockPos next = min.below();
      if (!(level.getBlockEntity(next) instanceof TankBlockEntity below) || below.getBlockState().getBlock() != block) {
        break;
      }
      min = next;
    }

    List<TankBlockEntity> tanks = new ArrayList<>();
    BlockPos cursor = min;
    while (true) {
      if (!(level.getBlockEntity(cursor) instanceof TankBlockEntity tank) || tank.getBlockState().getBlock() != block) {
        break;
      }
      tanks.add(tank);
      cursor = cursor.above();
    }
    return tanks;
  }

  /**
   * Performs a single settling step in a vertical tank column, moving fluid downward.
   * @return true if any fluid moved this step
   */
  public static boolean settleTankColumnStep(TankBlockEntity start, int maxTransferPerPair) {
    List<TankBlockEntity> tanks = getTankColumn(start);
    if (tanks.size() <= 1) {
      return false;
    }

    boolean movedAny = false;
    int transferLimit = Math.max(1, maxTransferPerPair);
    for (int i = 0; i < tanks.size() - 1; i++) {
      TankBlockEntity lower = tanks.get(i);
      TankBlockEntity upper = tanks.get(i + 1);

      FluidStack upperFluid = upper.tank.getFluid();
      if (upperFluid.isEmpty()) {
        continue;
      }

      int toMove = Math.min(transferLimit, upperFluid.getAmount());
      if (toMove <= 0) {
        continue;
      }

      int filled = lower.tank.fill(upperFluid.copyWithAmount(toMove), FluidAction.EXECUTE);
      if (filled > 0) {
        upper.tank.drain(filled, FluidAction.EXECUTE);
        movedAny = true;
      }
    }
    return movedAny;
  }

  /** Combined fluid handler for a vertical stack of tanks */
  private record StackedTankHandler(TankBlockEntity root) implements IFluidHandler {
    private List<TankBlockEntity> tanks() {
      return getTankColumn(root);
    }

    @Override
    public int getTanks() {
      return 1;
    }

    @Override
    public @Nonnull FluidStack getFluidInTank(int tank) {
      if (tank != 0) {
        return FluidStack.EMPTY;
      }
      FluidStack fluid = FluidStack.EMPTY;
      int amount = 0;
      for (TankBlockEntity current : tanks()) {
        FluidStack contents = current.tank.getFluid();
        if (!contents.isEmpty()) {
          if (fluid.isEmpty()) {
            fluid = contents.copy();
            amount += contents.getAmount();
          } else if (FluidStack.isSameFluidSameComponents(contents, fluid)) {
            amount += contents.getAmount();
          }
        }
      }
      return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(amount);
    }

    @Override
    public int getTankCapacity(int tank) {
      if (tank != 0) {
        return 0;
      }
      int capacity = 0;
      for (TankBlockEntity current : tanks()) {
        capacity += current.tank.getCapacity();
      }
      return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
      return tank == 0 && root.tank.isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      if (resource.isEmpty()) {
        return 0;
      }

      List<TankBlockEntity> tanks = tanks();
      FluidStack existing = FluidStack.EMPTY;
      for (TankBlockEntity current : tanks) {
        FluidStack contents = current.tank.getFluid();
        if (!contents.isEmpty()) {
          existing = contents;
          break;
        }
      }
      if (!existing.isEmpty() && !FluidStack.isSameFluidSameComponents(existing, resource)) {
        return 0;
      }

      int total = 0;
      FluidStack remaining = resource.copy();
      for (TankBlockEntity current : tanks) {
        if (remaining.isEmpty()) {
          break;
        }
        int filled = current.tank.fill(remaining, action);
        if (filled > 0) {
          total += filled;
          remaining.shrink(filled);
        }
      }
      return total;
    }

    @Override
    public @Nonnull FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource.isEmpty()) {
        return FluidStack.EMPTY;
      }
      int max = resource.getAmount();
      if (max <= 0) {
        return FluidStack.EMPTY;
      }

      FluidStack drained = FluidStack.EMPTY;
      int remaining = max;
      List<TankBlockEntity> tanks = tanks();
      for (int i = tanks.size() - 1; i >= 0 && remaining > 0; i--) {
        TankBlockEntity current = tanks.get(i);
        FluidStack contents = current.tank.getFluid();
        if (contents.isEmpty() || !FluidStack.isSameFluidSameComponents(contents, resource)) {
          continue;
        }
        FluidStack part = current.tank.drain(remaining, action);
        if (!part.isEmpty()) {
          if (drained.isEmpty()) {
            drained = part.copy();
          } else {
            drained.grow(part.getAmount());
          }
          remaining -= part.getAmount();
        }
      }
      return drained;
    }

    @Override
    public @Nonnull FluidStack drain(int maxDrain, FluidAction action) {
      if (maxDrain <= 0) {
        return FluidStack.EMPTY;
      }
      List<TankBlockEntity> tanks = tanks();
      for (int i = tanks.size() - 1; i >= 0; i--) {
        FluidStack fluid = tanks.get(i).tank.getFluid();
        if (!fluid.isEmpty()) {
          return drain(fluid.copyWithAmount(maxDrain), action);
        }
      }
      return FluidStack.EMPTY;
    }
  }


  /*
   * Tank methods
   */

  @Nonnull
  @Override
  public ModelData getModelData() {
    // For stacked tanks, aggregate fluid from the entire column
    StackedTankHandler handler = new StackedTankHandler(this);
    FluidStack stacked = handler.getFluidInTank(0);
    int capacity = handler.getTankCapacity(0);
    return ModelData.builder()
                    .with(ModelProperties.FLUID_STACK, stacked)
                    .with(ModelProperties.TANK_CAPACITY, capacity).build();
  }

  /** Updates the light for this tank using {@link SearedTankBlock#LIGHT} */
  public static void updateLight(BlockEntity be, IFluidTank tank) {
    Level level = be.getLevel();
    if (level != null && !level.isClientSide) {
      FluidStack fluid = tank.getFluid();
      int light = fluid.isEmpty() ? 0 : fluid.getFluid().getFluidType().getLightLevel(fluid);
      BlockState state = be.getBlockState();
      if (light != state.getValue(SearedTankBlock.LIGHT)) {
        level.setBlock(be.getBlockPos(), state.setValue(SearedTankBlock.LIGHT, light), Block.UPDATE_CLIENTS);
      }
    }
  }

  @Override
  public void onTankContentsChanged() {
    ITankBlockEntity.super.onTankContentsChanged();
    if (this.level != null) {
      updateLight(this, tank);
      this.requestModelDataUpdate();
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide) {
      BlockPos masterPos = getMasterPos();
      if (masterPos != null && level.getBlockEntity(masterPos) instanceof IMasterLogic master) {
        master.onServantLoad(this);
      }
    }
  }

  /*
   * NBT
   */

  /**
   * Sets the tag on the stack based on the contained tank
   * @param stack  Stack
   */
  public void setTankTag(ItemStack stack) {
    TankItem.setTank(stack, tank);
  }

  /**
   * Updates the tank from an NBT tag, used in the block
   * @param nbt  tank NBT
   */
  public void updateTank(CompoundTag nbt) {
    if (nbt.isEmpty()) {
      tank.setFluid(FluidStack.EMPTY);
    } else {
      HolderLookup.Provider registries = level != null ? level.registryAccess() : net.minecraft.core.RegistryAccess.EMPTY;
      tank.readFromNBT(registries, nbt);
      updateLight(this, tank);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    tank.setCapacity(getCapacity(getBlockState().getBlock()));
    if (tag.contains(NBTTags.TANK)) {
      CompoundTag tankTag = tag.getCompound(NBTTags.TANK);
      if (tankTag.isEmpty()) {
        tank.setFluid(FluidStack.EMPTY);
      } else {
        tank.readFromNBT(registries, tankTag);
        updateLight(this, tank);
      }
    }
    super.loadAdditional(tag, registries);
  }

  @Override
  public void saveSynced(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveSynced(tag, registries);
    // want tank on the client on world load
    if (!tank.isEmpty()) {
      tag.put(NBTTags.TANK, tank.writeToNBT(registries, new CompoundTag()));
    }
  }

  /** Interface for blocks to return their capacity */
  public interface ITankBlock {
    /** Gets the capacity for this tank */
    int getCapacity();
  }
}

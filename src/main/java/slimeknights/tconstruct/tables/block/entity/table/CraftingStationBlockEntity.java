package slimeknights.tconstruct.tables.block.entity.table;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CraftingStationBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {
  public static final Component UNCRAFTABLE = TConstruct.makeTranslation("gui", "crafting_station.uncraftable");
  private static final Component NAME = TConstruct.makeTranslation("gui", "crafting_station");
  /** Dimensions of the crafting grid, also used by the menu and the consumption logic */
  public static final int GRID_WIDTH = 3;
  public static final int GRID_HEIGHT = 3;
  public static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;

  /** Last crafted crafting recipe */
  @Nullable
  private RecipeHolder<CraftingRecipe> lastRecipe;
  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultContainer craftingResult;
  /** Crafting inventory for the recipe calls */
  private final CraftingContainerWrapper craftingInventory;
  public CraftingStationBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.craftingStationTile.get(), pos, state, NAME, GRID_SIZE);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.craftingInventory = new CraftingContainerWrapper(this, GRID_WIDTH, GRID_HEIGHT);
    this.craftingResult = new LazyResultContainer(this);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new CraftingStationContainerMenu(menuId, playerInventory, this);
  }

  /** Creates a snapshot of the crafting grid for vanilla recipe matching */
  private CraftingInput createCraftingInput() {
    return this.craftingInventory.asCraftInput();
  }

  /* Crafting */

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.level == null || isEmpty()) {
      return ItemStack.EMPTY;
    }
    // assume empty unless we learn otherwise
    ItemStack result = ItemStack.EMPTY;
    CraftingInput input = createCraftingInput();
    if (!this.level.isClientSide && this.level.getServer() != null) {
      RecipeManager manager = this.level.getServer().getRecipeManager();

      // first, try the cached recipe
      CommonHooks.setCraftingPlayer(player);
      RecipeHolder<CraftingRecipe> recipe = lastRecipe;
      // if it does not match, find a new recipe
      // note we intentionally have no player access during matches, that could lead to an unstable recipe
      if (recipe == null || !recipe.value().matches(input, this.level)) {
        recipe = manager.getRecipeFor(RecipeType.CRAFTING, input, this.level).orElse(null);
      }

      // if we have a recipe, fetch its result
      if (recipe != null) {
        result = recipe.value().assemble(input, level.registryAccess());

        // sync if the recipe is different
        if (recipe != lastRecipe) {
          this.lastRecipe = recipe;
          this.syncToRelevantPlayers(this::syncRecipe);
        }
      }
      CommonHooks.setCraftingPlayer(null);
    }
    else if (this.lastRecipe != null && this.lastRecipe.value().matches(input, this.level)) {
      CommonHooks.setCraftingPlayer(player);
      result = this.lastRecipe.value().assemble(input, level.registryAccess());
      CommonHooks.setCraftingPlayer(null);
    }
    return result;
  }

  /**
   * Gets the player sensitive crafting result, also validating the player has access to this recipe
   * @param player  Player
   * @return  Player sensitive result
   */
  public ItemStack getResultForPlayer(Player player) {
    CommonHooks.setCraftingPlayer(player);
    RecipeHolder<CraftingRecipe> recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
    CraftingInput input = createCraftingInput();

    // try matches again now that we have player access
    if (recipe == null || this.level == null || !recipe.value().matches(input, level)) {
      CommonHooks.setCraftingPlayer(null);
      return ItemStack.EMPTY;
    }

    // check if the player has access to the recipe, if not give up
    // Disabled because this is an absolute mess of logic, and the gain is rather small, treating this like a furnace instead
    // note the gamerule is client side only anyways, so you would have to sync it, such as in the container
    // if you want limited crafting, disable the crafting station, the design of the station is incompatible with the game rule and vanilla syncing
//    if (!recipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
//      // mojang, why can't PlayerEntity just have a RecipeBook getter, why must I go through the sided classes? grr
//      boolean locked;
//      if (!world.isRemote) {
//        locked = player instanceof ServerPlayerEntity && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(recipe);
//      } else {
//        locked = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> player instanceof ClientPlayerEntity && !((ClientPlayerEntity) player).getRecipeBook().isUnlocked(recipe));
//      }
//      // if the player cannot craft this, block crafting
//      if (locked) {
//        CommonHooks.setCraftingPlayer(null);
//        return ItemStack.EMPTY;
//      }
//    }

    ItemStack result = recipe.value().assemble(input, level.registryAccess());
    CommonHooks.setCraftingPlayer(null);
    return result;
  }

  /**
   * Removes the result from this inventory, updating inputs and triggering recipe hooks
   * @param player  Player taking result
   * @param result  Result removed
   * @param amount  Number of times crafted
   */
  public void takeResult(Player player, ItemStack result, int amount) {
    RecipeHolder<CraftingRecipe> recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
    if (recipe == null || this.level == null) {
      return;
    }

    // fire crafting events
    if (!recipe.value().isSpecial()) {
      // unlock the recipe if it was not unlocked, so it shows in the recipe book
      player.awardRecipes(List.<RecipeHolder<?>>of(recipe));
    }
    result.onCraftedBy(this.level, player, amount);
    EventHooks.firePlayerCraftingEvent(player, result, this.craftingInventory);

    // update all slots in the inventory
    // the recipe input is cropped to the used area, so remap its indices back onto the full grid
    CraftingInput.Positioned positioned = this.craftingInventory.asPositionedCraftInput();
    CommonHooks.setCraftingPlayer(player);
    NonNullList<ItemStack> remaining = recipe.value().getRemainingItems(positioned.input());
    CommonHooks.setCraftingPlayer(null);
    consumeCraftingInput(this, positioned, remaining, stack -> {
      if (!player.getInventory().add(stack)) {
        player.drop(stack, false);
      }
    });
  }

  /**
   * Consumes one item from each slot used by the crafting recipe, applying remaining items such as empty buckets.
   * The crafting input is cropped to the bounding box of used slots, so its indices do not match the grid slots.
   * @param grid        Crafting grid to update
   * @param positioned  Positioned crafting input used to match the recipe
   * @param remaining   Remaining items for the cropped input
   * @param leftover    Sink for items that do not fit back into the grid
   */
  static void consumeCraftingInput(Container grid, CraftingInput.Positioned positioned, NonNullList<ItemStack> remaining, Consumer<ItemStack> leftover) {
    CraftingInput input = positioned.input();
    int left = positioned.left();
    int top = positioned.top();
    int width = input.width();
    for (int row = 0; row < input.height(); row++) {
      for (int col = 0; col < width; col++) {
        int index = col + row * width;
        int slot = (left + col) + (top + row) * GRID_WIDTH;
        ItemStack original = grid.getItem(slot);
        ItemStack newStack = remaining.get(index);

        // if empty or size 1, set directly (decreases by 1)
        if (original.isEmpty() || original.getCount() == 1) {
          grid.setItem(slot, newStack);
        }
        else if (ItemStack.isSameItemSameComponents(original, newStack)) {
          // if matching, merge (decreasing by 1)
          newStack.grow(original.getCount() - 1);
          grid.setItem(slot, newStack);
        }
        else {
          // directly update the slot
          grid.setItem(slot, original.copyWithCount(original.getCount() - 1));
          // otherwise, give the item to the player
          if (!newStack.isEmpty()) {
            leftover.accept(newStack);
          }
        }
      }
    }
  }

  /** Sends a message alerting the player this item is currently uncraftable, typically due to gamerules */
  public void notifyUncraftable(Player player) {
    // if empty, send a message so the player is more aware of why they cannot craft it, sent to chat as status bar is not visible
    // TODO: consider moving into the UI somewhere
    if (level != null && !level.isClientSide) {
      player.displayClientMessage(CraftingStationBlockEntity.UNCRAFTABLE, false);
    }
  }

  @Override
  public void onCraft(Player player, ItemStack result, int amount) {
    // update the inputs and trigger recipe hooks
    if (amount != 0 && !result.isEmpty()) {
      takeResult(player, result, amount);
    }
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    // clear the crafting result when the matrix changes so we recalculate the result
    this.craftingResult.clearContent();
  }


  /* Syncing */

  /**
   * Sends the current recipe to the given player
   * @param player  Player to send an update to
   */
  public void syncRecipe(Player player) {
    // must have a last recipe and a server world
    if (this.lastRecipe != null && this.level != null && !this.level.isClientSide && player instanceof ServerPlayer) {
      TinkerNetwork.getInstance().sendTo(new UpdateCraftingRecipePacket(this.worldPosition, this.lastRecipe), (ServerPlayer) player);
    }
  }

  /**
   * Updates the recipe from the server
   * @param recipe  New recipe
   */
  public void updateRecipe(RecipeHolder<CraftingRecipe> recipe) {
    this.lastRecipe = recipe;
    this.craftingResult.clearContent();
  }
}

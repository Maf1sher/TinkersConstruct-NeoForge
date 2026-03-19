package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Registers item capabilities for modifiable tool items.
 * In NeoForge 1.21.1, item capabilities are registered via RegisterCapabilitiesEvent
 * instead of the old initCapabilities() + ICapabilityProvider pattern.
 */
public class ToolCapabilityProvider {
  /** Kept for addon compatibility - constructors that create tool capability providers */
  private static final List<BiFunction<ItemStack, Supplier<? extends IToolStackView>, IToolCapabilityProvider>> PROVIDER_CONSTRUCTORS = new ArrayList<>();

  /** Registers a tool capability provider constructor (kept for addon compatibility) */
  public static void register(BiFunction<ItemStack, Supplier<? extends IToolStackView>, IToolCapabilityProvider> constructor) {
    PROVIDER_CONSTRUCTORS.add(constructor);
  }

  /** Collects all modifiable items from the registry */
  private static Item[] getModifiableItems() {
    List<Item> items = new ArrayList<>();
    for (Item item : BuiltInRegistries.ITEM) {
      if (item instanceof IModifiableDisplay) {
        items.add(item);
      }
    }
    return items.toArray(new Item[0]);
  }

  /** Registers all tool capabilities - called from RegisterCapabilitiesEvent */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    Item[] items = getModifiableItems();
    if (items.length == 0) return;

    // Register fluid handler capability
    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> {
      ToolStack tool = ToolStack.from(stack);
      if (tool.getVolatileData().getInt(ToolFluidCapability.TOTAL_TANKS) > 0) {
        return new ToolFluidCapability(stack, () -> tool);
      }
      return null;
    }, items);

    // Register item handler (inventory) capability
    event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> {
      ToolStack tool = ToolStack.from(stack);
      if (tool.getVolatileData().getInt(ToolInventoryCapability.TOTAL_SLOTS) > 0) {
        return new ToolInventoryCapability(() -> tool);
      }
      return null;
    }, items);

    // Register energy storage capability
    event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> {
      ToolStack tool = ToolStack.from(stack);
      if (tool.getStats().getInt(ToolEnergyCapability.MAX_STAT) > 0) {
        return new ToolEnergyCapability(() -> tool);
      }
      return null;
    }, items);

    // Register block item provider capability (custom TConstruct capability)
    event.registerItem(BlockItemProviderCapability.CAPABILITY, (stack, ctx) -> {
      ToolStack tool = ToolStack.from(stack);
      if (!tool.getModifiers().isEmpty()) {
        return new ToolBlockItemProviderHook.CapabilityImpl(tool);
      }
      return null;
    }, items);
  }

  /** Interface to get a capability on a tool - kept for addon compatibility */
  @FunctionalInterface
  public interface IToolCapabilityProvider {
    /** Gets a capability on the given tool. Returns null if not available */
    @Nullable
    Object getCapability(IToolStackView tool);

    /** Called to clear the cache of the provider */
    default void clearCache() {}
  }
}

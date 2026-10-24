package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Creates NeoForge item capability values backed by a tool stack. */
public class ToolCapabilityProvider {
  private static final List<BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider>> PROVIDER_CONSTRUCTORS = new ArrayList<>();

  /** Registers standard and Tinkers-specific item capabilities for every modifiable item. */
  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    ItemLike[] tools = BuiltInRegistries.ITEM.stream().filter(IModifiable.class::isInstance).toArray(ItemLike[]::new);
    event.registerItem(Capabilities.ItemHandler.ITEM,
      (stack, context) -> getCapability(stack, Capabilities.ItemHandler.ITEM), tools);
    event.registerItem(Capabilities.FluidHandler.ITEM,
      (stack, context) -> getCapability(stack, Capabilities.FluidHandler.ITEM), tools);
    event.registerItem(Capabilities.EnergyStorage.ITEM,
      (stack, context) -> getCapability(stack, Capabilities.EnergyStorage.ITEM), tools);
    event.registerItem(BlockItemProviderCapability.CAPABILITY,
      (stack, context) -> getCapability(stack, BlockItemProviderCapability.CAPABILITY), tools);
  }

  /** Gets a capability value for the given stack, or null if no tool provider supplies it. */
  @Nullable
  @SuppressWarnings("unchecked")
  public static <T> T getCapability(ItemStack stack, ItemCapability<T, @Nullable Void> capability) {
    ToolStack tool = ToolStack.from(stack);
    Supplier<ToolStack> toolSupplier = () -> tool;
    List<IToolCapabilityProvider> providers = PROVIDER_CONSTRUCTORS.stream()
      .map(constructor -> constructor.apply(stack, toolSupplier))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    for (IToolCapabilityProvider provider : providers) {
      T value = (T) provider.getCapability(tool, capability);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /** Registers a tool capability provider constructor. Every new tool will call this constructor to create your provider.
   * Is it valid for this constructor to return null, just note that it will not be called a second time if the tools state changes. Thus you should avoid conditioning on anything other than item type */
  public static void register(BiFunction<ItemStack,Supplier<? extends IToolStackView>,IToolCapabilityProvider> constructor) {
    PROVIDER_CONSTRUCTORS.add(constructor);
  }

  /** Interface to get a capability on a tool */
  @FunctionalInterface
  public interface IToolCapabilityProvider {
    /** Gets a capability on the given tool, or null when this provider does not supply it. */
    Object getCapability(IToolStackView tool, ItemCapability<?, @Nullable Void> capability);
  }
}

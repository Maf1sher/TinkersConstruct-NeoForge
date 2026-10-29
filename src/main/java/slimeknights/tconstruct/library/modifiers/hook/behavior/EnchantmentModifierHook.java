package slimeknights.tconstruct.library.modifiers.hook.behavior;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockHarvestModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * This interface exposes two methods, {@link #updateEnchantmentLevel(IToolStackView, ModifierEntry, Holder, int)} and {@link #updateEnchantments(IToolStackView, ModifierEntry, HolderLookup.RegistryLookup, ItemEnchantments.Mutable)}
 * to allow tools to claim to have enchantments to vanilla APIs without modifying NBT. For performance reasons we don't simply have one hook call the other, but their behavior must be consistent.
 * That is, whatever change you make to the level in {@link #updateEnchantmentLevel(IToolStackView, ModifierEntry, Holder, int)} must also be reflected in the enchantments in {@link #updateEnchantments(IToolStackView, ModifierEntry, HolderLookup.RegistryLookup, ItemEnchantments.Mutable)}.
 */
public interface EnchantmentModifierHook {
  /** Predicate to remove unneeded values from the enchantments */
  Predicate<Integer> VALUE_REMOVER = value -> value == null || value <= 0;

  /**
   * Gets the enchantment level for the given tool
   * @param tool         Tool instance
   * @param modifier     Modifier instance
   * @param enchantment  Enchantment being queried
   * @param level        Level before this enchantment makes any changes. May be negative, will be capped to 0+ after the hook runs.
   * @return Enchantment level, typically added to {@code level} instead of replacing it. May be negative, will be capped to 0+ after the hook runs.
   */
  int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Holder<Enchantment> enchantment, int level);

  /**
   * Adds all enchantment modifications made by this tool to the enchantments.
   * You may reduce the value to a negative, all values 0 or less will be removed after the hook runs.
   * @param tool          Tool instance
   * @param modifier      Modifier instance
   * @param enchantmentLookup  Lookup for enchantments, used by modules that reference an enchantment by key
   * @param enchantments       Mutable enchantments to add enchantments from this modifier. May contain negatives.
   * @see #addEnchantment(ItemEnchantments.Mutable, Holder, int)
   */
  void updateEnchantments(IToolStackView tool, ModifierEntry modifier, HolderLookup.RegistryLookup<Enchantment> enchantmentLookup, ItemEnchantments.Mutable enchantments);

  /** Adds the given enchantment to the mutable enchantments */
  static void addEnchantment(ItemEnchantments.Mutable enchantments, Holder<Enchantment> enchantment, int amount) {
    if (amount != 0) {
      enchantments.set(enchantment, enchantments.getLevel(enchantment) + amount);
    }
  }

  /**
   * Gets the enchantment level for the given tool
   * @param stack        Item stack instance
   * @param enchantment  Enchantment to query
   * @return  Enchantment level
   */
  static int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
    // read the stored level directly; EnchantmentHelper.getItemEnchantmentLevel delegates to ItemStack#getEnchantmentLevel,
    // which calls back into Item#getEnchantmentLevel and would recurse infinitely for modifiable tools
    int level = EnchantmentHelper.getTagEnchantmentLevel(enchantment, stack);
    IToolStackView tool = ToolStack.from(stack);
    for (ModifierEntry entry : tool.getModifierList()) {
      level = entry.getHook(ModifierHooks.ENCHANTMENTS).updateEnchantmentLevel(tool, entry, enchantment, level);
    }
    // we allow hooks to return negative, such as to cancel out an enchantment
    return Math.max(level, 0);
  }

  /**
   * Gets all enchantments on the given stack
   * @param stack  Stack instance
   * @param enchantmentLookup  Lookup for enchantments
   * @return  All contained enchantments
   */
  static ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> enchantmentLookup) {
    ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
    IToolStackView tool = ToolStack.from(stack);
    for (ModifierEntry entry : tool.getModifierList()) {
      entry.getHook(ModifierHooks.ENCHANTMENTS).updateEnchantments(tool, entry, enchantmentLookup, enchantments);
    }
    // we allow hooks to return negative, such as to cancel out an enchantment
    enchantments.removeIf(enchantment -> VALUE_REMOVER.test(enchantments.getLevel(enchantment)));
    return enchantments.toImmutable();
  }

  /** Merger that combines all modules together */
  record AllMerger(Collection<EnchantmentModifierHook> modules) implements EnchantmentModifierHook {
    @Override
    public int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Holder<Enchantment> enchantment, int level) {
      for (EnchantmentModifierHook module : modules) {
        level = module.updateEnchantmentLevel(tool, modifier, enchantment, level);
      }
      return level;
    }

    @Override
    public void updateEnchantments(IToolStackView tool, ModifierEntry modifier, HolderLookup.RegistryLookup<Enchantment> enchantmentLookup, ItemEnchantments.Mutable enchantments) {
      for (EnchantmentModifierHook module : modules) {
        module.updateEnchantments(tool, modifier, enchantmentLookup, enchantments);
      }
    }
  }

  /**
   * Simple implementation of this hook with just a single enchantment
   */
  @SuppressWarnings("unused") // API
  interface SingleEnchantment extends EnchantmentModifierHook {
    /**
     * Gets the enchantment this hook adds to the tool
     * @param tool      Tool instance
     * @param modifier  Modifier instance
     * @return  Enchantment for this hook to add
     */
    Holder<Enchantment> getEnchantment(IToolStackView tool, ModifierEntry modifier);

    /**
     * Gets the level of the enchantment to add
     * @param tool      Tool instance
     * @param modifier  Modifier instance
     * @return  Level of the enchantment to add
     */
    default int getEnchantmentLevel(IToolStackView tool, ModifierEntry modifier) {
      return modifier.getLevel();
    }

    @Override
    default int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Holder<Enchantment> enchantment, int level) {
      if (enchantment == getEnchantment(tool, modifier)) {
        level += getEnchantmentLevel(tool, modifier);
      }
      return level;
    }

    @Override
    default void updateEnchantments(IToolStackView tool, ModifierEntry modifier, HolderLookup.RegistryLookup<Enchantment> enchantmentLookup, ItemEnchantments.Mutable enchantments) {
      addEnchantment(enchantments, getEnchantment(tool, modifier), getEnchantmentLevel(tool, modifier));
    }
  }

  /** Combination of {@link SingleEnchantment} with {@link BlockHarvestModifierHook.MarkHarvesting} */
  interface SingleHarvestEnchantment extends SingleEnchantment, BlockHarvestModifierHook.MarkHarvesting {
    @Override
    default int updateEnchantmentLevel(IToolStackView tool, ModifierEntry modifier, Holder<Enchantment> enchantment, int level) {
      if (BlockHarvestModifierHook.MarkHarvesting.isHarvesting(tool)) {
        return SingleEnchantment.super.updateEnchantmentLevel(tool, modifier, enchantment, level);
      }
      return level;
    }

    @Override
    default void updateEnchantments(IToolStackView tool, ModifierEntry modifier, HolderLookup.RegistryLookup<Enchantment> enchantmentLookup, ItemEnchantments.Mutable enchantments) {
      if (BlockHarvestModifierHook.MarkHarvesting.isHarvesting(tool)) {
        SingleEnchantment.super.updateEnchantments(tool, modifier, enchantmentLookup, enchantments);
      }
    }
  }
}

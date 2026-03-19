package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/** A hook used to provide BlockItems through the {@link BlockItemProviderCapability}, for modifiers such as exchanging */
public interface ToolBlockItemProviderHook {
    /**
     * Get a {@link BlockItem} to provide, wrapped as an ItemStack with any required placement NBT data.
     */
    ItemStack getBlockItemStack(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity entity);

    /**
     * Consume a block from this provider.
     * @return {@code true} if this hook consumed, otherwise {@code false} indicating that another modifier needs to consume.
     */
    boolean consumeBlockItem(IToolStackView tool, ModifierEntry modifier, ItemStack backingStack, @Nullable LivingEntity entity);

    record CapabilityImpl(IToolStackView tool) implements BlockItemProviderCapability {
        @Override
        public ItemStack getBlockItemStack(ItemStack capStack, @Nullable LivingEntity entity) {
            for (ModifierEntry entry : tool.getModifiers()) {
                ToolBlockItemProviderHook hook = entry.getHook(ModifierHooks.BLOCK_ITEM_PROVIDER);
                ItemStack stack = hook.getBlockItemStack(tool, entry, entity);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (item instanceof BlockItem) {
                        return stack;
                    } else {
                        TConstruct.LOG.warn("ToolBlockItemProviderHook implementation tried to return a non-empty, non-blockitem stack! Hook: {}, Hook Class: {}, Provided Item: {}", hook, hook.getClass().getName(), BuiltInRegistries.ITEM.getId(item));
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void consume(ItemStack capStack, ItemStack backingStack, @Nullable LivingEntity entity) {
            for (ModifierEntry entry : tool.getModifiers()) {
                if (entry.getHook(ModifierHooks.BLOCK_ITEM_PROVIDER).consumeBlockItem(tool, entry, backingStack, entity)) {
                    return;
                }
            }
            TConstruct.LOG.warn("Could not find a modifier to consume {} from after providing it from ToolBlockItemProviderHook. This is likely causing a duplication glitch! Stack nbt: {}", BuiltInRegistries.ITEM.getKey(backingStack.getItem()), backingStack.getComponents());
        }
    }
}

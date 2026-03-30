package slimeknights.tconstruct.library.modifiers.hook.behavior;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Modifier hook for adding attributes to a tool when in the correct slot.
 */
public interface AttributesModifierHook {
  /** ResourceLocation IDs for armor attributes on held tools, indexed by slot index (mainhand=0, offhand=1) */
  ResourceLocation[] HELD_ARMOR_IDS = new ResourceLocation[]{TConstruct.getResource("held_armor.mainhand"), TConstruct.getResource("held_armor.offhand")};

  /**
   * Adds attributes from this modifier's effect. Called whenever the item stack refreshes attributes, typically on equipping and unequipping.
   * It is important that you return the same list when equipping and unequipping the item.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link ToolStatsModifierHook}: Limited context, but can affect durability, mining level, and mining speed.</li>
   * </ul>
   * @param tool      Current tool instance
   * @param modifier  Modifier level
   * @param slot      Slot for the attributes
   * @param consumer  Attribute consumer
   */
  void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer);

  /**
   * Gets attribute modifiers for a weapon with melee capability
   * @param tool  Tool instance
   * @param slot  Held slot
   * @return  Map of attribute modifiers
   */
  static Multimap<Attribute,AttributeModifier> getHeldAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    if (!tool.isBroken()) {
      // base melee stats - skip if not melee
      StatsNBT statsNBT = tool.getStats();
      if (slot == EquipmentSlot.MAINHAND && EntityInteractionModifierHook.isMeleeWeapon(tool)) {
        builder.put(Attributes.ATTACK_DAMAGE.value(), new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, statsNBT.get(ToolStats.ATTACK_DAMAGE), AttributeModifier.Operation.ADD_VALUE));
        // base attack speed is 4, but our numbers start from 4
        builder.put(Attributes.ATTACK_SPEED.value(), new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, statsNBT.get(ToolStats.ATTACK_SPEED) - 4d, AttributeModifier.Operation.ADD_VALUE));
      }

      if (slot.getType() == Type.HAND) {
        // shields and slimestaffs can get armor
        if (tool.hasTag(TinkerTags.Items.ARMOR)) {
          ResourceLocation id = HELD_ARMOR_IDS[slot.getIndex()];
          double value = statsNBT.get(ToolStats.ARMOR);
          if (value != 0) {
            builder.put(Attributes.ARMOR.value(), new AttributeModifier(id.withSuffix(".armor"), value, AttributeModifier.Operation.ADD_VALUE));
          }
          value = statsNBT.get(ToolStats.ARMOR_TOUGHNESS);
          if (value != 0) {
            builder.put(Attributes.ARMOR_TOUGHNESS.value(), new AttributeModifier(id.withSuffix(".toughness"), value, AttributeModifier.Operation.ADD_VALUE));
          }
          value = statsNBT.get(ToolStats.KNOCKBACK_RESISTANCE);
          if (value != 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE.value(), new AttributeModifier(id.withSuffix(".knockback_resistance"), value, AttributeModifier.Operation.ADD_VALUE));
          }
        }

        // grab attributes from modifiers, only do for hands (other slots would just be weird)
        BiConsumer<Attribute,AttributeModifier> attributeConsumer = builder::put;
        for (ModifierEntry entry : tool.getModifierList()) {
          entry.getHook(ModifierHooks.ATTRIBUTES).addAttributes(tool, entry, slot, attributeConsumer);
        }
      }
    }
    return builder.build();
  }

  /**
   * Builds an ItemAttributeModifiers from a tool for use in getDefaultAttributeModifiers(ItemStack).
   * Collects modifiers for both hand slots and returns a combined result.
   * @param tool  Tool instance
   * @return  ItemAttributeModifiers containing all attribute modifiers for both hand slots
   */
  static ItemAttributeModifiers buildDefaultAttributeModifiers(IToolStackView tool) {
    List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
    for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
      Multimap<Attribute, AttributeModifier> modifiers = getHeldAttributeModifiers(tool, slot);
      EquipmentSlotGroup slotGroup = slot == EquipmentSlot.MAINHAND ? EquipmentSlotGroup.MAINHAND : EquipmentSlotGroup.OFFHAND;
      for (var entry : modifiers.entries()) {
        entries.add(new ItemAttributeModifiers.Entry(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()), entry.getValue(), slotGroup));
      }
    }
    return new ItemAttributeModifiers(entries, true);
  }

  /** Merger that runs all hooks */
  record AllMerger(Collection<AttributesModifierHook> modules) implements AttributesModifierHook {
    @Override
    public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {
      for (AttributesModifierHook module : modules) {
        module.addAttributes(tool, modifier, slot, consumer);
      }
    }
  }
}

package slimeknights.tconstruct.library.tools.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;

import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;

import slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.display.ToolNameHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModifiableArmorItem extends ArmorItem implements IModifiableDisplay {
  /** Volatile modifier tag to make piglins neutal when worn */
  public static final ResourceLocation PIGLIN_NEUTRAL = TConstruct.getResource("piglin_neutral");
  /** Volatile modifier tag to make this item an elytra */
  public static final ResourceLocation ELYTRA = TConstruct.getResource("elyta");
  /** Volatile flag for a boot item to walk on powdered snow. Cold immunity is handled through a tag */
  public static final ResourceLocation SNOW_BOOTS = TConstruct.getResource("snow_boots");
  /** Volatile flag for an item to act as an enderman mask, stopping them from getting angry. */
  public static final ResourceLocation ENDERMASK = TConstruct.getResource("endermask");

  /** Resource locations for armor attribute modifiers per type */
  private static final ResourceLocation[] ARMOR_MODIFIER_ID_PER_TYPE = {
    TConstruct.getResource("armor.boots"),
    TConstruct.getResource("armor.leggings"),
    TConstruct.getResource("armor.chestplate"),
    TConstruct.getResource("armor.helmet"),
    TConstruct.getResource("armor.body")
  };

  @Getter
  private final ToolDefinition toolDefinition;
  /** Cache of the tool built for rendering */
  private ItemStack toolForRendering = null;

  public ModifiableArmorItem(Holder<ArmorMaterial> materialIn, ArmorItem.Type type, Properties builderIn, ToolDefinition toolDefinition) {
    super(materialIn, type, builderIn);
    this.toolDefinition = toolDefinition;
  }

  public ModifiableArmorItem(ModifiableArmorMaterial material, ArmorItem.Type type, Properties properties) {
    this(material.getHolder(), type, properties, Objects.requireNonNull(material.getArmorDefinition(type), "Missing tool definition for " + type.getName()));
  }

  /* Basic properties */

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
    return ModifierUtil.checkVolatileFlag(stack, PIGLIN_NEUTRAL);
  }

  @Override
  public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
    return type == Type.BOOTS && ModifierUtil.checkVolatileFlag(stack, SNOW_BOOTS);
  }

  @Override
  public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
    return type == Type.HELMET && ModifierUtil.checkVolatileFlag(stack, ENDERMASK);
  }

  @Override
  public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
    return ModifierUtil.canPerformAction(ToolStack.from(stack), toolAction);
  }

  @Override
  public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
    return true;
  }


  /* Enchantments */

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
    return false;
  }

  // canApplyAtEnchantingTable removed in 1.21, replaced by supportsEnchantment/isPrimaryItemFor
  // Tinker tools don't support enchanting table, so we override supportsEnchantment to only allow curses
  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    // Only allow curse enchantments on tinker armor
    return enchantment.is(net.minecraft.tags.EnchantmentTags.CURSE) && super.supportsEnchantment(stack, enchantment);
  }


  /* Loading */

  // verifyTagAfterLoad removed in 1.21 - ItemStack no longer uses NBT tags directly

  @Override
  public void onCraftedBy(ItemStack stack, Level levelIn, Player playerIn) {
    ToolStack.ensureInitialized(stack, getToolDefinition());
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level levelIn, Player playerIn, InteractionHand handIn) {
    if (playerIn.isCrouching()) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      InteractionResult result = ToolInventoryCapability.tryOpenContainer(stack, null, getToolDefinition(), playerIn, Util.getSlotType(handIn));
      if (result.consumesAction()) {
        return new InteractionResultHolder<>(result, stack);
      }
    }
    return super.use(levelIn, playerIn, handIn);
  }


  /* Display */

  @Override
  public boolean isFoil(ItemStack stack) {
    // we use enchantments to handle some modifiers, so don't glow from them
    // however, if a modifier wants to glow let them
    return ModifierUtil.checkVolatileFlag(stack, SHINY);
  }

  // getRarity(ItemStack) removed in 1.21 - rarity is now a data component
  // Tool rarity is managed through the modifier volatile data system (RarityModule)


  /* Indestructible items */

  @Override
  public boolean hasCustomEntity(ItemStack stack) {
    return IndestructibleItemEntity.hasCustomEntity(stack);
  }

  @Nullable
  @Override
  public Entity createEntity(Level level, Entity original, ItemStack stack) {
    return IndestructibleItemEntity.createFrom(level, original, stack);
  }


  /* Damage/Durability */

  @Override
  public boolean isRepairable(ItemStack stack) {
    // handle in the tinker station
    return false;
  }

  @Override
  public int getMaxDamage(ItemStack stack) {
    return ToolDamageUtil.getFakeMaxDamage(stack);
  }

  @Override
  public int getDamage(ItemStack stack) {
    if (getMaxDamage(stack) <= 0) {
      return 0;
    }
    return ToolStack.from(stack).getDamage();
  }

  @Override
  public void setDamage(ItemStack stack, int damage) {
    if (getMaxDamage(stack) > 0) {
      ToolStack.from(stack).setDamage(damage);
    }
  }

  @Override
  public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T damager, Consumer<Item> onBroken) {
    // We basically emulate Itemstack.damageItem here. We always return 0 to skip the handling in ItemStack.
    // If we don't tools ignore our damage logic
    if (getMaxDamage(stack) > 0 && ToolDamageUtil.damage(ToolStack.from(stack), amount, damager, stack)) {
      onBroken.accept(stack.getItem());
    }

    return 0;
  }


  /* Durability display */

  @Override
  public boolean isBarVisible(ItemStack pStack) {
    return DurabilityDisplayModifierHook.showDurabilityBar(pStack);
  }

  @Override
  public int getBarColor(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityRGB(pStack);
  }

  @Override
  public int getBarWidth(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityWidth(pStack);
  }


  /* Armor properties */

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
    return false;
  }

  @Override
  public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
    return armorType == getEquipmentSlot();
  }

  @Nullable
  @Override
  public EquipmentSlot getEquipmentSlot(ItemStack stack) {
    return getEquipmentSlot();
  }


  /**
   * Gets the attribute modifiers for the given tool and slot.
   * This is a custom method (not an override) used by the Multimap-based attribute system internally.
   */
  public Multimap<Attribute,AttributeModifier> getAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
    if (slot != getEquipmentSlot()) {
      return ImmutableMultimap.of();
    }

    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    if (!tool.isBroken()) {
      // base stats
      StatsNBT statsNBT = tool.getStats();
      ResourceLocation modifierId = ARMOR_MODIFIER_ID_PER_TYPE[type.ordinal()];
      builder.put(Attributes.ARMOR.value(), new AttributeModifier(modifierId.withSuffix(".armor"), statsNBT.get(ToolStats.ARMOR), AttributeModifier.Operation.ADD_VALUE));
      builder.put(Attributes.ARMOR_TOUGHNESS.value(), new AttributeModifier(modifierId.withSuffix(".toughness"), statsNBT.get(ToolStats.ARMOR_TOUGHNESS), AttributeModifier.Operation.ADD_VALUE));
      double knockbackResistance = statsNBT.get(ToolStats.KNOCKBACK_RESISTANCE);
      if (knockbackResistance != 0) {
        builder.put(Attributes.KNOCKBACK_RESISTANCE.value(), new AttributeModifier(modifierId.withSuffix(".knockback_resistance"), knockbackResistance, AttributeModifier.Operation.ADD_VALUE));
      }
      // grab attributes from modifiers
      BiConsumer<Attribute,AttributeModifier> attributeConsumer = builder::put;
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.ATTRIBUTES).addAttributes(tool, entry, slot, attributeConsumer);
      }
    }

    return builder.build();
  }

  /**
   * Returns attribute modifiers for the given slot and stack.
   * This is a custom method (not an override) that bridges to the internal Multimap system.
   * In 1.21, the vanilla override is getDefaultAttributeModifiers(ItemStack) returning ItemAttributeModifiers.
   */
  public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
    if (slot != getEquipmentSlot() || ToolStack.from(stack).getStats() == StatsNBT.EMPTY) {
      return ImmutableMultimap.of();
    }
    return getAttributeModifiers(ToolStack.from(stack), slot);
  }


  /* Elytra */

  @Override
  public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
    return type == Type.CHESTPLATE && !ToolDamageUtil.isBroken(stack) && ModifierUtil.checkVolatileFlag(stack, ELYTRA);
  }

  @Override
  public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
    if (getEquipmentSlot() == EquipmentSlot.CHEST) {
      ToolStack tool = ToolStack.from(stack);
      if (!tool.isBroken()) {
        // if any modifier says stop flying, stop flying
        for (ModifierEntry entry : tool.getModifierList()) {
          if (entry.getHook(ModifierHooks.ELYTRA_FLIGHT).elytraFlightTick(tool, entry, entity, flightTicks)) {
            return false;
          }
        }
        // damage the tool and keep flying
        if (!entity.level().isClientSide && (flightTicks + 1) % 20 == 0) {
          ToolDamageUtil.damageAnimated(tool, 1, entity, EquipmentSlot.CHEST);
        }
        return true;
      }
    }
    return false;
  }


  /* Ticking */

  @Override
  public void inventoryTick(ItemStack stack, Level levelIn, Entity entityIn, int itemSlot, boolean isSelected) {
    // don't care about non-living, they skip most tool context
    if (entityIn instanceof LivingEntity living) {
      ToolStack tool = ToolStack.from(stack);
      if (!levelIn.isClientSide) {
        tool.ensureHasData();
      }
      List<ModifierEntry> modifiers = tool.getModifierList();
      if (!modifiers.isEmpty()) {
        boolean isCorrectSlot = living.getItemBySlot(getEquipmentSlot()) == stack;
        // we pass in the stack for most custom context, but for the sake of armor its easier to tell them that this is the correct slot for effects
        for (ModifierEntry entry : modifiers) {
          entry.getHook(ModifierHooks.INVENTORY_TICK).onInventoryTick(tool, entry, levelIn, living, itemSlot, isSelected, isCorrectSlot, stack);
        }
      }
    }
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    return SlotStackModifierHook.overrideStackedOnOther(held, slot, action, player) || super.overrideStackedOnOther(held, slot, action, player);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack slotStack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    return SlotStackModifierHook.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access) || super.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access);
  }


  /* Tooltips */

  @Override
  public Component getName(ItemStack stack) {
    return ToolNameHook.getName(getToolDefinition(), stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    TooltipUtil.addInformation(this, stack, (Level) null, tooltip, SafeClientAccess.getTooltipKey(), flag);
  }

  @Override
  public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
    tooltips = TooltipUtil.getArmorStats(tool, player, tooltips, key, tooltipFlag);
    TooltipUtil.addAttributes(this, tool, player, tooltips, TooltipUtil.SHOW_ARMOR_ATTRIBUTES, getEquipmentSlot());
    return tooltips;
  }

  // getDefaultTooltipHideFlags removed in 1.21 - tooltip flags are now handled through data components

  /* Display items */

  @Override
  public ItemStack getRenderTool() {
    if (toolForRendering == null) {
      toolForRendering = ToolBuildHandler.buildToolForRendering(this, this.getToolDefinition());
    }
    return toolForRendering;
  }
}

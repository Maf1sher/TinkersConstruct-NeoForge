package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import slimeknights.tconstruct.library.events.ToolEquipmentChangeEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Detects equipment changes to run modifier hooks, so entities can react to armor changes
 */
public class EquipmentChangeWatcher {
  /** Client only map of player to last equipment, replacing the old capability */
  private static final Map<Player,Map<EquipmentSlot,ItemStack>> LAST_CLIENT_EQUIPMENT = new WeakHashMap<>();

  private EquipmentChangeWatcher() {}

  /** Prevents enqueuing client listeners on dedicated servers */
  public static void register() {
    // equipment change is used on servers; the client uses its own tick logic
    NeoForge.EVENT_BUS.addListener(EquipmentChangeWatcher::onEquipmentChange);

    // only need to track equipment on the client, the server gets real equipment change events
    if (FMLEnvironment.dist == Dist.CLIENT) {
      NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> onPlayerTick(event));
    }
  }


  /* Events */

  /** Serverside modifier hooks */
  private static void onEquipmentChange(LivingEquipmentChangeEvent event) {
    runModifierHooks(event.getEntity(), event.getSlot(), event.getFrom(), event.getTo());
  }

  /** Client side modifier hooks */
  private static void onPlayerTick(PlayerTickEvent.Post event) {
    // only run for client side players every 5 ticks
    Player player = event.getEntity();
    if (!player.level().isClientSide) {
      return;
    }
    Map<EquipmentSlot,ItemStack> lastItems = LAST_CLIENT_EQUIPMENT.computeIfAbsent(player, key -> {
      Map<EquipmentSlot,ItemStack> map = new EnumMap<>(EquipmentSlot.class);
      for (EquipmentSlot slot : EquipmentSlot.values()) {
        map.put(slot, ItemStack.EMPTY);
      }
      return map;
    });
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      ItemStack newStack = player.getItemBySlot(slot);
      ItemStack oldStack = lastItems.get(slot);
      if (!ItemStack.matches(oldStack, newStack)) {
        lastItems.put(slot, newStack.copy());
        runModifierHooks(player, slot, oldStack, newStack);
      }
    }
  }


  /* Helpers */

  /** Shared modifier hook logic */
  private static void runModifierHooks(LivingEntity entity, EquipmentSlot changedSlot, ItemStack original, ItemStack replacement) {
    EquipmentChangeContext context = new EquipmentChangeContext(entity, changedSlot, original, replacement);

    // first, fire event to notify an item was removed
    IToolStackView tool = context.getOriginalTool();
    if (tool != null && ModifierUtil.validArmorSlot(tool, changedSlot)) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onUnequip(tool, entry, context);
      }
    }

    // next, fire event to notify an item was added
    tool = context.getReplacementTool();
    if (tool != null && ModifierUtil.validArmorSlot(tool, changedSlot)) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onEquip(tool, entry, context);
      }
    }

    // finally, fire events on all other slots to say something changed
    for (EquipmentSlot otherSlot : EquipmentSlot.values()) {
      if (otherSlot != changedSlot) {
        tool = context.getValidTool(otherSlot);
        if (tool != null) {
          for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(ModifierHooks.EQUIPMENT_CHANGE).onEquipmentChange(tool, entry, context, otherSlot);
          }
        }
      }
    }
    // fire event for modifiers that want to watch equipment when not equipped
    NeoForge.EVENT_BUS.post(new ToolEquipmentChangeEvent(context));
  }
}

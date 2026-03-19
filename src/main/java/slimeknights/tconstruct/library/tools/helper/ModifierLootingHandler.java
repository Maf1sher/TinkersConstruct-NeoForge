package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.bus.api.EventPriority;
import slimeknights.tconstruct.common.TinkerDamageTypes;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.hook.combat.ArmorLootingModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.LootingModifierHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.context.LootingContext;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.TinkerEffects;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Logic to handle the looting event for all main tinker tools.
 *
 * In NeoForge 1.21, LootingLevelEvent was removed. The vanilla looting enchantment now operates
 * through the enchantment effect component system (EnchantmentEffectComponents) and is applied
 * directly during loot table evaluation. There is no event to modify the looting level.
 *
 * This handler now uses LivingDropsEvent (at HIGH priority) to apply TConstruct's modifier-based
 * looting. When the computed looting level is > 0, it duplicates random drops to simulate the
 * looting effect, similar to how EnchantedCountIncreaseFunction works in vanilla loot tables.
 */
public class ModifierLootingHandler {
  /** If contained in the set, they should use the offhand for looting */
  private static final Map<UUID,EquipmentSlot> LOOTING_OFFHAND = new HashMap<>();
  private static boolean init = false;

  /** Initializes this listener */
  public static void init() {
    if (init) {
      return;
    }
    init = true;
    // In 1.21, LootingLevelEvent is removed. We use LivingDropsEvent to apply modifier looting.
    NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, ModifierLootingHandler::onLivingDrops);
    NeoForge.EVENT_BUS.addListener(ModifierLootingHandler::onLeaveServer);
  }

  /**
   * Sets the hand used for looting, so the tool is fetched from the proper context
   * @param entity    Player to set
   * @param slotType  Slot type
   */
  public static void setLootingSlot(LivingEntity entity, EquipmentSlot slotType) {
    if (slotType == EquipmentSlot.MAINHAND) {
      LOOTING_OFFHAND.remove(entity.getUUID());
    } else {
      LOOTING_OFFHAND.put(entity.getUUID(), slotType);
    }
  }

  /** Gets the slot to use for looting */
  public static EquipmentSlot getLootingSlot(@Nullable LivingEntity entity) {
    return entity != null ? LOOTING_OFFHAND.getOrDefault(entity.getUUID(), EquipmentSlot.MAINHAND) : EquipmentSlot.MAINHAND;
  }

  /**
   * Computes the TConstruct looting level for the given damage context.
   * This replaces the old LootingLevelEvent-based computation.
   * @return the computed looting level, or -1 if not applicable
   */
  public static int computeLootingLevel(DamageSource damageSource, LivingEntity target) {
    // bleeding kills use the level of the effect for looting
    if (damageSource.is(TinkerDamageTypes.BLEEDING)) {
      return Math.max(0, TinkerEffect.getAmplifier(target, TinkerEffects.bleeding));
    }

    Entity source = damageSource.getEntity();
    if (source instanceof LivingEntity holder) {
      Entity direct = damageSource.getDirectEntity();
      int level = 0;

      // determine who is in charge of the looting
      LootingContext context;
      IToolStackView tool = null;
      if (direct instanceof Projectile) {
        // need to build a context from the relevant capabilities to use the modifier
        ModifierNBT modifiers = EntityModifierCapability.getOrEmpty(direct);
        context = new LootingContext(holder, target, damageSource, null);
        // no modifiers means its not a projectile we fired, so just defer to dumb vanilla behavior
        if (!modifiers.isEmpty()) {
          ModDataNBT persistentData = PersistentDataCapability.getOrWarn(direct);
          level = LootingModifierHook.getLooting(new DummyToolStack(Items.AIR, modifiers, persistentData), context, 0);
        }
      } else {
        // not an arrow? means the held tool is to blame
        EquipmentSlot slotType = getLootingSlot(holder);
        context = new LootingContext(holder, target, damageSource, slotType);
        ItemStack held = holder.getItemBySlot(slotType);

        // if its modifiable, let it increase the level
        if (held.is(TinkerTags.Items.MODIFIABLE)) {
          tool = ToolStack.from(held);
          level = LootingModifierHook.getLooting(tool, context, level);
        } else if (slotType != EquipmentSlot.MAINHAND) {
          // if it's not modifiable, yet we have a slot marked to blame for looting, ignore the event value
          level = 0;
        }
      }
      // boost looting with armor regardless
      level = ArmorLootingModifierHook.getLooting(tool, context, level);
      return Math.max(level, 0);
    }
    return -1;
  }

  /**
   * Handler for LivingDropsEvent. Since LootingLevelEvent was removed in NeoForge 1.21,
   * we apply modifier-based looting by duplicating random drops when the computed looting level
   * is positive. This simulates the vanilla looting behavior for TConstruct modifiers.
   */
  private static void onLivingDrops(LivingDropsEvent event) {
    DamageSource damageSource = event.getSource();
    if (damageSource == null) {
      return;
    }
    LivingEntity target = event.getEntity();
    int lootingLevel = computeLootingLevel(damageSource, target);

    // only process if we computed a positive looting level
    if (lootingLevel > 0) {
      Collection<ItemEntity> drops = event.getDrops();
      if (!drops.isEmpty()) {
        // duplicate random items from the drops based on looting level
        // this is a simplified version of how EnchantedCountIncreaseFunction works:
        // for each level of looting, there's a chance to duplicate each drop
        List<ItemEntity> bonusDrops = new ArrayList<>();
        for (ItemEntity drop : drops) {
          ItemStack stack = drop.getItem();
          // add 0 to lootingLevel extra items per drop
          int extra = target.getRandom().nextInt(lootingLevel + 1);
          if (extra > 0) {
            ItemStack bonus = stack.copy();
            bonus.setCount(extra);
            ItemEntity bonusEntity = new ItemEntity(
              drop.level(), drop.getX(), drop.getY(), drop.getZ(), bonus
            );
            bonusEntity.setDefaultPickUpDelay();
            bonusDrops.add(bonusEntity);
          }
        }
        drops.addAll(bonusDrops);
      }
    }
  }

  /** Called when a player leaves the server to clear the face */
  private static void onLeaveServer(PlayerLoggedOutEvent event) {
    LOOTING_OFFHAND.remove(event.getEntity().getUUID());
  }
}

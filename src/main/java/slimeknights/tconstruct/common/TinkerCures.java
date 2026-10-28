package slimeknights.tconstruct.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/** Cures used by Tinkers' Construct effects. */
public class TinkerCures {
  /** Cure used for effects that are removed when the granting equipment is unequipped */
  public static final EffectCure EQUIPMENT = EffectCure.get("tconstruct:equipment");

  /**
   * Maps a curative item to the NeoForge cure it applies.
   * Replaces the removed per-item cure lists; unknown items fall back to the equipment cure.
   * @param stack  Curative item
   * @return  Cure applied by the item
   */
  public static EffectCure forItem(ItemStack stack) {
    if (stack.is(Items.MILK_BUCKET)) {
      return EffectCures.MILK;
    }
    if (stack.is(Items.HONEY_BOTTLE)) {
      return EffectCures.HONEY;
    }
    return EQUIPMENT;
  }

  private TinkerCures() {}
}

package slimeknights.tconstruct.fluids.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

/** Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient */
public class BottleBrewingRecipe extends BrewingRecipe {
  private final Item from;
  private final Item to;
  public BottleBrewingRecipe(Ingredient input, Item from, Item to, ItemStack output) {
    super(input, Ingredient.EMPTY, output);
    this.from = from;
    this.to = to;
  }

  /**
   * Gets the vanilla container ingredient converting one potion container into another.
   * Vanilla keeps its container mixes in an instance list since 1.21, so the two vanilla transformations are mirrored here.
   */
  private static Ingredient getVanillaIngredient(Item from, Item to) {
    if (from == Items.POTION && to == Items.SPLASH_POTION) {
      return Ingredient.of(Items.GUNPOWDER);
    }
    if (from == Items.SPLASH_POTION && to == Items.LINGERING_POTION) {
      return Ingredient.of(Items.DRAGON_BREATH);
    }
    return Ingredient.EMPTY;
  }

  @Override
  public boolean isIngredient(ItemStack stack) {
    return getVanillaIngredient(from, to).test(stack);
  }

  @Override
  public Ingredient getIngredient() {
    return getVanillaIngredient(from, to);
  }
}

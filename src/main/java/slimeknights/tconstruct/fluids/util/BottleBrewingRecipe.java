package slimeknights.tconstruct.fluids.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

/** Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient */
public class BottleBrewingRecipe extends BrewingRecipe {
  private final Item from;
  private final Item to;
  /** Lazily cached PotionBrewing instance for checking container mixes */
  private static PotionBrewing cachedBrewing;

  public BottleBrewingRecipe(Ingredient input, Item from, Item to, ItemStack output) {
    super(input, Ingredient.EMPTY, output);
    this.from = from;
    this.to = to;
  }

  /** Gets or creates a PotionBrewing instance for checking vanilla container mixes */
  @SuppressWarnings("deprecation")
  private static PotionBrewing getBrewing() {
    if (cachedBrewing == null) {
      cachedBrewing = PotionBrewing.bootstrap(FeatureFlags.DEFAULT_FLAGS);
    }
    return cachedBrewing;
  }

  @Override
  public boolean isIngredient(ItemStack stack) {
    // Create a test potion of the 'from' item type with water contents, then try mixing
    ItemStack fromStack = new ItemStack(from);
    fromStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
    ItemStack result = getBrewing().mix(fromStack, stack);
    // Check if the result item matches the expected 'to' item
    return result.is(to);
  }

  @Override
  public Ingredient getIngredient() {
    // Since PotionBrewing.Mix is package-private in 1.21, we check using hasContainerMix
    // The vanilla container mixes are well-known: gunpowder (potion→splash), dragon's breath (splash→lingering)
    // Test each known brewing ingredient to find the one that converts 'from' to 'to'
    PotionBrewing brewing = getBrewing();
    ItemStack fromStack = new ItemStack(from);
    fromStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
    // Check if the from→to mix has a known ingredient by testing against common brewing ingredients
    if (brewing.isContainerIngredient(new ItemStack(net.minecraft.world.item.Items.GUNPOWDER))) {
      ItemStack result = brewing.mix(fromStack, new ItemStack(net.minecraft.world.item.Items.GUNPOWDER));
      if (result.is(to)) {
        return Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER);
      }
    }
    if (brewing.isContainerIngredient(new ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH))) {
      ItemStack result = brewing.mix(fromStack, new ItemStack(net.minecraft.world.item.Items.DRAGON_BREATH));
      if (result.is(to)) {
        return Ingredient.of(net.minecraft.world.item.Items.DRAGON_BREATH);
      }
    }
    return Ingredient.EMPTY;
  }
}

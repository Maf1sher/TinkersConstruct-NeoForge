package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.utils.RecipeHelper;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.TinkerFluidIngredients;
import slimeknights.tconstruct.library.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.potion.ModifierPotionCastingRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Arrays;
import java.util.List;

/**
 * Casting recipe clearing the potion from a tool.
 * TODO 1.21: move to {@link slimeknights.tconstruct.library.recipe.casting.potion}
 */
public class TipClearingCastingRecipe extends ModifierPotionCastingRecipe {
  public static final RecordLoadable<TipClearingCastingRecipe> LOADER = RecordLoadable.create(
    TypeAwareRecipeSerializer.TYPED_SERIALIZER, ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TipClearingCastingRecipe::new);

  public TipClearingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient tool, SizedFluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    // must have the modifier, and the potion set
    return super.matches(inv, level) && !ModifierUtil.getPersistentString(inv.getStack(), modifier.getLocation()).isEmpty();
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = inv.getStack().copy();
    ToolStack.from(result).getPersistentData().remove(modifier.getLocation());
    return result;
  }


  /* JEI */

  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    return RecipeHelper.named(id, displayRecipes(id, provider));
  }
/** Lazily loaded list of display recipes for JEI */
  private List<IDisplayableCastingRecipe> displayRecipes(ResourceLocation id, HolderLookup.Provider access) {
    if (displayRecipes == null) {
      List<FluidStack> fluids = TinkerFluidIngredients.displayFluids(fluid);
      List<String> potions = getPotionIds();

      // create a recipe per tool
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        // start with just the tool with the modifier
        ItemStack withModifier = addModifier(stack);
        return DisplayCastingRecipe.from(id, this)
          .casts(addPotion(withModifier, potions)).consumed()
          .result(withModifier).unlinkOutput()
          .fluids(fluids).coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}

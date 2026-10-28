package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.fluid.FluidTagHelper;
import slimeknights.tconstruct.library.utils.RecipeHelper;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.potion.ModifierPotionCastingRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Casting recipe applying a potion to a tool.
 * TODO 1.21: move to {@link slimeknights.tconstruct.library.recipe.casting.potion}
 */
public class TippingCastingRecipe extends ModifierPotionCastingRecipe {
  /** NBT key of the potion on the fluid, matches {@link slimeknights.tconstruct.fluids.fluids.PotionFluidType} */
  private static final String POTION_TAG = "Potion";

  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = ModifierPotionCastingRecipe.TOOL_FIELD;
  public static final RecordLoadable<TippingCastingRecipe> LOADER = RecordLoadable.create(
    TypeAwareRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TippingCastingRecipe::new);

  public TippingCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient tool, SizedFluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    // must have the modifier to cast
    if (super.matches(inv, level)) {
      // must also have a specific potion, it's what we are going to copy
      // but it can't match what is already on the stack
      CompoundTag fluidTag = inv.getFluidTag();
      return fluidTag != null && fluidTag.contains(POTION_TAG, Tag.TAG_STRING)
        && !ModifierUtil.getPersistentString(inv.getStack(), modifier.getLocation()).equals(fluidTag.getString(POTION_TAG));
    }
    return false;
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    ItemStack result = inv.getStack().copy();
    CompoundTag tag = inv.getFluidTag();
    if (tag != null) {
      ToolStack.from(result).getPersistentData().putString(modifier.getLocation(), tag.getString(POTION_TAG));
    }
    return result;
  }


  /* JEI */


  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    return RecipeHelper.named(id, displayRecipes(provider));
  }
/** Lazily loaded list of display recipes for JEI */
  private List<IDisplayableCastingRecipe> displayRecipes(HolderLookup.Provider access) {
    if (displayRecipes == null) {
      // first, get a list of potion IDs
      List<String> potions = getPotionIds();
      List<CompoundTag> potionNBT = potions.stream().map(id -> {
        CompoundTag fluidNBT = new CompoundTag();
        fluidNBT.putString(POTION_TAG, id);
        return fluidNBT;
      }).toList();

      // next, make 1 copy of the potion fluid with NBT list per potion fluid
      List<FluidStack> basePotions = java.util.Arrays.asList(this.fluid.getFluids());
      List<FluidStack> fluids = basePotions.stream()
        .flatMap(fluid -> potionNBT.stream().map(nbt -> FluidTagHelper.withTag(fluid.getFluid(), fluid.getAmount(), nbt)))
        .toList();

      // finally, create 1 recipe per input tool
      int basePotionCount = basePotions.size();
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        // start with just the tool with the modifier
        ItemStack withModifier = addModifier(stack);
        // next, add the potion to the tools
        List<ItemStack> withPotion = addPotion(withModifier, potions);
        // duplicate the list if we have multiple potion fluids
        if (basePotionCount > 1) {
          List<ItemStack> list = new ArrayList<>(withPotion.size() * basePotionCount);
          for (int i = 0; i < basePotionCount; i++) {
            list.addAll(withPotion);
          }
          withPotion = List.copyOf(list);
        }
        // and finally create the recipe
        return DisplayCastingRecipe.from(this)
          .cast(withModifier).consumed()
          .results(withPotion).fluids(fluids).linkFluidsToOutput()
          .coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}

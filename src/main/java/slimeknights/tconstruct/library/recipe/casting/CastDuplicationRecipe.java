package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.utils.RecipeHelper;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.helper.TypeAwareRecipeSerializer;

import java.util.List;

/** Recipe which duplicates the input cast using a fluid */
public class CastDuplicationRecipe extends ItemCastingRecipe implements MultiNamedRecipe<IDisplayableCastingRecipe> {
  public static final RecordLoadable<CastDuplicationRecipe> LOADER = RecordLoadable.create(
    TypeAwareRecipeSerializer.TYPED_SERIALIZER, ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP,
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("cast", CastDuplicationRecipe::getCast),
    FLUID_FIELD, COOLING_TIME_FIELD,
    CastDuplicationRecipe::new);

  public CastDuplicationRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, SizedFluidIngredient fluid, int coolingTime) {
    super(serializer, id, group, cast, fluid, ItemOutput.EMPTY, coolingTime, false, false);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    return inv.getStack().copy();
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    ItemStack[] items = getCast().getItems();
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }

  /* JEI */
  private List<IDisplayableCastingRecipe> displayRecipes = null;


  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    return RecipeHelper.named(id, displayRecipes(id, provider));
  }
/** Lazily loaded list of display recipes for JEI */
  private List<IDisplayableCastingRecipe> displayRecipes(ResourceLocation id, HolderLookup.Provider access) {
    if (displayRecipes == null) {
      List<ItemStack> casts = List.of(getCast().getItems());
      displayRecipes = List.of(DisplayCastingRecipe.from(id, this)
        .casts(casts).results(casts).linkCastToOutput()
        .fluids(java.util.Arrays.asList(fluid.getFluids()))
        .coolingTime(coolingTime)
        .build());
    }
    return displayRecipes;
  }
}

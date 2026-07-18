package slimeknights.tconstruct.library.recipe.casting;

import com.google.gson.JsonParseException;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.json.field.CompatFluidIngredientField;
import slimeknights.tconstruct.library.json.field.CompatItemOutputField;

import java.util.Arrays;
import java.util.List;

/** Casting recipe that takes a fluid and optional cast and outputs an item. */
@Getter
public class ItemCastingRecipe extends AbstractCastingRecipe implements IDisplayableCastingRecipe {
  /* Shared fields */
  protected static final LoadableField<FluidIngredient,ItemCastingRecipe> FLUID_FIELD = new CompatFluidIngredientField<>(FluidIngredient.LOADABLE.requiredField("fluid", ItemCastingRecipe::getFluid));
  protected static final LoadableField<ItemOutput,ItemCastingRecipe> RESULT_FIELD = new CompatItemOutputField<>(ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", r -> r.result));
  protected static final LoadableField<Integer,ItemCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.requiredField("cooling_time", ItemCastingRecipe::getCoolingTime);
  /** Loader instance */
  public static final RecordLoadable<ItemCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPED_SERIALIZER.requiredField(), ContextKey.ID.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    ItemCastingRecipe::new);

  private final TypeAwareRecipeSerializer<?> serializer;
  protected final FluidIngredient fluid;
  protected final ItemOutput result;
  protected final int coolingTime;
  public ItemCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer.getType(), id, group, cast, consumed, switchSlots);
    this.serializer = serializer;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    try {
      validateItemOutput(id, result);
    } catch (RuntimeException e) {
      // during datagen, item tags may not be loaded yet; runtime loading will validate correctly
    }
    CastingRecipeLookup.registerCastable(result);
  }

  /** Ensures datapack outputs resolve to an actual item before the recipe reaches packet sync */
  protected static void validateItemOutput(ResourceLocation recipeId, ItemOutput output) {
    if (output == ItemOutput.EMPTY) {
      return;
    }

    // Tag outputs must resolve to a non-empty stack to safely sync in packets.
    // Some outputs may temporarily fail to resolve during early load if they depend on config,
    // in which case we defer validation and let final recipe loading decide.
    if (output.getTag() != null) {
      try {
        if (!output.get().isEmpty()) {
          return;
        }
      } catch (RuntimeException e) {
        if (isConfigNotLoaded(e)) {
          return;
        }
        throw e;
      }

      String source = "tag '" + output.getTag().location() + "'";
      throw new JsonParseException("Casting recipe '" + recipeId + "' has invalid result from " + source);
    }

    if (!output.get().isEmpty()) {
      return;
    }

    String source = "an empty item stack";
    throw new JsonParseException("Casting recipe '" + recipeId + "' has invalid result from " + source);
  }

  /** Detects the common early-load config access failure so we can defer validation until later load stages. */
  private static boolean isConfigNotLoaded(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && message.contains("Cannot get config value before config is loaded")) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return this.fluid.getAmount(inv.getFluid());
  }

  @Override
  public boolean matches(ICastingContainer inv, Level worldIn) {
    // In 1.21, Ingredient.EMPTY.test(ItemStack.EMPTY) returns false unlike Forge 1.20.
    // For cast-less recipes (cast == Ingredient.EMPTY), match when the slot is empty.
    Ingredient cast = getCast();
    ItemStack stack = inv.getStack();
    boolean castMatches = (cast == Ingredient.EMPTY) ? stack.isEmpty() : cast.test(stack);
    return castMatches && fluid.test(inv.getFluid());
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return getSafeResult();
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return this.coolingTime;
  }


  /* JEI */

  @Override
  public ResourceLocation getRecipeId() {
    // need a separate method as remapping makes the names mismatch
    return getId();
  }

  @Override
  public boolean hasCast() {
    return getCast() != Ingredient.EMPTY;
  }

  @Override
  public List<ItemStack> getCastItems() {
    return Arrays.asList(getCast().getItems());
  }

  @Override
  public ItemStack getOutput() {
    return getSafeResult();
  }

  /**
   * Some third-party recipe scanners request outputs before configs are loaded,
   * which can make tag preference lookup throw. Return empty in that phase.
   */
  private ItemStack getSafeResult() {
    try {
      return this.result.get();
    } catch (RuntimeException e) {
      if (isConfigNotLoaded(e)) {
        return ItemStack.EMPTY;
      }
      throw e;
    }
  }

  /**
   * Gets a list of valid fluid inputs for this recipe, for display in JEI
   * @return  List of fluids
   */
  @Override
  public List<FluidStack> getFluids() {
    return this.fluid.getFluids();
  }
}

package slimeknights.tconstruct.library.recipe.alloying;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.common.recipe.LoadableFinishedRecipe;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe.AlloyIngredient;

import java.util.ArrayList;
import java.util.List;

import static slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe.getTemperature;

/** Builder for alloy recipes */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "alloy")
public class AlloyRecipeBuilder extends AbstractRecipeBuilder<AlloyRecipeBuilder> {
  private final FluidOutput output;
  private final int temperature;
  private final List<AlloyIngredient> inputs = new ArrayList<>();

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid    fluid to alloy
   * @param amount   fluid amount
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(FluidObject<?> fluid, int amount) {
    return alloy(fluid.result(amount), getTemperature(fluid));
  }

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid   Fluid output
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(FluidStack fluid) {
    return alloy(FluidOutput.fromStack(fluid), getTemperature(fluid));
  }

  /**
   * Creates a new recipe producing the given fluid
   * @param fluid   Fluid output
   * @param amount  Output amount
   * @return  Builder instance
   */
  public static AlloyRecipeBuilder alloy(Fluid fluid, int amount) {
    return alloy(new FluidStack(fluid, amount));
  }


  /* Inputs */

  /**
   * Adds an input
   * @param input  Input ingredient
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(SizedFluidIngredient input) {
    inputs.add(new AlloyIngredient(input, false));
    return this;
  }

  /**
   * Adds an input that is not consumed
   * @param input  Catalyst ingredient
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addCatalyst(SizedFluidIngredient input) {
    inputs.add(new AlloyIngredient(input, true));
    return this;
  }

  /**
   * Adds an input
   * @param input  Input fluid
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(FluidStack input) {
    return addInput(SizedFluidIngredient.of(input));
  }

  /**
   * Adds an input
   * @param fluid   Input fluid
   * @param amount  Input amount
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(Fluid fluid, int amount) {
    return addInput(SizedFluidIngredient.of(new FluidStack(fluid, amount)));
  }

  /**
   * Adds an input
   * @param tag     Input tag
   * @param amount  Input amount
   * @return  Builder instance
   */
  public AlloyRecipeBuilder addInput(TagKey<Fluid> tag, int amount) {
    return addInput(SizedFluidIngredient.of(tag, amount));
  }


  /* Building */

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.FLUID.getKey(this.output.get().getFluid()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (inputs.size() < 2) {
      throw new IllegalStateException("Invalid alloying recipe " + id + ", must have at least two inputs");
    }
    LoadableFinishedRecipe.save(output, id, AlloyRecipe.LOADER, new AlloyRecipe(id, inputs, this.output, temperature), buildOptionalAdvancement(output, id, "alloys"));
  }
}

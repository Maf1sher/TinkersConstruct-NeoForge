package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.tconstruct.TConstruct;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Fluid ingredient matching a fluid by ID, allowing datagen to reference fluids from mods that are not loaded.
 * Replaces the removed Mantle helper of the same name; JSON is {@code {"type": "tconstruct:fluid_name", "fluid": "<id>"}}.
 */
public class FluidNameIngredient extends FluidIngredient {
  /** Ingredient type ID, registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("fluid_name");

  /** Codec only storing the fluid ID so unregistered fluids can still be named */
  public static final MapCodec<FluidNameIngredient> CODEC = ResourceLocation.CODEC
    .fieldOf("fluid")
    .xmap(FluidNameIngredient::new, ingredient -> ingredient.fluid);

  /** Ingredient type, replaces the old Forge serializer */
  public static final FluidIngredientType<FluidNameIngredient> TYPE = new FluidIngredientType<>(CODEC);

  private final ResourceLocation fluid;

  public FluidNameIngredient(ResourceLocation fluid) {
    this.fluid = fluid;
  }

  /** Creates a sized ingredient of the given fluid ID and amount */
  public static SizedFluidIngredient of(ResourceLocation fluid, int amount) {
    return new SizedFluidIngredient(new FluidNameIngredient(fluid), amount);
  }

  /** @return the named fluid ID */
  public ResourceLocation getFluidId() {
    return fluid;
  }

  @Override
  public boolean test(FluidStack stack) {
    return BuiltInRegistries.FLUID.getKey(stack.getFluid()).equals(fluid);
  }

  @Override
  protected Stream<FluidStack> generateStacks() {
    // only display registered fluids; unregistered names exist for mod compat datagen
    Fluid value = BuiltInRegistries.FLUID.get(fluid);
    if (value == null || value == Fluids.EMPTY) {
      return Stream.empty();
    }
    return Stream.of(new FluidStack(value, FluidType.BUCKET_VOLUME));
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public FluidIngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public int hashCode() {
    return fluid.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return this == other || (other instanceof FluidNameIngredient ingredient && Objects.equals(fluid, ingredient.fluid));
  }
}

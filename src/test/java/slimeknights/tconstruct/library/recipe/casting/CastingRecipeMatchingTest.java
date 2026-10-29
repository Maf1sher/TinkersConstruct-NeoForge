package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Casting tanks fill in small increments (a faucet transfers at most a nugget per tick), so casting recipes must match
 * the fluid identity only and read the required amount from {@link ICastingRecipe#getFluidAmount(ICastingContainer)}.
 */
class CastingRecipeMatchingTest extends BaseMcTest {
  @SuppressWarnings("unchecked")
  private static TypeAwareRecipeSerializer<?> serializer() {
    TypeAwareRecipeSerializer<?> serializer = mock(TypeAwareRecipeSerializer.class);
    doReturn(RecipeType.CRAFTING).when(serializer).getType();
    return serializer;
  }

  private static ICastingContainer container(ItemStack stack, Fluid fluid, int amount) {
    return new ICastingContainer() {
      @Override
      public ItemStack getStack() {
        return stack;
      }

      @Override
      public Fluid getFluid() {
        return fluid;
      }

      @Override
      public FluidStack getFluidStack() {
        return new FluidStack(fluid, amount);
      }
    };
  }

  @Test
  void castingContainerWithOnlyFluidIsNotEmpty() {
    // RecipeManager skips the lookup for empty inputs, which would hide cast-less recipes such as block casting
    assertThat(container(ItemStack.EMPTY, Fluids.WATER, 10).isEmpty()).isFalse();
    assertThat(container(ItemStack.EMPTY, Fluids.EMPTY, 0).isEmpty()).isTrue();
  }

  @Test
  void itemCastingMatchesPartialFluidAmount() {
    ItemCastingRecipe recipe = new ItemCastingRecipe(serializer(),
      ResourceLocation.fromNamespaceAndPath("tconstruct", "test/casting_block"), "",
      Ingredient.EMPTY, SizedFluidIngredient.of(Fluids.WATER, 810),
      ItemOutput.fromItem(Items.IRON_BLOCK), 180, false, false);
    ICastingContainer partial = container(ItemStack.EMPTY, Fluids.WATER, 10);

    assertThat(recipe.matches(partial, null)).isTrue();
    assertThat(recipe.getFluidAmount(partial)).isEqualTo(810);
    assertThat(recipe.matches(container(ItemStack.EMPTY, Fluids.LAVA, 10), null)).isFalse();
  }

  @Test
  void potionCastingMatchesPartialFluidAmount() {
    PotionCastingRecipe recipe = new PotionCastingRecipe(serializer(),
      ResourceLocation.fromNamespaceAndPath("tconstruct", "test/filling_bottle"), "",
      Ingredient.of(Items.GLASS_BOTTLE), SizedFluidIngredient.of(Fluids.WATER, 250),
      Items.POTION, 5);
    ICastingContainer partial = container(new ItemStack(Items.GLASS_BOTTLE), Fluids.WATER, 10);

    assertThat(recipe.matches(partial, null)).isTrue();
    assertThat(recipe.getFluidAmount(partial)).isEqualTo(250);
    assertThat(recipe.matches(container(new ItemStack(Items.GLASS_BOTTLE), Fluids.LAVA, 10), null)).isFalse();
  }
}

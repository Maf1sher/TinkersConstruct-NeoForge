package slimeknights.tconstruct.plugin.jei.material;

import com.google.common.collect.Streams;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.recipe.material.MaterialsCraftingTableRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapelessMaterialsRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.MaterialIdNBT;
import slimeknights.tconstruct.plugin.jei.util.CategoryUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Common logic for {@link ShapedMaterialsExtension} and {@link ShapelessMaterialsExtension} */
public class MaterialsCraftingExtension<T extends CraftingRecipe & MaterialsCraftingTableRecipe> implements ICraftingCategoryExtension<T> {
  private static final String RESULT_SLOT = "result";

  /** Display data derived from a single recipe, cached per recipe as JEI reuses the extension instance for all recipes of the class */
  protected record RecipeData(ItemStack plainResult, List<ItemStack> result, @Nullable int[] outputLink, List<int[]> partSlots) {}

  private final Map<ResourceLocation, RecipeData> dataCache = new ConcurrentHashMap<>();

  /** If false, the recipe is invalid for display and will not render any contents */
  protected boolean isValid(T recipe) {
    return true;
  }

  /** Gets the display data for the given recipe, computing it on demand */
  protected RecipeData getData(RecipeHolder<T> holder) {
    return dataCache.computeIfAbsent(holder.id(), id -> createData(holder.value()));
  }

  @SuppressWarnings("deprecation")
  private RecipeData createData(T recipe) {
    ItemStack plainResult = recipe.getResultItem(Objects.requireNonNull(SafeClientAccess.getRegistryAccess()));
    // if we have just the one part, set the output to match its material
    if (recipe.getPartCount() == 1) {
      Ingredient firstPart = recipe.getParts().get(0);
      List<ItemStack> result = Arrays.stream(firstPart.getItems()).map(variant -> {
        ItemStack stack = plainResult.copy();
        recipe.setMaterial(stack, MaterialRecipeCache.getMaterial(variant));
        return stack;
      }).toList();
      return new RecipeData(plainResult, result, getMaterialSlots(recipe, firstPart), List.of());
    }
    // otherwise, use a display material. allow display tool part if it has just 1 material
    // just set a display tool, it should never be seen
    List<ItemStack> result = List.of(IModifiableDisplay.getDisplayStack(plainResult));
    List<int[]> partSlots = recipe.getParts().stream().map(part -> getMaterialSlots(recipe, part)).toList();
    return new RecipeData(plainResult, result, null, partSlots);
  }

  /** Gets the material slots for the given recipe from {@link CraftingRecipe#getIngredients()} */
  protected int[] getMaterialSlots(T recipe, Ingredient firstPart) {
    return new int[] {0};
  }

  @Override
  public Optional<ResourceLocation> getRegistryName(RecipeHolder<T> holder) {
    return Optional.of(holder.id());
  }

  /** Gets a modifiable list of inputs to the given recipe */
  private static List<List<ItemStack>> getInputs(CraftingRecipe recipe) {
    // using Collectors.toList to ensure we can mutate the list
    return recipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).collect(Collectors.toList());
  }

  /** Sets the recipe in the builder */
  @Deprecated
  @SuppressWarnings("removal")
  public static void setRecipe(ICraftingCategoryExtension<?> self, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, CraftingRecipe recipe, List<ItemStack> result, ItemStack plainResult, @Nullable int[] materialSlots) {
    setRecipe(builder, craftingGridHelper, recipe, getInputs(recipe), result, plainResult, materialSlots);
  }

  /** Sets the recipe in the builder */
  public static void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, CraftingRecipe recipe, List<List<ItemStack>> inputStacks, List<ItemStack> result, ItemStack plainResult, @Nullable int[] materialSlots) {
    builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(plainResult);

    // shapeless needs its width and height set, but we also want to recover those sizes, so calculate it locally
    int width = recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped ? shaped.getWidth() : 0;
    int height = recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped ? shaped.getHeight() : 0;
    if (width <= 0 || height <= 0) {
      width = height = CategoryUtil.getShapelessSize(inputStacks.size());
      builder.setShapeless();
    }
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, inputStacks, width, height);
    IRecipeSlotBuilder output = craftingGridHelper.createAndSetOutputs(builder, result).setSlotName(RESULT_SLOT);
    if (inputs.size() != 9) {
      TConstruct.LOG.error("Failed to create focus link for {} as the layout {} is not 3x3", recipe, builder.getClass().getName());
    } else if (materialSlots != null) {
      // apply focus links
      int fWidth = width;
      int fHeight = height;
      CategoryUtil.createFocusLink(builder, Streams.concat(
        Stream.of(output),
        Arrays.stream(materialSlots).mapToObj(i -> inputs.get(MantleJEIConstants.getCraftingIndex(i, fWidth, fHeight)))
      ).toArray(IRecipeSlotBuilder[]::new));
    }
  }

  @Override
  public void setRecipe(RecipeHolder<T> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    T recipe = holder.value();
    if (!isValid(recipe)) {
      return;
    }
    RecipeData data = getData(holder);
    List<List<ItemStack>> inputs = getInputs(recipe);

    // if we have a focused tool, use it to filter the inputs to match
    // we just assume the first 9 slots are inputs to avoid needing to make a new list
    ItemStack focus = CategoryUtil.getResultItemFocus(focuses);
    // if we have an output focus, set the input slots to match
    if (!focus.isEmpty()) {
      MaterialIdNBT materials = MaterialIdNBT.from(focus);
      // loop through finding all overrides to display, but skip any that are not present on the too
      int partCount = data.partSlots().size();
      for (int i = 0; i < partCount; i++) {
        // find all inputs matching the material
        MaterialVariantId material = materials.getMaterial(i);
        List<ItemStack> matchingStacks = Arrays.stream(recipe.getParts().get(i).getItems())
          .filter(stack -> material.matchesVariant(MaterialRecipeCache.getMaterial(stack)))
          .toList();
        // if filtered to empty, just display the original stacks
        // this happens if the focus has materials that are not in this recipe
        if (!matchingStacks.isEmpty()) {
          for (int slot : data.partSlots().get(i)) {
            inputs.set(slot, matchingStacks);
          }
        }
      }
    }
    setRecipe(builder, craftingGridHelper, recipe, inputs, data.result(), data.plainResult(), data.outputLink());
  }

  @Override
  public void onDisplayedIngredientsUpdate(RecipeHolder<T> holder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
    T recipe = holder.value();
    RecipeData data = getData(holder);
    // don't care if only 1 part
    if (data.partSlots().size() > 1) {
      IRecipeSlotDrawable resultSlot = CategoryUtil.findSlot(recipeSlots, RESULT_SLOT);
      if (resultSlot != null) {
        int width = recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped ? shaped.getWidth() : 0;
        int height = recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped ? shaped.getHeight() : 0;
        if (width <= 0 || height <= 0) {
          width = height = CategoryUtil.getShapelessSize(recipe.getIngredients().size());
        }
        // find input materials and use to set the output
        List<MaterialVariantId> variants = new ArrayList<>(data.partSlots().size());
        for (int[] partSlot : data.partSlots()) {
          ItemStack stack = recipeSlots.get(MantleJEIConstants.getCraftingIndex(partSlot[0], width, height)).getDisplayedItemStack().orElse(ItemStack.EMPTY);
          variants.add(MaterialRecipeCache.getMaterial(stack));
        }
        variants.addAll(recipe.getExtraMaterials());
        resultSlot.createDisplayOverrides().addItemStack(new MaterialIdNBT(variants).updateStack(data.plainResult().copy()));
      }
    }
  }
}

package slimeknights.tconstruct.tools.recipe;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modules.cosmetic.TrimModule;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArmorTrimRecipe implements ITinkerStationRecipe, IMultiRecipe<IDisplayModifierRecipe> {
  protected static final String KEY_INVALID_MATERIAL = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_material");
  protected static final String KEY_INVALID_PATTERN = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_pattern");


  /** Recipe ID used for display purposes */
  @Getter
  private final ResourceLocation id;

  /** No-arg constructor for use with {@link slimeknights.mantle.recipe.helper.SimpleRecipeSerializer} */
  public ArmorTrimRecipe() {
    this.id = TinkerModifiers.trim.getId();
    ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.trim);
  }

  /** @deprecated recipes no longer carry IDs in 1.21, use no-arg constructor */
  @Deprecated
  public ArmorTrimRecipe(ResourceLocation id) {
    this.id = id;
    ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.trim);
  }

  /** Match for the trim item finding method */
  private record TrimItems(ItemStack template, ItemStack material) {}

  /** Finds the trim template and material */
  @Nullable
  private static TrimItems findInputs(ITinkerStationContainer inv) {
    ItemStack template = ItemStack.EMPTY;
    ItemStack material = ItemStack.EMPTY;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // find the two matching tags, but ensure no duplicates
        if (stack.is(ItemTags.TRIM_TEMPLATES)) {
          if (!template.isEmpty()) {
            return null;
          }
          template = stack;
        }
        if (stack.is(ItemTags.TRIM_MATERIALS)) {
          if (!material.isEmpty()) {
            return null;
          }
          material = stack;
        }
      }
    }
    // if we found both, we match
    if (!material.isEmpty() && !template.isEmpty()) {
      return new TrimItems(template, material);
    }
    return null;
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    // ensure this modifier can be applied
    if (!inv.getTinkerableStack().is(TinkerTags.Items.TRIM)) {
      return false;
    }
    // need to locate two things: the trim material, and the trim template
    return findInputs(inv) != null;
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, net.minecraft.core.RegistryAccess access) {
    // first need to find our trim and material instances
    TrimItems trimItems = findInputs(inv);
    // should never happen
    if (trimItems == null) {
      return RecipeResult.pass();
    }
    // validate the material and pattern items
    Optional<Reference<TrimMaterial>> material = TrimMaterials.getFromIngredient(access, trimItems.material);
    if (material.isEmpty()) {
      return RecipeResult.failure(KEY_INVALID_MATERIAL, trimItems.material.getDisplayName());
    }
    Optional<Reference<TrimPattern>> pattern = TrimPatterns.getFromTemplate(access, trimItems.template);
    if (pattern.isEmpty()) {
      return RecipeResult.failure(KEY_INVALID_PATTERN, trimItems.template.getDisplayName());
    }

    // store into tool NBT
    ToolStack tool = inv.getTinkerable().copy();
    ModDataNBT persistentData = tool.getPersistentData();
    ModifierId modifier = TinkerModifiers.trim.getModifierId();
    persistentData.putString(TrimModule.materialKey(modifier), material.get().key().location().toString());
    persistentData.putString(TrimModule.patternKey(modifier), pattern.get().key().location().toString());

    // add the modifier if missing
    if (tool.getModifierLevel(modifier) == 0) {
      tool.addModifier(modifier, 1);
    }
    return ITinkerStationRecipe.success(tool, inv);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.armorTrimSerializer.get();
  }


  /* JEI */

  private List<IDisplayModifierRecipe> displayRecipes = null;

  @SuppressWarnings("deprecation")
  @Override
  public List<IDisplayModifierRecipe> getRecipes(HolderLookup.Provider access) {
    if (displayRecipes == null) {
      List<ItemStack> trims = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, ItemTags.TRIM_TEMPLATES)
                                            .map(ItemStack::new).toList();
      List<ItemStack> toolInputs = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.TRIM)
                                                 .map(IModifiableDisplay::getDisplayStack).toList();
      ResourceLocation id = getId();
      // RegistryAccess extends HolderLookup.Provider, but we receive HolderLookup.Provider here
      // Use lookupOrThrow to get the trim material registry
      displayRecipes = access.lookupOrThrow(Registries.TRIM_MATERIAL).listElements()
                             .map(material -> new DisplayRecipe(id, toolInputs, trims, material))
                             .collect(Collectors.toList());
    }
    return displayRecipes;
  }

  private static class DisplayRecipe implements IDisplayModifierRecipe {
    private static final IntRange LEVELS = new IntRange(1, 1);
    private final ModifierEntry RESULT = new ModifierEntry(TinkerModifiers.trim, 1);

    @Getter
    private final ResourceLocation recipeId;
    @Getter
    private final List<ItemStack> toolWithoutModifier;
    @Getter
    private final List<ItemStack> toolWithModifier;
    private final List<ItemStack> trim;
    private final List<ItemStack> material;
    @Getter
    private final Component variant;

    public DisplayRecipe(ResourceLocation id, List<ItemStack> tools, List<ItemStack> trim, Reference<TrimMaterial> holder) {
      this.recipeId = id;
      TrimMaterial material = holder.value();
      toolWithoutModifier = tools;
      this.trim = trim;
      this.material = List.of(new ItemStack(material.ingredient().value()));
      this.variant = material.description().plainCopy();

      String materialName = holder.key().location().toString();
      List<ModifierEntry> results = List.of(RESULT);
      ResourceLocation key = TrimModule.materialKey(TinkerModifiers.trim.getModifierId());
      toolWithModifier = tools.stream().map(stack -> IDisplayModifierRecipe.withModifiers(stack, results, data -> data.putString(key, materialName))).toList();

    }

    @Override
    public int getInputCount() {
      return 2;
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
      return switch (slot) {
        case 0 -> trim;
        case 1 -> material;
        default -> List.of();
      };
    }

    @Override
    public ModifierEntry getDisplayResult() {
      return RESULT;
    }

    @Override
    public IntRange getLevel() {
      return LEVELS;
    }
  }
}

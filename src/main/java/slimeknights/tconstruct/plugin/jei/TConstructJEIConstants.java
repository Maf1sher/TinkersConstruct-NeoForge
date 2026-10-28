package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.LazyMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.material.IDisplayMaterialRecipe;
import slimeknights.tconstruct.library.recipe.melting.IDisplayableMeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.display.DisplayRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.recipe.tinkerstation.IDisplayToolTinkering;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;

public class TConstructJEIConstants {
  public static final ResourceLocation PLUGIN = TConstruct.getResource("jei_plugin");

  // ingredient types
  public static final IIngredientTypeWithSubtypes<Modifier,ModifierEntry> MODIFIER_TYPE = new IIngredientTypeWithSubtypes<>() {
    @Override
    public Class<? extends ModifierEntry> getIngredientClass() {
      return ModifierEntry.class;
    }

    @Override
    public Class<? extends Modifier> getIngredientBaseClass() {
      return Modifier.class;
    }

    @Override
    public Modifier getBase(ModifierEntry ingredient) {
      return ingredient.getModifier();
    }
  };
  public static final IIngredientType<Pattern> PATTERN_TYPE = () -> Pattern.class;
  public static final IIngredientTypeWithSubtypes<SlotType, SlotCount> SLOT_TYPE = new IIngredientTypeWithSubtypes<>() {
    @Override
    public Class<? extends SlotCount> getIngredientClass() {
      return SlotCount.class;
    }

    @Override
    public Class<? extends SlotType> getIngredientBaseClass() {
      return SlotType.class;
    }

    @Override
    public SlotType getBase(SlotCount slots) {
      return slots.type();
    }
  };
  public static final IIngredientTypeWithSubtypes<LazyMaterial, MaterialVariant> MATERIAL_TYPE = new IIngredientTypeWithSubtypes<>() {
    @Override
    public Class<? extends MaterialVariant> getIngredientClass() {
      return MaterialVariant.class;
    }

    @Override
    public Class<? extends LazyMaterial> getIngredientBaseClass() {
      return LazyMaterial.class;
    }

    @Override
    public LazyMaterial getBase(MaterialVariant variant) {
      return variant.getBase();
    }
  };

  // casting
  public static final RecipeType<DisplayRecipe<IDisplayableCastingRecipe>> CASTING_BASIN = type("casting_basin");
  public static final RecipeType<DisplayRecipe<IDisplayableCastingRecipe>> CASTING_TABLE = type("casting_table");
  public static final RecipeType<DisplayRecipe<MoldingRecipe>> MOLDING = type("molding");

  // melting
  public static final RecipeType<DisplayRecipe<IDisplayableMeltingRecipe>> MELTING = type("melting");
  public static final RecipeType<DisplayRecipe<EntityMeltingRecipe>> ENTITY_MELTING = type("entity_melting");
  public static final RecipeType<DisplayRecipe<AlloyRecipe>> ALLOY = type("alloy");
  public static final RecipeType<DisplayRecipe<IDisplayableMeltingRecipe>> FOUNDRY = type("foundry");
  public static final RecipeType<DisplayRecipe<MeltingFuel>> FUEL = type("fuel");

  // tinker station
  public static final RecipeType<DisplayRecipe<IDisplayModifierRecipe>> MODIFIERS = type("modifiers");
  public static final RecipeType<DisplayRecipe<SeveringRecipe>> SEVERING = type("severing");
  public static final RecipeType<DisplayRecipe<ToolBuildingRecipe>> TOOL_BUILDING = type("tool_recipes");
  public static final RecipeType<DisplayRecipe<IDisplayToolTinkering>> TOOL_MODIFICATION = type("tool_modification");

  // part builder
  public static final RecipeType<DisplayRecipe<IDisplayMaterialRecipe>> MATERIALS = type("materials");
  public static final RecipeType<DisplayRecipe<IDisplayPartBuilderRecipe>> PART_BUILDER = type("part_builder");

  // modifier workstation
  public static final RecipeType<DisplayRecipe<IModifierWorktableRecipe>> MODIFIER_WORKTABLE = type("worktable");

  /** Creates a recipe type for display recipes, keeping the recipe IDs available to JEI. */
  @SuppressWarnings("unchecked")
  private static <T> RecipeType<DisplayRecipe<T>> type(String name) {
    return RecipeType.create(TConstruct.MOD_ID, name, (Class<DisplayRecipe<T>>) (Class<?>) DisplayRecipe.class);
  }
}

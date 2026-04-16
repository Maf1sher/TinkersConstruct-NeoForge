package slimeknights.tconstruct.plugin.jei;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
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

  // casting
  public static final RecipeType<IDisplayableCastingRecipe> CASTING_BASIN = type("casting_basin", IDisplayableCastingRecipe.class);
  public static final RecipeType<IDisplayableCastingRecipe> CASTING_TABLE = type("casting_table", IDisplayableCastingRecipe.class);
  public static final RecipeType<RecipeHolder<MoldingRecipe>> MOLDING = holderType("molding");

  // melting
  public static final RecipeType<RecipeHolder<MeltingRecipe>> MELTING = holderType("melting");
  public static final RecipeType<RecipeHolder<EntityMeltingRecipe>> ENTITY_MELTING = holderType("entity_melting");
  public static final RecipeType<RecipeHolder<AlloyRecipe>> ALLOY = holderType("alloy");
  public static final RecipeType<RecipeHolder<MeltingRecipe>> FOUNDRY = holderType("foundry");

  // tinker station
  public static final RecipeType<IDisplayModifierRecipe> MODIFIERS = type("modifiers", IDisplayModifierRecipe.class);
  public static final RecipeType<RecipeHolder<SeveringRecipe>> SEVERING = holderType("severing");
  public static final RecipeType<RecipeHolder<ToolBuildingRecipe>> TOOL_BUILDING = holderType("tool_recipes");

  // part builder
  public static final RecipeType<IDisplayPartBuilderRecipe> PART_BUILDER = type("part_builder", IDisplayPartBuilderRecipe.class);

  // modifier workstation
  public static final RecipeType<IModifierWorktableRecipe> MODIFIER_WORKTABLE = type("worktable", IModifierWorktableRecipe.class);

  private static <T> RecipeType<T> type(String name, Class<T> clazz) {
    return RecipeType.create(TConstruct.MOD_ID, name, clazz);
  }

  private static <T extends Recipe<?>> RecipeType<RecipeHolder<T>> holderType(String name) {
    return RecipeType.createRecipeHolderType(TConstruct.getResource(name));
  }
}

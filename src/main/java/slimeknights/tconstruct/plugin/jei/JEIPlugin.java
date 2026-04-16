package slimeknights.tconstruct.plugin.jei;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.fml.ModList;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.fluids.PotionFluidType;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapedMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.material.ShapelessMaterialsRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.plugin.jei.casting.CastingBasinCategory;
import slimeknights.tconstruct.plugin.jei.casting.CastingTableCategory;
import slimeknights.tconstruct.plugin.jei.entity.DefaultEntityMeltingRecipe;
import slimeknights.tconstruct.plugin.jei.entity.EntityMeltingRecipeCategory;
import slimeknights.tconstruct.plugin.jei.entity.SeveringCategory;
import slimeknights.tconstruct.plugin.jei.material.MaterialsCraftingExtension;
import slimeknights.tconstruct.plugin.jei.material.ShapedMaterialsExtension;
import slimeknights.tconstruct.plugin.jei.melting.FoundryCategory;
import slimeknights.tconstruct.plugin.jei.melting.MeltingCategory;
import slimeknights.tconstruct.plugin.jei.melting.MeltingFuelHandler;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierBookmarkIngredientRenderer;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierIngredientHelper;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierRecipeCategory;
import slimeknights.tconstruct.plugin.jei.modifiers.ModifierWorktableCategory;
import slimeknights.tconstruct.plugin.jei.modifiers.SlotIngredientHelper;
import slimeknights.tconstruct.plugin.jei.modifiers.SlotIngredientRenderer;
import slimeknights.tconstruct.plugin.jei.partbuilder.MaterialItemList;
import slimeknights.tconstruct.plugin.jei.partbuilder.PartBuilderCategory;
import slimeknights.tconstruct.plugin.jei.partbuilder.PatternIngredientHelper;
import slimeknights.tconstruct.plugin.jei.partbuilder.PatternIngredientRenderer;
import slimeknights.tconstruct.plugin.jei.transfer.CraftingStationTransferInfo;
import slimeknights.tconstruct.plugin.jei.transfer.TinkerStationTransferInfo;
import slimeknights.tconstruct.plugin.jei.transfer.ToolInventoryTransferInfo;
import slimeknights.tconstruct.plugin.jei.util.GuiContainerTankHandler;
import slimeknights.tconstruct.plugin.jei.util.ToolPartSubtypeInterpreter;
import slimeknights.tconstruct.plugin.jei.util.ToolSubtypeInterpreter;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.client.screen.AlloyerScreen;
import slimeknights.tconstruct.smeltery.client.screen.HeatingStructureScreen;
import slimeknights.tconstruct.smeltery.client.screen.MelterScreen;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.client.ToolContainerScreen;
import slimeknights.tconstruct.tools.item.CreativeSlotItem;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static slimeknights.mantle.Mantle.commonResource;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  /** Recipes that are meant as jokes and tend to confuse players, so are hidden */
  private static final ResourceLocation[] EASTER_EGG_RECIPES = {
    TConstruct.getResource("tables/tinkers_forge"),
    TConstruct.getResource("tables/scorched_forge"),
    TConstruct.getResource("tables/seared_forge_material"),
    TConstruct.getResource("tables/scorched_forge_material")
  };
  public static IModIdHelper modIdHelper;

  @Override
  public ResourceLocation getPluginUid() {
    return TConstructJEIConstants.PLUGIN;
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registry) {
    final IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
    // casting
    registry.addRecipeCategories(new CastingBasinCategory(guiHelper));
    registry.addRecipeCategories(new CastingTableCategory(guiHelper));
    registry.addRecipeCategories(new MoldingRecipeCategory(guiHelper));
    // melting and casting
    registry.addRecipeCategories(new MeltingCategory(guiHelper));
    registry.addRecipeCategories(new AlloyRecipeCategory(guiHelper));
    registry.addRecipeCategories(new EntityMeltingRecipeCategory(guiHelper));
    registry.addRecipeCategories(new FoundryCategory(guiHelper));
    // tinker station
    registry.addRecipeCategories(new ModifierRecipeCategory(guiHelper));
    registry.addRecipeCategories(new SeveringCategory(guiHelper));
    registry.addRecipeCategories(new ToolBuildingCategory(guiHelper));
    // part builder
    registry.addRecipeCategories(new PartBuilderCategory(guiHelper));
    // modifier worktable
    registry.addRecipeCategories(new ModifierWorktableCategory(guiHelper));
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registration) {
    List<ModifierEntry> modifiers = Collections.emptyList();
    if (Config.CLIENT.showModifiersInJEI.get()) {
      modifiers = ModifierRecipeLookup.getRecipeModifierList();
    }
    registration.register(TConstructJEIConstants.MODIFIER_TYPE, modifiers, new ModifierIngredientHelper(), ModifierBookmarkIngredientRenderer.INSTANCE);
    registration.register(TConstructJEIConstants.PATTERN_TYPE, Collections.emptyList(), new PatternIngredientHelper(), PatternIngredientRenderer.INSTANCE);
    List<SlotCount> slots = SlotType.getAllSlotTypes().stream().map(type -> new SlotCount(type, 1)).toList();
    SlotIngredientRenderer.clearCache();
    registration.register(TConstructJEIConstants.SLOT_TYPE, slots, new SlotIngredientHelper(), SlotIngredientRenderer.INGREDIENT);
  }

  @Override
  public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registry) {
    registry.getCraftingCategory().addExtension(ShapedMaterialRecipe.class, new ShapedMaterialExtension());
    registry.getCraftingCategory().addExtension(ShapedMaterialsRecipe.class, new ShapedMaterialsExtension());
    registry.getCraftingCategory().addExtension(ShapelessMaterialsRecipe.class, new MaterialsCraftingExtension<>());
  }

  @Override
  public void registerRecipes(IRecipeRegistration register) {
    RegistryAccess access = SafeClientAccess.getRegistryAccess();
    RecipeManager manager = getClientRecipeManager();
    if (access == null || manager == null) {
      TConstruct.LOG.warn("Skipping JEI recipe registration because the client registry access or recipe manager is unavailable");
      return;
    }
    logRawClientRecipeState("register", manager);
    // casting
    List<IDisplayableCastingRecipe> castingBasinRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.CASTING_BASIN.get(), IDisplayableCastingRecipe.class);
    register.addRecipes(TConstructJEIConstants.CASTING_BASIN, castingBasinRecipes);
    List<IDisplayableCastingRecipe> castingTableRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.CASTING_TABLE.get(), IDisplayableCastingRecipe.class);
    register.addRecipes(TConstructJEIConstants.CASTING_TABLE, castingTableRecipes);

    // melting
    List<MeltingRecipe> meltingRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.MELTING.get(), MeltingRecipe.class);
    register.addRecipes(TConstructJEIConstants.MELTING, wrapRecipes(meltingRecipes, MeltingRecipe::getId));
    register.addRecipes(TConstructJEIConstants.FOUNDRY, wrapRecipes(meltingRecipes, MeltingRecipe::getId));
    MeltingFuelHandler.setMeltngFuels(RecipeHelper.getRecipes(manager, TinkerRecipeTypes.FUEL.get(), MeltingFuel.class));

    // entity melting
    List<EntityMeltingRecipe> entityMeltingRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.ENTITY_MELTING.get(), EntityMeltingRecipe.class);
    // generate a "default" recipe for all other entity types
    entityMeltingRecipes.add(new DefaultEntityMeltingRecipe(entityMeltingRecipes));
    register.addRecipes(TConstructJEIConstants.ENTITY_MELTING, wrapRecipes(entityMeltingRecipes, EntityMeltingRecipe::getId));

    // alloying
    List<AlloyRecipe> alloyRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.ALLOYING.get(), AlloyRecipe.class);
    register.addRecipes(TConstructJEIConstants.ALLOY, wrapRecipes(alloyRecipes, AlloyRecipe::getId));

    // molding
    List<MoldingRecipe> moldingRecipes = ImmutableList.<MoldingRecipe>builder()
      .addAll(getRecipesByType(access, manager, TinkerRecipeTypes.MOLDING_TABLE.get(), MoldingRecipe.class))
      .addAll(getRecipesByType(access, manager, TinkerRecipeTypes.MOLDING_BASIN.get(), MoldingRecipe.class))
      .build();
    register.addRecipes(TConstructJEIConstants.MOLDING, wrapRecipes(moldingRecipes, MoldingRecipe::getId));

    // modifiers
    List<IDisplayModifierRecipe> modifierRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.TINKER_STATION.get(), IDisplayModifierRecipe.class)
                                                               .stream()
                                                               .sorted((r1, r2) -> {
                                                                 SlotType t1 = r1.getSlotType();
                                                                 SlotType t2 = r2.getSlotType();
                                                                 String n1 = t1 == null ? "zzzzzzzzzz" : t1.getName();
                                                                 String n2 = t2 == null ? "zzzzzzzzzz" : t2.getName();
                                                                 return n1.compareTo(n2);
                                                               }).collect(Collectors.toList());
    register.addRecipes(TConstructJEIConstants.MODIFIERS, modifierRecipes);

    // beheading
    List<SeveringRecipe> severingRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.SEVERING.get(), SeveringRecipe.class);
    register.addRecipes(TConstructJEIConstants.SEVERING, wrapRecipes(severingRecipes, SeveringRecipe::getId));

    // tool building
    List<ToolBuildingRecipe> toolBuilding = getRecipesByType(access, manager, TinkerRecipeTypes.TINKER_STATION.get(), ToolBuildingRecipe.class)
      .stream()
      .sorted(Comparator.comparingInt(r -> StationSlotLayoutLoader.getInstance().get(r.getLayoutSlotId()).getSortIndex()))
      .toList();
    register.addRecipes(TConstructJEIConstants.TOOL_BUILDING, wrapRecipes(toolBuilding, ToolBuildingRecipe::getId));

    // part builder
    List<IDisplayPartBuilderRecipe> partBuilderRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.PART_BUILDER.get(), IDisplayPartBuilderRecipe.class);
    MaterialItemList.setRecipes(List.of()); // list of recipes is ignored as this whole class is getting ditched in 1.21; it just clears cache right now
    register.addRecipes(TConstructJEIConstants.PART_BUILDER, partBuilderRecipes);

    // modifier worktable
    List<IModifierWorktableRecipe> modifierWorktableRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), IModifierWorktableRecipe.class);
    register.addRecipes(TConstructJEIConstants.MODIFIER_WORKTABLE, modifierWorktableRecipes);

    TConstruct.LOG.info("JEI recipe registration counts: casting_basin={}, casting_table={}, melting={}, entity_melting={}, alloy={}, molding={}, modifiers={}, severing={}, tool_building={}, part_builder={}, modifier_worktable={}",
      castingBasinRecipes.size(),
      castingTableRecipes.size(),
      meltingRecipes.size(),
      entityMeltingRecipes.size(),
      alloyRecipes.size(),
      moldingRecipes.size(),
      modifierRecipes.size(),
      severingRecipes.size(),
      toolBuilding.size(),
        partBuilderRecipes.size(),
        modifierWorktableRecipes.size());
  }

  private static <T extends net.minecraft.world.item.crafting.Recipe<?>> List<RecipeHolder<T>> wrapRecipes(List<T> recipes, Function<T,ResourceLocation> idGetter) {
    return recipes.stream().map(recipe -> new RecipeHolder<>(idGetter.apply(recipe), recipe)).toList();
  }

  /**
   * Gets recipes by scanning the full client recipe list and filtering by recipe type.
   * This mirrors Create's approach more closely and avoids relying on RecipeManager's per-type index.
   */
  private static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>, C> List<C> getRecipesByType(RegistryAccess access, RecipeManager manager, RecipeType<T> type, Class<C> clazz) {
    return RecipeHelper.getJEIRecipes(access, manager.getRecipes().stream().filter(holder -> holder.value().getType() == type), clazz);
  }

  /** Logs what the raw client recipe manager contains before JEI filtering. */
  private static void logRawClientRecipeState(String phase, RecipeManager manager) {
    Collection<RecipeHolder<?>> allRecipes = manager.getRecipes();
    long tconstructIds = allRecipes.stream().filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID)).count();
    long meltingClass = allRecipes.stream().filter(holder -> holder.value() instanceof MeltingRecipe).count();
    long alloyClass = allRecipes.stream().filter(holder -> holder.value() instanceof AlloyRecipe).count();
    long moldingClass = allRecipes.stream().filter(holder -> holder.value() instanceof MoldingRecipe).count();
    long toolBuildingClass = allRecipes.stream().filter(holder -> holder.value() instanceof ToolBuildingRecipe).count();
    long partBuilderClass = allRecipes.stream().filter(holder -> holder.value() instanceof IDisplayPartBuilderRecipe).count();
    long modifierWorktableClass = allRecipes.stream().filter(holder -> holder.value() instanceof IModifierWorktableRecipe).count();
    long modifierClass = allRecipes.stream().filter(holder -> holder.value() instanceof IDisplayModifierRecipe).count();
    long severingClass = allRecipes.stream().filter(holder -> holder.value() instanceof SeveringRecipe).count();
    long castingClass = allRecipes.stream().filter(holder -> holder.value() instanceof IDisplayableCastingRecipe).count();

    Map<String, Long> tconstructTypeCounts = allRecipes.stream()
      .filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID))
      .collect(Collectors.groupingBy((RecipeHolder<?> holder) -> {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
        return key == null ? "unregistered_type" : key.toString();
      }, LinkedHashMap::new, Collectors.counting()));

    String sampleIds = allRecipes.stream()
      .filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID))
      .limit(12)
      .map(holder -> holder.id() + "(" + holder.value().getClass().getSimpleName() + ")")
      .collect(Collectors.joining(", "));

    TConstruct.LOG.info("JEI raw client recipe state [{}]: total={}, tconstruct_ids={}, MeltingRecipe={}, AlloyRecipe={}, MoldingRecipe={}, ToolBuildingRecipe={}, IDisplayPartBuilderRecipe={}, IModifierWorktableRecipe={}, IDisplayModifierRecipe={}, SeveringRecipe={}, IDisplayableCastingRecipe={}",
      phase,
      allRecipes.size(),
      tconstructIds,
      meltingClass,
      alloyClass,
      moldingClass,
      toolBuildingClass,
      partBuilderClass,
      modifierWorktableClass,
      modifierClass,
      severingClass,
      castingClass);
    TConstruct.LOG.info("JEI raw client tconstruct type counts [{}]: {}", phase, tconstructTypeCounts);
    TConstruct.LOG.info("JEI raw client tconstruct sample ids [{}]: {}", phase, sampleIds.isEmpty() ? "<none>" : sampleIds);

    IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
    if (server != null) {
      logRecipeManagerState("server", phase, server.getRecipeManager());
    }
  }

  /** Logs a recipe manager summary for comparing client vs integrated server recipe availability. */
  private static void logRecipeManagerState(String side, String phase, RecipeManager manager) {
    Collection<RecipeHolder<?>> allRecipes = manager.getRecipes();
    long tconstructIds = allRecipes.stream().filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID)).count();
    Map<String, Long> tconstructTypeCounts = allRecipes.stream()
      .filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID))
      .collect(Collectors.groupingBy((RecipeHolder<?> holder) -> {
        ResourceLocation key = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
        return key == null ? "unregistered_type" : key.toString();
      }, LinkedHashMap::new, Collectors.counting()));
    String sampleIds = allRecipes.stream()
      .filter(holder -> holder.id().getNamespace().equals(TConstruct.MOD_ID))
      .limit(12)
      .map(holder -> holder.id() + "(" + holder.value().getClass().getSimpleName() + ")")
      .collect(Collectors.joining(", "));

    TConstruct.LOG.info("JEI raw {} recipe state [{}]: total={}, tconstruct_ids={}", side, phase, allRecipes.size(), tconstructIds);
    TConstruct.LOG.info("JEI raw {} tconstruct type counts [{}]: {}", side, phase, tconstructTypeCounts);
    TConstruct.LOG.info("JEI raw {} tconstruct sample ids [{}]: {}", side, phase, sampleIds.isEmpty() ? "<none>" : sampleIds);
  }

  /** Rebuilds recipe lists from the current client recipe manager and injects them into JEI at runtime. */
  private static void addLateRecipes(IJeiRuntime jeiRuntime) {
    RegistryAccess access = SafeClientAccess.getRegistryAccess();
    RecipeManager manager = getClientRecipeManager();
    if (access == null || manager == null) {
      TConstruct.LOG.warn("JEI runtime recipe injection skipped because client registry access or recipe manager is unavailable");
      return;
    }
    logRawClientRecipeState("runtime", manager);

    List<IDisplayableCastingRecipe> castingBasinRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.CASTING_BASIN.get(), IDisplayableCastingRecipe.class);
    List<IDisplayableCastingRecipe> castingTableRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.CASTING_TABLE.get(), IDisplayableCastingRecipe.class);
    List<MeltingRecipe> meltingRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.MELTING.get(), MeltingRecipe.class);
    List<AlloyRecipe> alloyRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.ALLOYING.get(), AlloyRecipe.class);
    List<MoldingRecipe> moldingRecipes = ImmutableList.<MoldingRecipe>builder()
      .addAll(getRecipesByType(access, manager, TinkerRecipeTypes.MOLDING_TABLE.get(), MoldingRecipe.class))
      .addAll(getRecipesByType(access, manager, TinkerRecipeTypes.MOLDING_BASIN.get(), MoldingRecipe.class))
      .build();
    List<IDisplayModifierRecipe> modifierRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.TINKER_STATION.get(), IDisplayModifierRecipe.class)
                                                               .stream()
                                                               .sorted((r1, r2) -> {
                                                                 SlotType t1 = r1.getSlotType();
                                                                 SlotType t2 = r2.getSlotType();
                                                                 String n1 = t1 == null ? "zzzzzzzzzz" : t1.getName();
                                                                 String n2 = t2 == null ? "zzzzzzzzzz" : t2.getName();
                                                                 return n1.compareTo(n2);
                                                               }).collect(Collectors.toList());
    List<SeveringRecipe> severingRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.SEVERING.get(), SeveringRecipe.class);
    List<ToolBuildingRecipe> toolBuilding = getRecipesByType(access, manager, TinkerRecipeTypes.TINKER_STATION.get(), ToolBuildingRecipe.class)
      .stream()
      .sorted(Comparator.comparingInt(r -> StationSlotLayoutLoader.getInstance().get(r.getLayoutSlotId()).getSortIndex()))
      .toList();
    List<IDisplayPartBuilderRecipe> partBuilderRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.PART_BUILDER.get(), IDisplayPartBuilderRecipe.class);
    List<IModifierWorktableRecipe> modifierWorktableRecipes = getRecipesByType(access, manager, TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), IModifierWorktableRecipe.class);

    TConstruct.LOG.info("JEI late recipe counts: casting_basin={}, casting_table={}, melting={}, alloy={}, molding={}, modifiers={}, severing={}, tool_building={}, part_builder={}, modifier_worktable={}",
      castingBasinRecipes.size(),
      castingTableRecipes.size(),
      meltingRecipes.size(),
      alloyRecipes.size(),
      moldingRecipes.size(),
      modifierRecipes.size(),
      severingRecipes.size(),
      toolBuilding.size(),
      partBuilderRecipes.size(),
      modifierWorktableRecipes.size());

    var recipeManager = jeiRuntime.getRecipeManager();
    if (!castingBasinRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.CASTING_BASIN, castingBasinRecipes);
    }
    if (!castingTableRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.CASTING_TABLE, castingTableRecipes);
    }
    if (!meltingRecipes.isEmpty()) {
      List<RecipeHolder<MeltingRecipe>> wrappedMelting = wrapRecipes(meltingRecipes, MeltingRecipe::getId);
      recipeManager.addRecipes(TConstructJEIConstants.MELTING, wrappedMelting);
      recipeManager.addRecipes(TConstructJEIConstants.FOUNDRY, wrappedMelting);
    }
    if (!alloyRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.ALLOY, wrapRecipes(alloyRecipes, AlloyRecipe::getId));
    }
    if (!moldingRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.MOLDING, wrapRecipes(moldingRecipes, MoldingRecipe::getId));
    }
    if (!modifierRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.MODIFIERS, modifierRecipes);
    }
    if (!severingRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.SEVERING, wrapRecipes(severingRecipes, SeveringRecipe::getId));
    }
    if (!toolBuilding.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.TOOL_BUILDING, wrapRecipes(toolBuilding, ToolBuildingRecipe::getId));
    }
    if (!partBuilderRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.PART_BUILDER, partBuilderRecipes);
    }
    if (!modifierWorktableRecipes.isEmpty()) {
      recipeManager.addRecipes(TConstructJEIConstants.MODIFIER_WORKTABLE, modifierWorktableRecipes);
    }
  }

  /** Gets the recipe manager currently synchronized to the client */
  private static RecipeManager getClientRecipeManager() {
    ClientPacketListener connection = Minecraft.getInstance().getConnection();
    if (connection != null) {
      return connection.getRecipeManager();
    }
    Level level = SafeClientAccess.getLevel();
    return level != null ? level.getRecipeManager() : null;
  }

  /**
   * Adds an item as a casting catalyst, and as a molding catalyst if it has molding recipes
   * @param registry     Catalyst regisry
   * @param item         Item to add
   * @param ownCategory  Category to always add
   * @param type         Molding recipe type
   */
  private static void addCastingCatalyst(IRecipeCatalystRegistration registry, ItemLike item, mezz.jei.api.recipe.RecipeType<IDisplayableCastingRecipe> ownCategory, RecipeType<MoldingRecipe> type) {
    ItemStack stack = new ItemStack(item);
    registry.addRecipeCatalyst(stack, ownCategory);
    RecipeManager recipes = getClientRecipeManager();
    if (recipes != null && !recipes.byType(type).isEmpty()) {
      registry.addRecipeCatalyst(stack, TConstructJEIConstants.MOLDING);
    }
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
    // tables
    registry.addRecipeCatalyst(new ItemStack(TinkerTables.partBuilder), TConstructJEIConstants.PART_BUILDER);
    registry.addRecipeCatalyst(new ItemStack(TinkerTables.tinkerStation), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registry.addRecipeCatalyst(new ItemStack(TinkerTables.tinkersAnvil), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registry.addRecipeCatalyst(new ItemStack(TinkerTables.scorchedAnvil), TConstructJEIConstants.MODIFIERS, TConstructJEIConstants.TOOL_BUILDING);
    registry.addRecipeCatalyst(new ItemStack(TinkerTables.modifierWorktable), TConstructJEIConstants.MODIFIER_WORKTABLE);

    // smeltery
    registry.addRecipeCatalyst(new ItemStack(TinkerSmeltery.searedMelter), TConstructJEIConstants.MELTING);
    registry.addRecipeCatalyst(new ItemStack(TinkerSmeltery.searedHeater), RecipeTypes.FUELING);
    addCastingCatalyst(registry, TinkerSmeltery.searedTable, TConstructJEIConstants.CASTING_TABLE, TinkerRecipeTypes.MOLDING_TABLE.get());
    addCastingCatalyst(registry, TinkerSmeltery.searedBasin, TConstructJEIConstants.CASTING_BASIN, TinkerRecipeTypes.MOLDING_BASIN.get());
    registry.addRecipeCatalyst(new ItemStack(TinkerSmeltery.smelteryController), TConstructJEIConstants.MELTING, TConstructJEIConstants.ALLOY, TConstructJEIConstants.ENTITY_MELTING);

    // foundry
    registry.addRecipeCatalyst(new ItemStack(TinkerSmeltery.scorchedAlloyer), TConstructJEIConstants.ALLOY);
    addCastingCatalyst(registry, TinkerSmeltery.scorchedTable, TConstructJEIConstants.CASTING_TABLE, TinkerRecipeTypes.MOLDING_TABLE.get());
    addCastingCatalyst(registry, TinkerSmeltery.scorchedBasin, TConstructJEIConstants.CASTING_BASIN, TinkerRecipeTypes.MOLDING_BASIN.get());
    registry.addRecipeCatalyst(new ItemStack(TinkerSmeltery.foundryController), TConstructJEIConstants.FOUNDRY);

    // modifiers
    registry.addRecipeCatalyst(TConstructJEIConstants.MODIFIER_TYPE, new ModifierEntry(TinkerModifiers.severing, 1), TConstructJEIConstants.SEVERING);
    registry.addRecipeCatalyst(TConstructJEIConstants.MODIFIER_TYPE, new ModifierEntry(TinkerModifiers.melting, 1), TConstructJEIConstants.MELTING, TConstructJEIConstants.ENTITY_MELTING);
    for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MODIFIABLE)) {
      if (item.value() instanceof IModifiableDisplay modifiable) {
        // add any tools with a severing trait to severing
        ModifierNBT traits = ToolTraitHook.getTraits(modifiable.getToolDefinition(), MaterialNBT.EMPTY);
        if (traits.getLevel(new ModifierId(TinkerModifiers.severing.getId())) > 0) {
          registry.addRecipeCatalyst(modifiable.getRenderTool(), TConstructJEIConstants.SEVERING);
        }
        // add any tools with a melting trait to melting
        if (traits.getLevel(new ModifierId(TinkerModifiers.melting.getId())) > 0) {
          // only add to entity melting if its melee too
          if (item.is(TinkerTags.Items.MELEE)) {
            registry.addRecipeCatalyst(modifiable.getRenderTool(), TConstructJEIConstants.MELTING, TConstructJEIConstants.ENTITY_MELTING);
          } else {
            registry.addRecipeCatalyst(modifiable.getRenderTool(), TConstructJEIConstants.MELTING);
          }
        }
      }
    }
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registry) {
    // Do not register JEI subtypes for retextured tables, smeltery blocks, or anvils.
    // On JEI 19.27 these texture-specific subtypes interfere with recipe lookups and catalysts,
    // causing the visible variant in JEI to miss recipes whose outputs/catalysts use the plain stack.

    // TODO: potion subtype interpreters need migration to data components (getTag() removed in 1.21)
    // PotionSubtypeInterpreter needs to be updated to use data components instead of CompoundTag

    // parts
    for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
      registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item.value(), ToolPartSubtypeInterpreter.INSTANCE);
    }

    // tools
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MULTIPART_TOOL)) {
      Item item = holder.value();
      registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, holder.is(TinkerTags.Items.SINGLEPART_TOOL) ? ToolSubtypeInterpreter.ALWAYS : ToolSubtypeInterpreter.INGREDIENT);
    }

    // fluid containers have types based on fluid, don't bother with different sizes
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.copperCan.get(), (stack, context) -> CopperCanItem.getSubtype(stack));
    IIngredientSubtypeInterpreter<ItemStack> tankInterpreter = (stack, context) -> TankItem.getSubtype(stack);
    for (TankType type : TankType.values()) {
      registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedTank.get(type).asItem(), tankInterpreter);
      registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedTank.get(type).asItem(), tankInterpreter);
    }
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedLantern.asItem(), tankInterpreter);
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedLantern.asItem(), tankInterpreter);
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.searedFluidCannon.asItem(), tankInterpreter);
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerSmeltery.scorchedFluidCannon.asItem(), tankInterpreter);

    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerModifiers.creativeSlotItem.get(), (stack, context) -> {
      SlotType slotType = CreativeSlotItem.getSlot(stack);
      return slotType != null ? slotType.getName() : "";
    });
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, TinkerModifiers.modifierCrystal.get(), (stack, context) -> {
      ModifierId id = ModifierCrystalItem.getModifier(stack);
      return id == null ? "" : id.toString();
    });
  }

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGenericGuiContainerHandler(MelterScreen.class, new GuiContainerTankHandler<>());
    registration.addGenericGuiContainerHandler(AlloyerScreen.class, new GuiContainerTankHandler<>());
    registration.addGenericGuiContainerHandler(HeatingStructureScreen.class, new GuiContainerTankHandler<>());
    registration.addGenericGuiContainerHandler(ToolContainerScreen.class, new GuiContainerTankHandler<>());
  }

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new CraftingStationTransferInfo());
    IRecipeTransferHandlerHelper helper = registration.getTransferHelper();
    registration.addRecipeTransferHandler(new TinkerStationTransferInfo<>(TConstructJEIConstants.MODIFIERS, helper), TConstructJEIConstants.MODIFIERS);
    registration.addRecipeTransferHandler(new TinkerStationTransferInfo<>(TConstructJEIConstants.TOOL_BUILDING, helper), TConstructJEIConstants.TOOL_BUILDING);
    registration.addRecipeTransferHandler(new ToolInventoryTransferInfo(helper), RecipeTypes.CRAFTING);
  }

  /**
   * Removes a fluid from JEI
   * @param remove  List of ingredients to remove for batching
   * @param fluid   Fluid to remove
   */
  private static void removeFluid(List<FluidStack> remove, Fluid fluid) {
    remove.add(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
  }

  /** Checks if the given tag exists */
  @SuppressWarnings("deprecation")
  private static boolean tagExists(String name) {
    Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(ItemTags.create(commonResource(name)));
    return tag.isPresent() && tag.get().size() > 0;
  }

  /** Removes any retextured variants that shouldn't show */
  private static void cleanupRetexturedBlock(Predicate<ItemStack> remover, boolean showAll, ItemLike item, TagKey<Item> tag) {
    if (!showAll) {
      RetexturedHelper.addTagVariants(remover, item, tag);
    }
    // do not remove blank if not showing all as that removes all anvils from the catalyst display due to recipe context
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    addLateRecipes(jeiRuntime);

    IIngredientManager manager = jeiRuntime.getIngredientManager();

    List<ItemStack> removeItems = new ArrayList<>();
    Consumer<ItemStack> removeItem = removeItems::add;
    List<ItemStack> addItems = new ArrayList<>();
    Consumer<ItemStack> addItem = addItems::add;
    // shown via the modifiers
    removeItems.add(new ItemStack(TinkerModifiers.modifierCrystal));
    ModifierCrystalItem.addVariants(removeItem);
    // shown via modifier slots
    removeItems.add(new ItemStack(TinkerModifiers.creativeSlotItem));
    TinkerModifiers.creativeSlotItem.get().addVariants(removeItem);

    // fluids can be clutter so remove them by default
    if (!Config.CLIENT.showFilledFluidTanks.get()) {
      CopperCanItem.addFilledVariants(removeItem);
      TankItem.addFilledVariants(removeItem);
      // add back lava and blazing blood filled tanks, since they are useful and not much clutter
      // easier to do this than to filter the list
      addItems.add(TankItem.fillTank(TinkerSmeltery.searedTank, TankType.FUEL_TANK, Fluids.LAVA));
      addItems.add(TankItem.fillTank(TinkerSmeltery.searedTank, TankType.FUEL_TANK, TinkerFluids.blazingBlood.get()));
      addItems.add(TankItem.fillTank(TinkerSmeltery.scorchedTank, TankType.FUEL_TANK, Fluids.LAVA));
      addItems.add(TankItem.fillTank(TinkerSmeltery.scorchedTank, TankType.FUEL_TANK, TinkerFluids.blazingBlood.get()));
    }
    // tool config filters to 1 material, easiest to just remove all then add back the 1
    String showOnlyTools = Config.CLIENT.showOnlyToolMaterial.get();
    if (!showOnlyTools.isEmpty()) {
      for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MODIFIABLE)) {
        if (item.value() instanceof IModifiable modifiable) {
          ToolBuildHandler.addVariants(removeItem, modifiable, "");
          ToolBuildHandler.addVariants(addItem, modifiable, showOnlyTools);
        }
      }
    }
    String showOnlyParts = Config.CLIENT.showOnlyPartMaterial.get();
    if (!showOnlyTools.isEmpty()) {
      for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
        if (item.value() instanceof IMaterialItem part) {
          part.addVariants(removeItem, "");
          part.addVariants(addItem, showOnlyParts);
        }
      }
    }
    // For retextured tables and smeltery blocks, removing variants at runtime breaks recipe lookup in JEI 19.27.
    // Keep all variants available so recipes and catalysts stay discoverable.
    Predicate<ItemStack> cleanupItem = stack -> {
      removeItems.add(stack);
      return false;
    };
    TConstruct.LOG.info("JEI runtime available: keeping retextured table, anvil, and smeltery block variants visible for reliable recipe lookup");

    if (!removeItems.isEmpty()) {
      manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, removeItems);
    }
    if (!addItems.isEmpty()) {
      manager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, addItems);
    }

    // fluid hiding, buckets are hidden via the creative tab logic
    // hide compat that is not present
    List<FluidStack> removeFluids = new ArrayList<>();
    compatLoop:
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      // if none of the tags exist, remove the fluid
      if (!compat.isPresent()) {
        removeFluid(removeFluids, compat.getFluid().get());
      }
    }
    if (!ModList.get().isLoaded("ceramics")) {
      removeFluid(removeFluids, TinkerFluids.moltenPorcelain.get());
    }

    // add potion fluids for each potion variant if requested
    if (Config.CLIENT.showPotionFluidInJEI.get()) {
      manager.addIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK,
                                      BuiltInRegistries.POTION.holders().filter(holder -> {
                                        return !holder.is(Potions.WATER) && !holder.is(TinkerTags.Potions.HIDDEN_FLUID);
                                      }).map(holder -> PotionFluidType.potionFluid(holder.key(), FluidType.BUCKET_VOLUME)).toList());
    }
    // remove variantless potion fluid
    removeFluid(removeFluids, TinkerFluids.potion.get());

    // remove all the fluids
    manager.removeIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK, removeFluids);

    // hide easter egg recipes
    RecipeManager recipes = getClientRecipeManager();
    if (recipes != null) {
      List<RecipeHolder<CraftingRecipe>> easterEggs = Arrays.stream(EASTER_EGG_RECIPES)
        .flatMap(id -> recipes.byKey(id).stream())
        .filter(holder -> holder.value() instanceof CraftingRecipe)
        .<RecipeHolder<CraftingRecipe>>map(holder -> (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) holder)
        .toList();
      if (!easterEggs.isEmpty()) {
        jeiRuntime.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING, easterEggs);
      }
    }

    modIdHelper = jeiRuntime.getJeiHelpers().getModIdHelper();
  }
}

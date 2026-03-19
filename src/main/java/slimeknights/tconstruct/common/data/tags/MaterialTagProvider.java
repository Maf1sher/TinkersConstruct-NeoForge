package slimeknights.tconstruct.common.data.tags;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.tinkering.AbstractMaterialTagProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.Arrays;

public class MaterialTagProvider extends AbstractMaterialTagProvider {
  public MaterialTagProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
    super(packOutput, TConstruct.MOD_ID, existingFileHelper);
  }

  /** Helper to convert MaterialId varargs to ResourceLocation array */
  private static ResourceLocation[] loc(MaterialId... ids) {
    return Arrays.stream(ids).map(MaterialId::location).toArray(ResourceLocation[]::new);
  }

  @Override
  protected void addTags() {
    // fiery is obtained through specific progression in TF, better to not add a progression bypass
    tag(TinkerTags.Materials.EXCLUDE_FROM_LOOT).addOptional(MaterialIds.fiery.location());
    tag(TinkerTags.Materials.NETHER).add(loc(
      // tier 1
      MaterialIds.wood, MaterialIds.flint, MaterialIds.rock, MaterialIds.bone,
      MaterialIds.leather, MaterialIds.string,
      // tier 2
      MaterialIds.gold, MaterialIds.slimewood,
      // tier 3
      MaterialIds.nahuatl, MaterialIds.obsidian, MaterialIds.darkthread, MaterialIds.steel,
      // tier 4
      MaterialIds.ancient
    )).addTag(TinkerTags.Materials.NETHER_GATED);

    // things that *require* nether access to craft
    tag(TinkerTags.Materials.NETHER_GATED).add(loc(
      // tier 1
      MaterialIds.twistingVine, MaterialIds.weepingVine,
      // tier 2
      MaterialIds.scorchedStone, MaterialIds.necroticBone,
      // tier 3
      MaterialIds.cobalt,
      // tier 4
      MaterialIds.manyullyn, MaterialIds.hepatizon, MaterialIds.cinderslime,
      MaterialIds.queensSlime, MaterialIds.blazingBone, MaterialIds.blazewood,
      MaterialIds.ancientHide,
      // ammo
      MaterialIds.glowstone, MaterialIds.ichor, MaterialIds.quartz, MaterialIds.blaze, MaterialIds.magma
    )).addOptional(MaterialIds.necronium.location());

    // all materials to show in materials and you for ammo, kept to 9 options
    tag(TinkerTags.Materials.BASIC_AMMO).add(loc(
      // head
      MaterialIds.flint, MaterialIds.wool, MaterialIds.glass,
      // shaft
      MaterialIds.wood, MaterialIds.bamboo, MaterialIds.cactus,
      // fletching
      MaterialIds.feather, MaterialIds.paper, MaterialIds.leaves
    ));

    // tier 4 is split into several parts in different books
    tag(TinkerTags.Materials.BLAZING_BLOOD).add(loc(MaterialIds.manyullyn, MaterialIds.hepatizon, MaterialIds.queensSlime, MaterialIds.cinderslime, MaterialIds.blazingBone, MaterialIds.blazewood, MaterialIds.ancientHide))
      .addOptional(MaterialIds.nicrosil.location());
    tag(TinkerTags.Materials.DISTANT).add(loc(
      // tiers 1-2
      MaterialIds.chorus, MaterialIds.whitestone,
      // tier 4
      MaterialIds.knightmetal, MaterialIds.knightly, MaterialIds.knightslime, MaterialIds.enderslimeVine, MaterialIds.ancient,
      // ammo and maille
      MaterialIds.shulker, MaterialIds.dragonScale, MaterialIds.enderslime, MaterialIds.endRod
    )).addOptional(MaterialIds.ironwood.location(), MaterialIds.steeleaf.location(), MaterialIds.fiery.location());

    // materials bartered by piglins
    tag(TinkerTags.Materials.BARTERED).add(loc(
      // tier 3
      MaterialIds.nahuatl, MaterialIds.obsidian, MaterialIds.darkthread,
      MaterialIds.cobalt, MaterialIds.steel,
      // tier 4
      MaterialIds.manyullyn, MaterialIds.hepatizon,
      MaterialIds.cinderslime, MaterialIds.queensSlime,
      MaterialIds.blazingBone, MaterialIds.blazewood,
      MaterialIds.ancientHide, MaterialIds.ancient
    )).addOptional(MaterialIds.necronium.location());

    // tag all compat materials
    tag(TinkerTags.Materials.COMPATABILITY_METALS).addOptional(
      // tier 2
      MaterialIds.silver.location(), MaterialIds.lead.location(), MaterialIds.aluminum.location(),
      MaterialIds.osmium.location(), MaterialIds.ironwood.location(),
      // tier 3
      MaterialIds.steeleaf.location(),
      // tier 4
      MaterialIds.fiery.location()
    ).addTag(TinkerTags.Materials.COMPATABILITY_BLOCKS);
    tag(TinkerTags.Materials.COMPATABILITY_BLOCKS).addTag(TinkerTags.Materials.COMPATABILITY_ALLOYS);
    tag(TinkerTags.Materials.COMPATABILITY_ALLOYS).addOptional(MaterialIds.bronze.location(), MaterialIds.constantan.location(), MaterialIds.invar.location(), MaterialIds.electrum.location(), MaterialIds.pewter.location(), MaterialIds.nicrosil.location());

    // material categories
    // melee harvest
    tag(TinkerTags.Materials.GENERAL).add(loc(
      // tier 1
      MaterialIds.wood, MaterialIds.string, MaterialIds.vine, MaterialIds.leather,
      // tier 2
      MaterialIds.iron, MaterialIds.slimewood,
      // tier 3
      MaterialIds.slimesteel, MaterialIds.pigIron, MaterialIds.roseGold, MaterialIds.cobalt,
      // tier 4
      MaterialIds.cinderslime, MaterialIds.queensSlime, MaterialIds.enderslimeVine
    )).addOptional(
      // tier 1
      MaterialIds.treatedWood.location(),
      // tier 2
      MaterialIds.osmium.location(), MaterialIds.ironwood.location(),
      // tier 3
      MaterialIds.platedSlimewood.location(), MaterialIds.electrum.location(), MaterialIds.steeleaf.location(),
      // tier 4
      MaterialIds.fiery.location()
    );
    tag(TinkerTags.Materials.HARVEST).add(loc(
      // tier 1
      MaterialIds.rock, MaterialIds.copper,
      // tier 2
      MaterialIds.searedStone, MaterialIds.whitestone, MaterialIds.skyslimeVine, MaterialIds.twistingVine,
      // tier 3
      MaterialIds.amethystBronze,
      // tier 4
      MaterialIds.hepatizon, MaterialIds.ancientHide, MaterialIds.knightslime
    )).addOptional(
      // tier 2
      MaterialIds.lead.location(),
      // tier 3
      MaterialIds.bronze.location(), MaterialIds.constantan.location()
    );
    tag(TinkerTags.Materials.MELEE).add(loc(
      // tier 1
      MaterialIds.flint, MaterialIds.bone, MaterialIds.chorus,
      // tier 2
      MaterialIds.scorchedStone, MaterialIds.necroticBone, MaterialIds.venombone, MaterialIds.weepingVine,
      // tier 3
      MaterialIds.nahuatl, MaterialIds.steel, MaterialIds.darkthread,
      // tier 4
      MaterialIds.manyullyn, MaterialIds.blazingBone, MaterialIds.knightmetal
    )).addOptional(
      // tier 2
      MaterialIds.silver.location(),
      // tier 3
      MaterialIds.invar.location(), MaterialIds.pewter.location(), MaterialIds.necronium.location(),
      // tier 4
      MaterialIds.nicrosil.location()
    );

    // ranged
    tag(TinkerTags.Materials.BALANCED).add(loc(
      // tier 1
      MaterialIds.wood, MaterialIds.chorus,
      MaterialIds.string, MaterialIds.vine, MaterialIds.leather,
      // tier 2
      MaterialIds.slimewood, MaterialIds.necroticBone, MaterialIds.skyslimeVine,
      // tier 3
      MaterialIds.slimesteel, MaterialIds.darkthread, MaterialIds.cobalt, MaterialIds.pigIron,
      // tier 4
      MaterialIds.blazingBone, MaterialIds.ancientHide, MaterialIds.enderslimeVine
    )).addOptional(
      // tier 1
      MaterialIds.treatedWood.location(),
      // tier 2
      MaterialIds.silver.location(), MaterialIds.ironwood.location(),
      // tier 3
      MaterialIds.invar.location(), MaterialIds.pewter.location(), MaterialIds.steeleaf.location()
    );
    tag(TinkerTags.Materials.LIGHT).add(loc(
      // tier 1
      MaterialIds.bamboo, MaterialIds.bone,
      // tier 2
      MaterialIds.venombone, MaterialIds.twistingVine,
      // tier 3
      MaterialIds.nahuatl, MaterialIds.roseGold,
      // tier 4
      MaterialIds.hepatizon, MaterialIds.queensSlime, MaterialIds.knightmetal
    )).addOptional(
      // tier 2
      MaterialIds.aluminum.location(),
      // tier 3
      MaterialIds.necronium.location(), MaterialIds.constantan.location(), MaterialIds.platedSlimewood.location(),
      // tier 4
      MaterialIds.nicrosil.location()
    );
    tag(TinkerTags.Materials.HEAVY).add(loc(
      // tier 1
      MaterialIds.copper, MaterialIds.cactus,
      // tier 2
      MaterialIds.iron, MaterialIds.weepingVine,
      // tier 3
      MaterialIds.amethystBronze, MaterialIds.steel,
      // tier 4
      MaterialIds.manyullyn, MaterialIds.cinderslime, MaterialIds.knightslime
    )).addOptional(
      // tier 2
      MaterialIds.lead.location(),
      // tier 3
      MaterialIds.bronze.location(), MaterialIds.electrum.location(),
      // tier 4
      MaterialIds.fiery.location()
    );

    // slimeskull sort order
    tag(TinkerTags.Materials.SLIMESKULL).add(loc(
      // creeper
      MaterialIds.glass,
      // zombie
      MaterialIds.leather, MaterialIds.iron, MaterialIds.copper,
      // spider
      MaterialIds.string, MaterialIds.darkthread,
      // skeleton
      MaterialIds.bone, MaterialIds.ice, MaterialIds.necroticBone,
      // piglins
      MaterialIds.gold, MaterialIds.roseGold, MaterialIds.pigIron,
      // misc
      MaterialIds.blaze, MaterialIds.enderPearl, MaterialIds.dragonScale,
      // crafted
      MaterialIds.venombone, MaterialIds.blazingBone
    )).addOptional(MaterialIds.necronium.location());
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Material Tag Provider";
  }
}

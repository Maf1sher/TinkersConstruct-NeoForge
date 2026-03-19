package slimeknights.tconstruct.common.data.tags;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierTagProvider;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

import static slimeknights.tconstruct.common.TinkerTags.Modifiers.ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.AOE_INTERACTION;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.ARMOR_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.ARMOR_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BLOCK_WHILE_CHARGING;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BONUS_SLOTLESS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BOOT_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BOOT_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BYPASS_EXTRA_DURABILITY;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BYPASS_FROSTSHIELD;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BYPASS_OVERSLIME;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BYPASS_REINFORCED;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.BYPASS_TANNED;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.CHARGE_EMPTY_BOW_WITHOUT_DRAWTIME;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.CHARGE_EMPTY_BOW_WITH_DRAWTIME;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.CHESTPLATE_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.CHESTPLATE_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.COSMETIC_SLOTLESS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.DAMAGE_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.DEFENSE;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.DRILL_ATTACKS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.DUAL_INTERACTION;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.EXTRACT_SLOTLESS_BLACKLIST;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.EXTRACT_UPGRADE_BLACKLIST;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GEMS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GENERAL_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GENERAL_ARMOR_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GENERAL_ARMOR_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GENERAL_SLOTLESS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.GENERAL_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.HARVEST_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.HARVEST_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.HELMET_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.HELMET_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.INTERACTION_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.INVISIBLE_INK_BLACKLIST;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.KNOCKBACK_SLINGS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.LEGGING_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.LEGGING_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.MELEE_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.MELEE_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.OVERSLIME_FRIEND;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.PROTECTION_DEFENSE;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.RANGED_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.RANGED_UPGRADES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.REMOVE_MODIFIER_BLACKLIST;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SECONDARY_DURABILITY;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SELF_KNOCKBACK_SLINGS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SHIELD_ABILITIES;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SLIME_DEFENSE;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SLOTLESS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.SPECIAL_DEFENSE;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.TARGET_KNOCKBACK_SLINGS;
import static slimeknights.tconstruct.common.TinkerTags.Modifiers.UPGRADES;

public class ModifierTagProvider extends AbstractModifierTagProvider {
  public ModifierTagProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
    super(packOutput, TConstruct.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags() {
    tag(GEMS).add(ModifierIds.diamond.location(), ModifierIds.emerald.location());
    tag(INVISIBLE_INK_BLACKLIST).add(
      TinkerModifiers.embellishment.getId(), TinkerModifiers.dyed.getId(), TinkerModifiers.trim.getId(),
      TinkerModifiers.creativeSlot.getId(), TinkerModifiers.statOverride.getId(),
      ModifierIds.shiny.location(), TinkerModifiers.golden.getId()
    );
    tag(REMOVE_MODIFIER_BLACKLIST).add(TinkerModifiers.creativeSlot.getId(), TinkerModifiers.statOverride.getId());
    tag(EXTRACT_MODIFIER_BLACKLIST).add(
      TinkerModifiers.embellishment.getId(), TinkerModifiers.dyed.getId(), TinkerModifiers.trim.getId(),
      ModifierIds.rebalanced.location(), TinkerModifiers.overslime.getId()
    ).addTag(REMOVE_MODIFIER_BLACKLIST);
    // blacklist modifiers that are not really slotless, they just have a slotless recipe
    tag(EXTRACT_SLOTLESS_BLACKLIST).add(ModifierIds.luck.location(), ModifierIds.toolBelt.location());
    tag(EXTRACT_UPGRADE_BLACKLIST);

    // modifiers in this tag support both left click and right click interaction
    tag(DUAL_INTERACTION).add(
      ModifierIds.bucketing.location(), ModifierIds.splashing.location(),
      ModifierIds.glowing.location(), ModifierIds.firestarter.location(),
      ModifierIds.stripping.location(), ModifierIds.tilling.location(), ModifierIds.pathing.location(),
      ModifierIds.shears.location(), ModifierIds.silkyShears.location(),
      ModifierIds.harvest.location(), ModifierIds.fishing.location(),
      ModifierIds.pockets.location()
    );
    tag(BLOCK_WHILE_CHARGING).add(
      ModifierIds.flinging.location(), ModifierIds.springing.location(), ModifierIds.bonking.location(), ModifierIds.warping.location(),
      ModifierIds.spitting.location(), ModifierIds.scope.location(), ModifierIds.zoom.location(), ModifierIds.brushing.location(), ModifierIds.throwing.location()
    );
    tag(SLIME_DEFENSE).add(
      ModifierIds.meleeProtection.location(), ModifierIds.projectileProtection.location(),
      ModifierIds.fireProtection.location(), ModifierIds.magicProtection.location(),
      ModifierIds.blastProtection.location()
    );
    tag(OVERSLIME_FRIEND).add(
      ModifierIds.overgrowth.location(), ModifierIds.overcast.location(), ModifierIds.overburn.location(), ModifierIds.overlord.location(), ModifierIds.overshield.location(), ModifierIds.overwield.location(),
      ModifierIds.overforced.location(), ModifierIds.overslimeFriend.location(), TinkerModifiers.overworked.getId()
    );
    tag(AOE_INTERACTION).add(ModifierIds.pathing.location(), ModifierIds.stripping.location(), ModifierIds.tilling.location(), ModifierIds.brushing.location(), ModifierIds.splashing.location(), ModifierIds.harvest.location());
    tag(CHARGE_EMPTY_BOW_WITH_DRAWTIME).add(ModifierIds.flinging.location(), ModifierIds.springing.location(), ModifierIds.bonking.location(), ModifierIds.warping.location(), ModifierIds.throwing.location());
    tag(CHARGE_EMPTY_BOW_WITHOUT_DRAWTIME).add(ModifierIds.blocking.location(), ModifierIds.scope.location(), ModifierIds.zoom.location(), ModifierIds.slurping.location(), ModifierIds.tasty.location());
    tag(DRILL_ATTACKS).add(ModifierIds.flinging.location(), ModifierIds.springing.location(), ModifierIds.grapple.location());
    tag(SELF_KNOCKBACK_SLINGS).add(ModifierIds.flinging.location(), ModifierIds.springing.location());
    tag(TARGET_KNOCKBACK_SLINGS).add(ModifierIds.bonking.location());
    tag(KNOCKBACK_SLINGS).addTag(SELF_KNOCKBACK_SLINGS, TARGET_KNOCKBACK_SLINGS);

    // durability tags
    tag(BYPASS_TANNED).addTag(SECONDARY_DURABILITY);
    tag(SECONDARY_DURABILITY).add(
      // protection is used for the damage correction on armor, which tanned should prevent
      ModifierIds.protection.location(),
      // counter-attack
      ModifierIds.thorns.location(), ModifierIds.fiery.location(), ModifierIds.freezing.location(), ModifierIds.springy.location(),
      ModifierIds.pierce.location(), ModifierIds.venom.location(), ModifierIds.conductive.location(), ModifierIds.shock.location(),
      // special effects
      ModifierIds.necrotic.location(), ModifierIds.restore.location(), TinkerModifiers.enderporting.getId()
    );
    tag(BYPASS_REINFORCED).add(ModifierIds.glowing.location());
    tag(BYPASS_EXTRA_DURABILITY);
    tag(BYPASS_OVERSLIME).addTag(BYPASS_EXTRA_DURABILITY).add(ModifierIds.glowing.location());
    tag(BYPASS_FROSTSHIELD).addTag(BYPASS_EXTRA_DURABILITY).add(ModifierIds.glowing.location());

    // book tags
    this.tag(UPGRADES).addTag(GENERAL_UPGRADES, MELEE_UPGRADES, DAMAGE_UPGRADES, HARVEST_UPGRADES, ARMOR_UPGRADES, RANGED_UPGRADES);
    this.tag(ARMOR_UPGRADES).addTag(GENERAL_ARMOR_UPGRADES, HELMET_UPGRADES, CHESTPLATE_UPGRADES, LEGGING_UPGRADES, BOOT_UPGRADES);
    this.tag(ABILITIES).addTag(GENERAL_ABILITIES, INTERACTION_ABILITIES, MELEE_ABILITIES, HARVEST_ABILITIES, ARMOR_ABILITIES, RANGED_ABILITIES);
    this.tag(ARMOR_ABILITIES).addTag(GENERAL_ARMOR_ABILITIES, HELMET_ABILITIES, CHESTPLATE_ABILITIES, LEGGING_ABILITIES, BOOT_ABILITIES, SHIELD_ABILITIES);
    this.tag(DEFENSE).addTag(PROTECTION_DEFENSE, SPECIAL_DEFENSE);
    this.tag(SLOTLESS).addTag(GENERAL_SLOTLESS, BONUS_SLOTLESS, COSMETIC_SLOTLESS);

    // upgrades
    this.tag(GENERAL_UPGRADES).add(
      ModifierIds.diamond.location(), ModifierIds.emerald.location(), ModifierIds.netherite.location(),
      ModifierIds.reinforced.location(), ModifierIds.overforced.location(), ModifierIds.soulbound.location(),
      ModifierIds.experienced.location(), ModifierIds.magnetic.location(), ModifierIds.scope.location(), ModifierIds.zoom.location(),
      ModifierIds.tank.location(), ModifierIds.smelting.location(), ModifierIds.fireprimer.location())
        .addOptional(ModifierIds.theOneProbe.location());

    this.tag(MELEE_UPGRADES).add(
      ModifierIds.knockback.location(), ModifierIds.padded.location(),
      TinkerModifiers.severing.getId(), ModifierIds.necrotic.location(), ModifierIds.sweeping.location(),
      ModifierIds.fiery.location(), ModifierIds.freezing.location());
    this.tag(DAMAGE_UPGRADES).add(
      ModifierIds.sharpness.location(), ModifierIds.pierce.location(), ModifierIds.swiftstrike.location(),
      ModifierIds.antiaquatic.location(), ModifierIds.baneOfSssss.location(), ModifierIds.cooling.location(), ModifierIds.killager.location(), ModifierIds.smite.location());

    this.tag(HARVEST_UPGRADES).add(ModifierIds.haste.location(), ModifierIds.blasting.location(), ModifierIds.hydraulic.location(), ModifierIds.lightspeed.location());

    this.tag(GENERAL_ARMOR_UPGRADES).add(
      ModifierIds.fiery.location(), ModifierIds.freezing.location(), ModifierIds.thorns.location(),
      ModifierIds.ricochet.location(), ModifierIds.springy.location(), ModifierIds.blockade.location());
    this.tag(HELMET_UPGRADES).add(TinkerModifiers.itemFrame.getId(), ModifierIds.respiration.location(), ModifierIds.minimap.location()).addOptional(ModifierIds.headlight.location());
    this.tag(CHESTPLATE_UPGRADES).add(ModifierIds.haste.location(), ModifierIds.knockback.location(), TinkerModifiers.sleeves.getId());
    this.tag(LEGGING_UPGRADES).add(ModifierIds.leaping.location(), TinkerModifiers.shieldStrap.getId(), ModifierIds.speedy.location(), ModifierIds.swiftSneak.location(), ModifierIds.stepUp.location());
    this.tag(BOOT_UPGRADES).add(ModifierIds.depthStrider.location(), ModifierIds.featherFalling.location(), ModifierIds.longFall.location(), ModifierIds.lightspeed.location(), ModifierIds.soulspeed.location());

    this.tag(RANGED_UPGRADES).add(
      ModifierIds.pierce.location(), ModifierIds.power.location(), ModifierIds.punch.location(), ModifierIds.quickCharge.location(),
      TinkerModifiers.sinistral.getId(), ModifierIds.trueshot.location(),
      ModifierIds.fiery.location(), ModifierIds.freezing.location(),
      ModifierIds.arrowPierce.location(), ModifierIds.bounce.location(), ModifierIds.necrotic.location(),
      ModifierIds.lure.location(), ModifierIds.collecting.location(), ModifierIds.fins.location());

    // abilities
    this.tag(GENERAL_ABILITIES).add(
      ModifierIds.expanded.location(), ModifierIds.gilded.location(), ModifierIds.unbreakable.location(),
      ModifierIds.luck.location(), TinkerModifiers.melting.getId());
    this.tag(MELEE_ABILITIES).add(
      ModifierIds.blocking.location(), TinkerModifiers.parrying.getId(),
      TinkerModifiers.dualWielding.getId(), ModifierIds.spilling.location());
    this.tag(HARVEST_ABILITIES).add(ModifierIds.autosmelt.location(), TinkerModifiers.exchanging.getId(), ModifierIds.silky.location());
    this.tag(RANGED_ABILITIES).add(
      ModifierIds.bulkQuiver.location(), ModifierIds.trickQuiver.location(),
      ModifierIds.crystalshot.location(), ModifierIds.multishot.location(), ModifierIds.ballista.location(),
      ModifierIds.grapple.location(),
      ModifierIds.channeling.location(), ModifierIds.returning.location());
    this.tag(INTERACTION_ABILITIES).add(
      ModifierIds.bucketing.location(), ModifierIds.firestarter.location(), ModifierIds.glowing.location(),
      ModifierIds.pathing.location(), ModifierIds.stripping.location(), ModifierIds.tilling.location(), ModifierIds.brushing.location(),
      ModifierIds.spitting.location(), ModifierIds.splashing.location(), ModifierIds.slurping.location(),
      ModifierIds.bonking.location(), ModifierIds.flinging.location(), ModifierIds.springing.location(), ModifierIds.warping.location(),
      ModifierIds.throwing.location(), ModifierIds.drillAttack.location());
    // armor
    this.tag(GENERAL_ARMOR_ABILITIES).add(ModifierIds.protection.location(), TinkerModifiers.bursting.getId(), TinkerModifiers.wetting.getId());
    this.tag(HELMET_ABILITIES).add(ModifierIds.aquaAffinity.location(), ModifierIds.slurping.location());
    this.tag(CHESTPLATE_ABILITIES).add(TinkerModifiers.ambidextrous.getId(), ModifierIds.reach.location(), ModifierIds.strength.location(), ModifierIds.wings.location());
    this.tag(LEGGING_ABILITIES).add(ModifierIds.pockets.location(), ModifierIds.soulBelt.location(), ModifierIds.toolBelt.location(), ModifierIds.craftingTable.location());
    this.tag(BOOT_ABILITIES).add(
      ModifierIds.bouncy.location(), ModifierIds.doubleJump.location(),
      ModifierIds.flamewake.location(), ModifierIds.snowdrift.location(), ModifierIds.tilling.location(), ModifierIds.pathing.location(), ModifierIds.frostWalker.location(), ModifierIds.glowing.location());
    this.tag(SHIELD_ABILITIES).add(ModifierIds.boundless.location(), ModifierIds.reflecting.location());

    // defense
    this.tag(PROTECTION_DEFENSE).add(
      ModifierIds.blastProtection.location(), ModifierIds.fireProtection.location(), ModifierIds.magicProtection.location(),
      ModifierIds.meleeProtection.location(), ModifierIds.projectileProtection.location(),
      ModifierIds.dragonborn.location(), ModifierIds.shulking.location(), ModifierIds.turtleShell.location());
    this.tag(SPECIAL_DEFENSE).add(ModifierIds.knockbackResistance.location(), ModifierIds.revitalizing.location());

    // slotless
    this.tag(GENERAL_SLOTLESS).add(
      TinkerModifiers.overslime.getId(), ModifierIds.worldbound.location(),
      ModifierIds.offhanded.location(), ModifierIds.blunted.location(), ModifierIds.workbench.location(),
      ModifierIds.blindshot.location(), ModifierIds.barebow.location());
    this.tag(BONUS_SLOTLESS).add(
      ModifierIds.draconic.location(), ModifierIds.rebalanced.location(), ModifierIds.redirected.location(),
      ModifierIds.harmonious.location(), ModifierIds.recapitated.location(), ModifierIds.forecast.location(), ModifierIds.writable.location())
      .addOptional(ModifierIds.embossed.location());
    this.tag(COSMETIC_SLOTLESS).add(
      ModifierIds.shiny.location(),
      TinkerModifiers.dyed.getId(), TinkerModifiers.embellishment.getId(), TinkerModifiers.trim.getId(),
      ModifierIds.farsighted.location(), ModifierIds.nearsighted.location());
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Modifier Tag Provider";
  }
}

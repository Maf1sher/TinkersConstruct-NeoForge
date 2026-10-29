package slimeknights.tconstruct.library.tools.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.behavior.InfinityModule;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that display stacks do not modify the tool data they were copied from.
 * In 1.21 {@code ItemStack#copy()} keeps the source {@code CustomData} instance, so editing a copy in place would
 * otherwise corrupt cached render tools and recipe inputs.
 */
public class ToolDisplayStackTest extends ToolItemTest {
  @Test
  void copyRenderToolHasIndependentData() {
    ItemStack render = tool.getRenderTool();
    ToolStack renderTool = ToolStack.from(render);
    int durability = renderTool.getStats().getInt(ToolStats.DURABILITY);
    int damage = renderTool.getDamage();

    // mimic the repair display recipe: damaged tool with a fixed durability stat
    ItemStack display = ToolBuildHandler.copyRenderTool(tool);
    CompoundTag tag = DataComponentHelper.getOrCreateCustomData(display);
    tag.put(ToolStack.TAG_STATS, StatsNBT.builder().set(ToolStats.DURABILITY, 250).build().serializeToNBT());
    tag.putInt(ToolStack.TAG_DAMAGE, 200);

    ToolStack renderAfter = ToolStack.from(render);
    assertThat(renderAfter.getStats().getInt(ToolStats.DURABILITY)).isEqualTo(durability);
    assertThat(renderAfter.getDamage()).isEqualTo(damage);
    assertThat(renderAfter.isBroken()).isFalse();
  }

  @Test
  void copyCustomDataIsolatesStack() {
    ItemStack copy = testItemStack.copy();
    DataComponentHelper.copyCustomData(copy);
    ToolStack.from(copy).setDamage(5);

    assertThat(ToolStack.from(testItemStack).getDamage()).isZero();
    assertThat(ToolStack.from(copy).getDamage()).isEqualTo(5);
  }

  @Test
  void displayModifiersDoNotModifyTool() {
    int damage = ToolStack.from(testItemStack).getDamage();

    ItemStack display = IDisplayModifierRecipe.withModifiers(testItemStack, 1, List.of());
    ToolStack.from(display).setDamage(5);

    assertThat(ToolStack.from(testItemStack).getDamage()).isEqualTo(damage);
    assertThat(ToolStack.from(display).getDamage()).isEqualTo(5);
  }

  @Test
  void infinityAmmoDoesNotModifyTemplate() {
    ItemStack template = new ItemStack(Items.ARROW);
    DataComponentHelper.getOrCreateCustomData(template).putBoolean("template", true);
    InfinityModule module = new InfinityModule(template, "", 1, true);

    ItemStack ammo = module.findAmmo(ToolStack.from(testItemStack), ModifierEntry.EMPTY, null, ItemStack.EMPTY, stack -> true);

    assertThat(ammo.isEmpty()).isFalse();
    CompoundTag templateTag = DataComponentHelper.getCustomData(template);
    assertThat(templateTag).isNotNull();
    assertThat(templateTag.contains("tic_infinity")).isFalse();
    CompoundTag ammoTag = DataComponentHelper.getCustomData(ammo);
    assertThat(ammoTag).isNotNull();
    assertThat(ammoTag.getBoolean("tic_infinity")).isTrue();
  }

  @Test
  void creativeVariantsAreUndamaged() {
    List<ItemStack> stacks = new ArrayList<>();
    ToolBuildHandler.addVariants(stacks::add, tool, "");
    assertThat(stacks).isNotEmpty();
    for (ItemStack stack : stacks) {
      ToolStack variant = ToolStack.from(stack);
      assertThat(variant.getDamage()).isZero();
      assertThat(variant.getCurrentDurability()).isEqualTo(variant.getStats().getInt(ToolStats.DURABILITY));
      assertThat(stack.getBarWidth()).isEqualTo(13);
    }
  }
}

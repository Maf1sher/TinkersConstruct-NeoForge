package slimeknights.tconstruct.library.modifiers.hook.behavior;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import slimeknights.tconstruct.common.TagFixture;
import slimeknights.tconstruct.fixture.MaterialFixture;
import slimeknights.tconstruct.fixture.MaterialItemFixture;
import slimeknights.tconstruct.fixture.ToolDefinitionFixture;
import slimeknights.tconstruct.library.materials.MaterialRegistryExtension;
import slimeknights.tconstruct.library.modifiers.ModifierFixture;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Regression tests for reading enchantments on modifiable tools */
@ExtendWith(MaterialRegistryExtension.class)
public class EnchantmentModifierHookTest extends BaseMcTest {
  private static ModifiableItem tool;
  private static Holder<Enchantment> enchantment;
  private ItemStack testItemStack;

  @BeforeAll
  static void beforeAll() {
    MaterialItemFixture.init();
    ModifierFixture.init();
    TagFixture.init();
    if (tool == null) {
      tool = new ModifiableItem(new Item.Properties().stacksTo(1), ToolDefinitionFixture.getStandardToolDefinition());
      Registry.register(BuiltInRegistries.ITEM, TestHelper.id("enchantment_tool"), tool);
    }
    // direct holder is enough, the hook only compares and stores holders
    enchantment = Holder.direct(new Enchantment(
      Component.literal("test"),
      Enchantment.definition(HolderSet.direct(Items.BOW.builtInRegistryHolder()), 1, 1,
        Enchantment.constantCost(1), Enchantment.constantCost(1), 1, EquipmentSlotGroup.ANY),
      HolderSet.empty(), DataComponentMap.EMPTY));
  }

  @BeforeEach
  void setUp() {
    testItemStack = ToolBuildHandler.buildItemFromMaterials(tool, MaterialNBT.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA));
  }

  /**
   * NeoForge's {@code EnchantmentHelper#getItemEnchantmentLevel} forwards to {@code ItemStack#getEnchantmentLevel}, which
   * dispatches back into {@code Item#getEnchantmentLevel}. Reading the base level through that helper from inside the
   * Tinkers override recursed until the stack overflowed; the stored level must now be read without the dispatch.
   */
  @Test
  void readsStoredEnchantmentLevel() {
    ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
    mutable.set(enchantment, 3);
    testItemStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());

    assertEquals(3, testItemStack.getEnchantmentLevel(enchantment));
  }

  /** A missing enchantment must return zero instead of recursing */
  @Test
  void missingEnchantmentReturnsZero() {
    assertEquals(0, testItemStack.getEnchantmentLevel(enchantment));
  }
}

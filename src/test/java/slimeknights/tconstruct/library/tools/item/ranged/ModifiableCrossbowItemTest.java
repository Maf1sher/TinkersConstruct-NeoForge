package slimeknights.tconstruct.library.tools.item.ranged;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
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
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Regression tests for loading a modifiable crossbow */
@ExtendWith(MaterialRegistryExtension.class)
public class ModifiableCrossbowItemTest extends BaseMcTest {
  private static final int DRAW_TIME = 20;
  private static ModifiableCrossbowItem crossbow;

  private ItemStack bow;
  private LivingEntity living;
  private Level level;

  @BeforeAll
  static void beforeAll() {
    MaterialItemFixture.init();
    ModifierFixture.init();
    TagFixture.init();
    if (crossbow == null) {
      crossbow = new ModifiableCrossbowItem(new Item.Properties().stacksTo(1), ToolDefinitionFixture.getStandardToolDefinition(), ProjectileWeaponItem.ARROW_ONLY);
      Registry.register(BuiltInRegistries.ITEM, TestHelper.id("crossbow_test"), crossbow);
    }
  }

  @BeforeEach
  void setUp() {
    bow = ToolBuildHandler.buildItemFromMaterials(crossbow, MaterialNBT.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA));
    ToolStack.from(bow).getPersistentData().putInt(GeneralInteractionModifierHook.KEY_DRAWTIME, DRAW_TIME);

    living = mock(LivingEntity.class);
    level = mock(Level.class);
    when(living.level()).thenReturn(level);
    when(living.getProjectile(any())).thenReturn(new ItemStack(Items.ARROW));
    when(level.getRandom()).thenReturn(RandomSource.create());
    when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
  }

  /** Fully drawn release stores real ammo */
  @Test
  void releaseStoresAmmo() {
    crossbow.releaseUsing(bow, level, living, bow.getUseDuration(living) - DRAW_TIME);

    CompoundTag stored = ToolStack.from(bow).getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO);
    assertFalse(stored.isEmpty(), "stored ammo must contain the saved item");
    assertEquals(Items.ARROW, ItemStack.parseOptional(RegistryAccess.EMPTY, stored).getItem());
  }

  /** An empty compound left by older saves must not block loading forever */
  @Test
  void emptyAmmoTagDoesNotBlockLoading() {
    ToolStack.from(bow).getPersistentData().put(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO, new CompoundTag());

    crossbow.releaseUsing(bow, level, living, bow.getUseDuration(living) - DRAW_TIME);

    CompoundTag stored = ToolStack.from(bow).getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO);
    assertFalse(stored.isEmpty(), "empty legacy ammo tag must be replaced");
  }

  /** Releasing before the draw time finishes must not load */
  @Test
  void releaseBeforeDrawTimeDoesNotLoad() {
    crossbow.releaseUsing(bow, level, living, bow.getUseDuration(living) - DRAW_TIME + 1);

    assertTrue(ToolStack.from(bow).getPersistentData().getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO).isEmpty());
  }
}

package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.List;
import java.util.Map;

/**
 * Standalone armor material that no longer implements {@link ArmorMaterial}, as that became a record in 1.21.
 * Call {@link #toVanilla()} when constructing items that require a vanilla armor material.
 */
@RequiredArgsConstructor
@Getter
public class DummyArmorMaterial implements IdAwareObject {
  private final ResourceLocation id;
  private final SoundEvent equipSound;

  @Override
  public ResourceLocation id() {
    return id;
  }

  /** {@return a vanilla armor material with zero stats, as we bypass all the stat usages} */
  public ArmorMaterial toVanilla() {
    return new ArmorMaterial(Map.of(), 0, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(equipSound), () -> Ingredient.EMPTY,
                             List.of(new ArmorMaterial.Layer(id)), 0, 0);
  }
}

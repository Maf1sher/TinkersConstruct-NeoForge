package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Armor material that returns 0 except for name, since we bypass all the usages.
 * In 1.21, ArmorMaterial is a record so this class creates and registers a dummy ArmorMaterial instance.
 */
public class DummyArmorMaterial implements IdAwareObject {
  @Getter
  private final ResourceLocation id;
  private final Holder<ArmorMaterial> holder;

  public DummyArmorMaterial(ResourceLocation id, SoundEvent equipSound) {
    this(id, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(equipSound));
  }

  public DummyArmorMaterial(ResourceLocation id, Holder<SoundEvent> equipSound) {
    this.id = id;
    // Create empty defense map with all 0 values
    Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
    for (ArmorItem.Type type : ArmorItem.Type.values()) {
      defense.put(type, 0);
    }
    // Register the armor material to get a Holder
    this.holder = Registry.registerForHolder(
      BuiltInRegistries.ARMOR_MATERIAL,
      id,
      new ArmorMaterial(defense, 0, equipSound, () -> Ingredient.EMPTY,
        List.of(new ArmorMaterial.Layer(id)), 0.0F, 0.0F)
    );
  }

  /** Gets the holder for this armor material, for use with ArmorItem constructor */
  public Holder<ArmorMaterial> getHolder() {
    return holder;
  }

  /** Gets the equip sound from the underlying material */
  public Holder<SoundEvent> getEquipSound() {
    return holder.value().equipSound();
  }

  /** Gets the name of this material */
  public String getName() {
    return id.toString();
  }
}

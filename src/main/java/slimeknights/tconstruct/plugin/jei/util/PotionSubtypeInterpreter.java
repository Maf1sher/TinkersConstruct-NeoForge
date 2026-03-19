package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/** Common logic for subtype interpreter between the fluid and item form of our potion. Based on a JEI class with the same name */
public interface PotionSubtypeInterpreter<T> extends IIngredientSubtypeInterpreter<T> {
  /** Gets the potion contents from the ingredient using data components */
  @Nullable
  PotionContents getPotionContents(T ingredient);

  @Override
  default String apply(T ingredient, UidContext context) {
    PotionContents contents = getPotionContents(ingredient);
    if (contents == null) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    Optional<Holder<Potion>> potionHolder = contents.potion();
    if (potionHolder.isEmpty()) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    Potion potionType = potionHolder.get().value();
    // Use registry key as unique identifier since Potion.getName(String) instance method was removed in 1.21
    String potionTypeString = BuiltInRegistries.POTION.getKey(potionType).toString();
    StringBuilder stringBuilder = new StringBuilder(potionTypeString);
    List<MobEffectInstance> effects = potionType.getEffects();
    for (MobEffectInstance effect : effects) {
      stringBuilder.append(";").append(effect);
    }
    return stringBuilder.toString();
  }
}

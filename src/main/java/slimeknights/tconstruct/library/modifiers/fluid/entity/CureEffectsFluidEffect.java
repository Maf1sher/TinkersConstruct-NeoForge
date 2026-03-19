package slimeknights.tconstruct.library.modifiers.fluid.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Effect to clear all effects using the given stack.
 * In 1.21 NeoForge, per-instance curative items were replaced by the EffectCure system.
 * This now cures effects that are curable by MILK (the default cure for milk bucket).
 * @param stack  Stack used for curing, retained for data loading compatibility
 */
public record CureEffectsFluidEffect(ItemStack stack) implements FluidEffect<FluidEffectContext.Entity> {
  public static final RecordLoadable<CureEffectsFluidEffect> LOADER = RecordLoadable.create(ItemStackLoadable.REQUIRED_ITEM.requiredField("item", e -> e.stack), CureEffectsFluidEffect::new);

  public CureEffectsFluidEffect(ItemLike item) {
    this(new ItemStack(item));
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, Entity context, FluidAction action) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && level.isFull()) {
      // when simulating, search the effects list directly for curable effects
      if (action.simulate()) {
        return target.getActiveEffects().stream().anyMatch(effect -> effect.getCures().contains(EffectCures.MILK)) ? 1 : 0;
      }
      // remove all effects that are curable by milk
      List<Holder<MobEffect>> toRemove = new ArrayList<>();
      for (MobEffectInstance effect : target.getActiveEffects()) {
        if (effect.getCures().contains(EffectCures.MILK)) {
          toRemove.add(effect.getEffect());
        }
      }
      if (toRemove.isEmpty()) {
        return 0;
      }
      for (Holder<MobEffect> effect : toRemove) {
        target.removeEffect(effect);
      }
      return 1;
    }
    return 0;
  }

  @Override
  public RecordLoadable<CureEffectsFluidEffect> getLoader() {
    return LOADER;
  }
}

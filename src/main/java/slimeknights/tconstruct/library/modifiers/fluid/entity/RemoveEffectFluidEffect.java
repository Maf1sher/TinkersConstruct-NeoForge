package slimeknights.tconstruct.library.modifiers.fluid.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext.Entity;

/** Spilling effect to remove a specific effect */
public record RemoveEffectFluidEffect(MobEffect effect) implements FluidEffect<FluidEffectContext.Entity> {
  public static final RecordLoadable<RemoveEffectFluidEffect> LOADER = RecordLoadable.create(Loadables.MOB_EFFECT.requiredField("effect", e -> e.effect), RemoveEffectFluidEffect::new);

  /** Constructor accepting a Holder for convenience with 1.21 MobEffects constants */
  public RemoveEffectFluidEffect(Holder<MobEffect> effectHolder) {
    this(effectHolder.value());
  }

  @Override
  public RecordLoadable<RemoveEffectFluidEffect> getLoader() {
    return LOADER;
  }

  /** Gets the effect wrapped as a holder for use with 1.21 APIs */
  private Holder<MobEffect> effectHolder() {
    return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, Entity context, FluidAction action) {
    LivingEntity living = context.getLivingTarget();
    if (living != null && level.isFull()) {
      if (action.simulate()) {
        return living.hasEffect(effectHolder()) ? 1 : 0;
      }
      return living.removeEffect(effectHolder()) ? 1 : 0;
    }
    return 0;
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    return FluidEffect.makeTranslation(getLoader(), effect.getDisplayName());
  }
}

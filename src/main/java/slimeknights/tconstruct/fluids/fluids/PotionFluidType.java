package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.fluids.TinkerFluids;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  /** Gets the PotionContents from a FluidStack's data components */
  @Nullable
  public static PotionContents getPotionContents(FluidStack stack) {
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, null);
  }

  /** Gets a Holder<Potion> from a FluidStack */
  public static Optional<Holder<Potion>> getPotionHolder(FluidStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, null);
    if (contents != null) {
      return contents.potion();
    }
    return Optional.empty();
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    PotionContents contents = getPotionContents(stack);
    if (contents != null && contents.potion().isPresent()) {
      return Potion.getName(contents.potion(), "item.minecraft.potion.effect.");
    }
    return "item.minecraft.potion.effect.empty";
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    itemStack.applyComponents(fluidStack.getComponentsPatch());
    return itemStack;
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      /**
       * Gets the color, based on {@link PotionContents#getColor()}
       * @param stack  Fluid stack instance
       * @return  Color for the fluid
       */
      @Override
      public int getTintColor(FluidStack stack) {
        PotionContents contents = getPotionContents(stack);
        if (contents != null) {
          int color = contents.getColor();
          return color | 0xFF000000;
        }
        Optional<Holder<Potion>> potionHolder = getPotionHolder(stack);
        if (potionHolder.isPresent()) {
          List<MobEffectInstance> effects = potionHolder.get().value().getEffects();
          if (!effects.isEmpty()) {
            return PotionContents.getColor(effects) | 0xFF000000;
          }
        }
        return getTintColor();
      }
    });
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    if (!Potions.WATER.is(potion)) {
      Optional<Holder.Reference<Potion>> holder = BuiltInRegistries.POTION.getHolder(potion);
      holder.ifPresent(potionHolder -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder)));
    }
    return stack;
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidStack potionFluid(Holder<Potion> potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    if (potion != Potions.WATER) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    }
    return stack;
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidOutput potionResult(Holder<Potion> potion, int size) {
    // FluidOutput needs a tag-based approach; use a direct fluid stack
    return FluidOutput.fromStack(potionFluid(potion, size));
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!Potions.WATER.is(potion)) {
      Optional<Holder.Reference<Potion>> holder = BuiltInRegistries.POTION.getHolder(potion);
      holder.ifPresent(potionHolder -> stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder)));
    }
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static ItemStack potionBucket(Holder<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (potion != Potions.WATER) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    }
    return stack;
  }
}

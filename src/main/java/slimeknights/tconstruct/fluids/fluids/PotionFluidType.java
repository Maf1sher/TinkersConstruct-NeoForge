package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    return Potion.getName(getPotion(stack), "item.minecraft.potion.effect.");
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    return DataComponentHelper.copy(itemStack, fluidStack, DataComponents.POTION_CONTENTS);
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      /**
         * Gets the color from the potion stored on the fluid.
       * @param stack  Fluid stack instance
       * @return  Color for the fluid
       */
      @Override
      public int getTintColor(FluidStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.potion().isPresent() || contents.customColor().isPresent() || !contents.customEffects().isEmpty()) {
          return contents.getColor() | 0xFF000000;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", Tag.TAG_ANY_NUMERIC)) {
          return tag.getInt("CustomPotionColor") | 0xFF000000;
        }
        return getPotion(stack).map(PotionContents::getColor).map(color -> color | 0xFF000000).orElseGet(this::getTintColor);
      }
    });
  }

  /** Resolves the legacy fluid NBT potion ID against the current registry. */
  public static Optional<Holder<Potion>> getPotion(@Nullable CompoundTag tag) {
    if (tag == null || !tag.contains("Potion", Tag.TAG_STRING)) {
      return Optional.empty();
    }
    ResourceLocation location = ResourceLocation.tryParse(tag.getString("Potion"));
    if (location == null) {
      return Optional.empty();
    }
    return BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, location)).map(holder -> (Holder<Potion>)holder);
  }

  /** Reads modern potion components first and accepts the old NBT field for saved fluid stacks. */
  private static Optional<Holder<Potion>> getPotion(FluidStack stack) {
    Optional<Holder<Potion>> component = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
    return component.isPresent() ? component : getPotion(stack.getTag());
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.getHolderOrThrow(potion)));
    return stack;
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidStack potionFluid(Potion potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    if (!potion.getEffects().isEmpty()) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    }
    return stack;
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidOutput potionResult(Potion potion, int size) {
    DataComponentPatch components = potion.getEffects().isEmpty()
      ? DataComponentPatch.EMPTY
      : DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion))).build();
    return FluidOutput.fromTag(Objects.requireNonNull(TinkerFluids.potion.getCommonTag()), size, components);
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.getHolderOrThrow(potion)));
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static ItemStack potionBucket(Potion potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (!potion.getEffects().isEmpty()) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    }
    return stack;
  }
}

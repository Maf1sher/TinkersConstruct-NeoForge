package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Custom ingredient matching an {@link net.minecraft.world.item.InstrumentItem} with a particular instrument.
 * Since 1.20.5, the instrument on the item stack is stored in the {@link DataComponents#INSTRUMENT} component.
 */
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"item", "instrument", "ignore"})
public class InstrumentIngredient implements ICustomIngredient {
  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("instrument");

  /** Codec keeping the legacy JSON layout: {@code {"type": "tconstruct:instrument", "item": ..., "instrument"|"ignore": ...}} */
  public static final MapCodec<InstrumentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ingredient -> ingredient.item),
    // instrument is a registry key, ignore is a tag; exactly one must be set
    ResourceLocation.CODEC.xmap(loc -> ResourceKey.create(Registries.INSTRUMENT, loc), ResourceKey::location)
      .optionalFieldOf("instrument").forGetter(ingredient -> Optional.ofNullable(ingredient.instrument)),
    TagKey.codec(Registries.INSTRUMENT)
      .optionalFieldOf("ignore").forGetter(ingredient -> Optional.ofNullable(ingredient.ignore))
  ).apply(instance, (item, instrument, ignore) -> new InstrumentIngredient(item, instrument.orElse(null), ignore.orElse(null)))
    .validate(ingredient -> ingredient.instrument == null && ingredient.ignore == null
      ? DataResult.error(() -> "Invalid InstrumentIngredient: must set either 'instrument' or 'ignore'")
      : DataResult.success(ingredient)));

  /** Network codec: item ID, then either the instrument key or the ignored tag, matching the old byte layout */
  public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentIngredient> STREAM_CODEC = StreamCodec.of(
    (buffer, ingredient) -> {
      buffer.writeVarInt(BuiltInRegistries.ITEM.getId(ingredient.item));
      if (ingredient.instrument != null) {
        buffer.writeBoolean(true);
        ResourceLocation.STREAM_CODEC.encode(buffer, ingredient.instrument.location());
      } else {
        assert ingredient.ignore != null;
        buffer.writeBoolean(false);
        ResourceLocation.STREAM_CODEC.encode(buffer, ingredient.ignore.location());
      }
    },
    buffer -> {
      // swap missing items for barriers
      Item item = BuiltInRegistries.ITEM.byId(buffer.readVarInt());
      if (item == null) {
        item = Items.BARRIER;
      }
      ResourceKey<Instrument> instrument = null;
      TagKey<Instrument> ignore = null;
      if (buffer.readBoolean()) {
        instrument = ResourceKey.create(Registries.INSTRUMENT, ResourceLocation.STREAM_CODEC.decode(buffer));
      } else {
        ignore = TagKey.create(Registries.INSTRUMENT, ResourceLocation.STREAM_CODEC.decode(buffer));
      }
      return new InstrumentIngredient(item, instrument, ignore);
    });

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final IngredientType<InstrumentIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final Item item;
  @Nullable
  private final ResourceKey<Instrument> instrument;
  @Nullable
  private final TagKey<Instrument> ignore;

  /** Creates a new instance matching the given instrument */
  public static InstrumentIngredient of(ItemLike item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), instrument, null);
  }

  /** Creates a new instance ignoring the given tag */
  public static InstrumentIngredient of(ItemLike item, TagKey<Instrument> ignore) {
    return new InstrumentIngredient(item.asItem(), null, ignore);
  }

  /** @return the item this ingredient matches */
  public Item getItem() {
    return item;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || !stack.is(item)) {
      return false;
    }
    Holder<Instrument> holder = stack.get(DataComponents.INSTRUMENT);
    if (holder != null) {
      // if we just want the instrument to match, can just compare by ID over doing a fetch
      if (instrument != null) {
        return holder.unwrapKey().map(key -> instrument.location().equals(key.location())).orElse(false);
      }
      // must be valid and not in the tag
      assert ignore != null;
      return !holder.is(ignore);
    }
    // no instrument set, its fine as long as we don't have a specific instrument
    return instrument == null;
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(item);
    // set the instrument on the stack for display
    if (instrument != null) {
      BuiltInRegistries.INSTRUMENT.getHolder(instrument).ifPresent(reference -> stack.set(DataComponents.INSTRUMENT, reference));
    }
    return Stream.of(stack);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}


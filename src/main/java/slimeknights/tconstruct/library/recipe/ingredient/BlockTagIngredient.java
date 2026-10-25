package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Custom ingredient matching items with a block form in the given tag */
@RequiredArgsConstructor
@EqualsAndHashCode(of = "tag")
public class BlockTagIngredient implements ICustomIngredient {
  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final ResourceLocation ID = TConstruct.getResource("block_tag");

  /** Codec keeping the legacy JSON layout: {@code {"type": "tconstruct:block_tag", "tag": ...}} */
  public static final MapCodec<BlockTagIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.BLOCK_TAG.fieldOf("tag").forGetter(ingredient -> ingredient.tag)
  ).apply(instance, BlockTagIngredient::new));

  /** Network codec: the block tag, symmetric on either side (the client rebuilds the item list from the synced tag) */
  public static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = StreamCodec.composite(
    new StreamableCodec<>(Loadables.BLOCK_TAG), ingredient -> ingredient.tag,
    BlockTagIngredient::new);

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerCommons */
  public static final IngredientType<BlockTagIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final TagKey<Block> tag;
  @Nullable
  private Set<Item> matchingItems;
  @Nullable
  private ItemStack[] items;

  /** @return the matched block tag */
  public TagKey<Block> getTag() {
    return tag;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && getMatchingItems().contains(stack.getItem());
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (items == null) {
      items = getMatchingItems().stream().map(ItemStack::new).toArray(ItemStack[]::new);
    }
    return Arrays.stream(items);
  }

  /** Gets the ordered matching items set */
  private Set<Item> getMatchingItems() {
    if (matchingItems == null) {
      matchingItems = RegistryHelper.getTagValueStream(BuiltInRegistries.BLOCK, tag)
                                    .map(Block::asItem)
                                    .filter(item -> item != Items.AIR)
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    return matchingItems;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean isSimple() {
    return true;
  }
}

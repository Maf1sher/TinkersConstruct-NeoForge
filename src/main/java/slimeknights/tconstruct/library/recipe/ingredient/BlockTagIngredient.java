package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Item ingredient matching items with a block form in the given tag */
@RequiredArgsConstructor
public class BlockTagIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("block_tag");

  private final TagKey<Block> tag;
  @Nullable
  private Set<Item> matchingItems;
  @Nullable
  private ItemStack[] items;

  /** MapCodec for JSON serialization */
  public static final MapCodec<BlockTagIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
    instance.group(
      TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(i -> i.tag)
    ).apply(instance, BlockTagIngredient::new)
  );

  /** StreamCodec for network serialization - writes item list, becomes vanilla ingredient client side */
  public static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public BlockTagIngredient decode(RegistryFriendlyByteBuf buffer) {
      // read as item stacks - on client side, reconstruct from stacks
      // This is a simplified approach since we can't reconstruct the block tag on client
      int size = buffer.readVarInt();
      Set<Item> items = new LinkedHashSet<>();
      for (int i = 0; i < size; i++) {
        items.add(ItemStack.STREAM_CODEC.decode(buffer).getItem());
      }
      // We need a tag to construct, but on client side we use the items directly
      // Since the tag won't resolve on client, we store the items for matching
      // For network, we just send a dummy tag and set items directly
      BlockTagIngredient ingredient = new BlockTagIngredient(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("air")));
      ingredient.matchingItems = items;
      ingredient.items = items.stream().map(ItemStack::new).toArray(ItemStack[]::new);
      return ingredient;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, BlockTagIngredient ingredient) {
      ItemStack[] stacks = ingredient.items != null ? ingredient.items : ingredient.getItems().toArray(ItemStack[]::new);
      buffer.writeVarInt(stacks.length);
      for (ItemStack stack : stacks) {
        ItemStack.STREAM_CODEC.encode(buffer, stack);
      }
    }
  };

  /** IngredientType instance - must be registered to NeoForgeRegistries.INGREDIENT_TYPES */
  public static final IngredientType<BlockTagIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && getMatchingItems().contains(stack.getItem());
  }

  @Override
  public boolean isSimple() {
    return true;
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
  public Stream<ItemStack> getItems() {
    if (items == null) {
      items = getMatchingItems().stream().map(ItemStack::new).toArray(ItemStack[]::new);
    }
    return Stream.of(items);
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** Converts this custom ingredient to a vanilla Ingredient */
  public Ingredient toIngredient() {
    return toVanilla();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BlockTagIngredient that)) return false;
    return tag.equals(that.tag);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }
}

package slimeknights.tconstruct.library.json.loot;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.library.materials.RandomMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tools.TinkerToolParts;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Entry for a random tool part from a list with a random material */
public class ToolPartLootEntry extends LootPoolSingletonContainer {
  /** Codec for RandomMaterial bridging from Loadable to DynamicOps */
  private static final Codec<RandomMaterial> RANDOM_MATERIAL_CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<RandomMaterial, T>> decode(DynamicOps<T> ops, T input) {
      try {
        JsonElement json = ops.convertTo(JsonOps.INSTANCE, input);
        RandomMaterial mat = RandomMaterial.LOADER.convert(json, "material");
        return DataResult.success(Pair.of(mat, input));
      } catch (Exception e) {
        return DataResult.error(() -> "Failed to decode RandomMaterial: " + e.getMessage());
      }
    }

    @Override
    public <T> DataResult<T> encode(RandomMaterial input, DynamicOps<T> ops, T prefix) {
      try {
        JsonElement json = RandomMaterial.LOADER.serialize(input);
        return DataResult.success(JsonOps.INSTANCE.convertTo(ops, json));
      } catch (Exception e) {
        return DataResult.error(() -> "Failed to encode RandomMaterial: " + e.getMessage());
      }
    }
  };

  public static final MapCodec<ToolPartLootEntry> CODEC = RecordCodecBuilder.mapCodec(
    instance -> singletonFields(instance)
      .and(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(e -> e.tag))
      .and(RANDOM_MATERIAL_CODEC.fieldOf("material").forGetter(e -> e.material))
      .apply(instance, ToolPartLootEntry::new)
  );

  private final TagKey<Item> tag;
  private final RandomMaterial material;

  protected ToolPartLootEntry(int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions, TagKey<Item> tag, RandomMaterial material) {
    super(weight, quality, conditions, functions);
    this.tag = tag;
    this.material = material;
  }

  @Override
  public LootPoolEntryType getType() {
    return TinkerToolParts.toolPartLootEntry.get();
  }

  @Override
  protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
    List<IToolPart> options = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, tag)
      .flatMap(item -> item instanceof IToolPart mat ? Stream.of(mat) : Stream.empty()).toList();
    if (!options.isEmpty()) {
      RandomSource random = context.getRandom();
      IToolPart choice = options.get(random.nextInt(options.size()));
      MaterialVariantId material = this.material.getMaterial(choice.getStatType(), random);
      if (choice.canUseMaterial(material.getId())) {
        consumer.accept(choice.withMaterial(material));
      }
    }
  }


  /* Builders */

  /** Creates a builder with the given material */
  public static LootPoolSingletonContainer.Builder<?> entry(TagKey<Item> tag, RandomMaterial material) {
    return simpleBuilder((weight, quality, conditions, functions) -> new ToolPartLootEntry(weight, quality, conditions, functions, tag, material));
  }

  /** Creates a builder for a fixed material */
  public static LootPoolSingletonContainer.Builder<?> fixed(TagKey<Item> tag, MaterialVariantId material) {
    return entry(tag, RandomMaterial.fixed(material));
  }
}

package slimeknights.tconstruct.library.json.predicate.tool;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/**
 * Custom ItemSubPredicate for matching Tinker tools using tool stack predicates.
 * In 1.21, ItemPredicate is final so we implement ItemSubPredicate instead.
 */
@RequiredArgsConstructor
public class ToolStackItemPredicate implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");

  /** Codec bridging ToolStackPredicate.LOADER (Loadable) to DynamicOps */
  private static final Codec<IJsonPredicate<IToolStackView>> PREDICATE_CODEC = new Codec<>() {
    @Override
    public <T> DataResult<Pair<IJsonPredicate<IToolStackView>, T>> decode(DynamicOps<T> ops, T input) {
      try {
        JsonElement json = ops.convertTo(JsonOps.INSTANCE, input);
        IJsonPredicate<IToolStackView> predicate = ToolStackPredicate.LOADER.convert(json, "predicate");
        return DataResult.success(Pair.of(predicate, input));
      } catch (Exception e) {
        return DataResult.error(() -> "Failed to decode ToolStack predicate: " + e.getMessage());
      }
    }

    @Override
    public <T> DataResult<T> encode(IJsonPredicate<IToolStackView> input, DynamicOps<T> ops, T prefix) {
      try {
        JsonElement json = ToolStackPredicate.LOADER.serialize(input);
        return DataResult.success(JsonOps.INSTANCE.convertTo(ops, json));
      } catch (Exception e) {
        return DataResult.error(() -> "Failed to encode ToolStack predicate: " + e.getMessage());
      }
    }
  };

  /** Codec for this sub-predicate type */
  public static final Codec<ToolStackItemPredicate> CODEC = RecordCodecBuilder.create(
    instance -> instance.group(
      PREDICATE_CODEC.fieldOf("predicate").forGetter(p -> p.predicate)
    ).apply(instance, ToolStackItemPredicate::new)
  );

  /** The registered type for this sub-predicate - set during mod init */
  public static ItemSubPredicate.Type<ToolStackItemPredicate> TYPE;

  private final IJsonPredicate<IToolStackView> predicate;

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidentally modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }

  /** Creates an ItemPredicate with this tool predicate embedded as a sub-predicate */
  public static ItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return ItemPredicate.Builder.item()
      .withSubPredicate(TYPE, new ToolStackItemPredicate(predicate))
      .build();
  }

  /** Creates an ItemPredicate with a context predicate */
  public static ItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return ofTool(ToolStackPredicate.context(predicate));
  }
}

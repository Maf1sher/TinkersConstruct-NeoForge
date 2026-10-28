package slimeknights.tconstruct.library.json.predicate.tool;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.codec.LoadableCodec;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Variant of ItemPredicate for matching Tinker tools using {@link ToolStackPredicate} */
public record ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) implements ItemSubPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  /** Codec matching the legacy {@code {"predicate": {...}}} JSON shape */
  public static final Codec<ToolStackItemPredicate> CODEC = new LoadableCodec<>(ToolStackPredicate.LOADER)
    .xmap(ToolStackItemPredicate::new, ToolStackItemPredicate::predicate);
  /** Registered sub-predicate type; must be the same instance used in the item predicates */
  public static final ItemSubPredicate.Type<ToolStackItemPredicate> TYPE = new ItemSubPredicate.Type<>(CODEC);

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidentally modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }
}

package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.codec.JsonMapCodec;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Custom ingredient that only matches tools with a specific hook */
@RequiredArgsConstructor
@EqualsAndHashCode(of = {"tag", "hook"})
public class ToolHookIngredient implements ICustomIngredient {
  /** Ingredient type, replaces the old Forge serializer; registered in TinkerTools */
  public static final ResourceLocation ID = TConstruct.getResource("tool_hook");

  /** Codec keeping the legacy JSON layout: {@code {"type": "tconstruct:tool_hook", "tag": ..., "hook": ...}} */
  public static final MapCodec<ToolHookIngredient> CODEC = new JsonMapCodec<>() {
    @Override
    protected ToolHookIngredient deserialize(JsonObject json, DynamicOps<?> ops) {
      return new ToolHookIngredient(
        Loadables.ITEM_TAG.getOrDefault(json, "tag", TinkerTags.Items.MODIFIABLE),
        ToolHooks.LOADER.getIfPresent(json, "hook"));
    }

    @Override
    protected JsonObject serialize(ToolHookIngredient ingredient, DynamicOps<?> ops) {
      JsonObject json = new JsonObject();
      json.addProperty("tag", ingredient.tag.location().toString());
      json.addProperty("hook", ingredient.hook.getId().toString());
      return json;
    }
  };

  /** Network codec: item tag followed by the hook, matching the old byte layout */
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolHookIngredient> STREAM_CODEC = StreamCodec.composite(
    new StreamableCodec<>(Loadables.ITEM_TAG), ToolHookIngredient::getTag,
    new StreamableCodec<>(ToolHooks.LOADER), ToolHookIngredient::getHook,
    ToolHookIngredient::new);

  /** Ingredient type, replaces the old Forge serializer; registered in TinkerTools */
  public static final IngredientType<ToolHookIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  /** @return the tag this ingredient tests before the hook */
  public TagKey<Item> getTag() {
    return tag;
  }

  /** @return the hook this ingredient requires */
  public ModuleHook<?> getHook() {
    return hook;
  }

  public static ToolHookIngredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook);
  }

  public static ToolHookIngredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public Stream<ItemStack> getItems() {
    List<ItemStack> list = new ArrayList<>();
    // filtered version of tag values
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
        list.add(new ItemStack(modifiable));
      }
    }
    // if empty, add a barrier to make "no matches" visually obvious
    if (list.isEmpty()) {
      ItemStack barrier = new ItemStack(Blocks.BARRIER);
      barrier.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + tag.location()));
      list.add(barrier);
    }
    return list.stream();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }
}

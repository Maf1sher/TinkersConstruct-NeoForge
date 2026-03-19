package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that only matches tools with a specific hook */
public class ToolHookIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("tool_hook");

  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;
  @Nullable
  private ItemStack[] items;

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
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
    if (items == null) {
      List<ItemStack> list = new ArrayList<>();
      for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
        if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
          list.add(new ItemStack(modifiable));
        }
      }
      if (list.isEmpty()) {
        ItemStack barrier = new ItemStack(Blocks.BARRIER);
        barrier.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + tag.location()));
        list.add(barrier);
      }
      items = list.toArray(new ItemStack[0]);
    }
    return Stream.of(items);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** Converts this custom ingredient to a vanilla Ingredient */
  public Ingredient toIngredient() {
    return toVanilla();
  }

  /** MapCodec for JSON serialization */
  public static final MapCodec<ToolHookIngredient> CODEC = new MapCodec<>() {
    @Override
    public <T> Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
      return Stream.of(ops.createString("tag"), ops.createString("hook"));
    }

    @Override
    public <T> com.mojang.serialization.DataResult<ToolHookIngredient> decode(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> input) {
      try {
        // Parse tag
        T tagValue = input.get("tag");
        TagKey<Item> tag;
        if (tagValue != null) {
          String tagStr = ops.getStringValue(tagValue).result().orElse(null);
          if (tagStr != null) {
            tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(tagStr));
          } else {
            tag = TinkerTags.Items.MODIFIABLE;
          }
        } else {
          tag = TinkerTags.Items.MODIFIABLE;
        }

        // Parse hook
        T hookValue = input.get("hook");
        if (hookValue == null) {
          return com.mojang.serialization.DataResult.error(() -> "Missing 'hook' field");
        }
        String hookStr = ops.getStringValue(hookValue).result().orElse(null);
        if (hookStr == null) {
          return com.mojang.serialization.DataResult.error(() -> "Invalid 'hook' field");
        }
        ResourceLocation hookId = ResourceLocation.parse(hookStr);
        ModuleHook<?> hook = ToolHooks.LOADER.getValue(hookId);

        return com.mojang.serialization.DataResult.success(new ToolHookIngredient(tag, hook));
      } catch (Exception e) {
        return com.mojang.serialization.DataResult.error(() -> "Failed to decode ToolHookIngredient: " + e.getMessage());
      }
    }

    @Override
    public <T> com.mojang.serialization.RecordBuilder<T> encode(ToolHookIngredient input, com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.RecordBuilder<T> prefix) {
      prefix.add("tag", ops.createString(input.tag.location().toString()));
      prefix.add("hook", ops.createString(input.hook.getId().toString()));
      return prefix;
    }
  };

  /** StreamCodec for network serialization */
  public static final StreamCodec<RegistryFriendlyByteBuf, ToolHookIngredient> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public ToolHookIngredient decode(RegistryFriendlyByteBuf buffer) {
      return new ToolHookIngredient(
        Loadables.ITEM_TAG.decode(buffer),
        ToolHooks.LOADER.decode(buffer)
      );
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, ToolHookIngredient ingredient) {
      Loadables.ITEM_TAG.encode(buffer, ingredient.tag);
      ToolHooks.LOADER.encode(buffer, ingredient.hook);
    }
  };

  /** IngredientType instance - must be registered to NeoForgeRegistries.INGREDIENT_TYPES */
  public static final IngredientType<ToolHookIngredient> TYPE = new IngredientType<>(CODEC, STREAM_CODEC);
}

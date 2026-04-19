package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import lombok.Getter;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.LogicHelper;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 */
public class ShapedMaterialsRecipe extends ShapedRecipe implements MaterialsCraftingTableRecipe {
  /** List of tool parts to search for in the final recipe */
  @Getter
  private final List<Ingredient> parts;
  /**
   * If true, a part may show up multiple times in the inputs, and all copies should match.
   * If false, only the first instance of a part is checked for each input, allowing a tool with the same part multiple times.
   */
  private final boolean checkRepeats;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  public ShapedMaterialsRecipe(ShapedRecipe recipe, List<Ingredient> parts, List<MaterialVariantId> extraMaterials) {
    super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.result, recipe.showNotification());
    this.parts = parts;
    this.checkRepeats = parts.stream().unordered().distinct().count() == parts.size();
    this.extraMaterials = extraMaterials;
  }

  @Override
  public int getPartCount() {
    return parts.size();
  }

  /**
   * Finds materials for each of the parts
   * @return Array of all matched materials. Array will have no null entries, though the array may be null if no match was found.
   */
  @Nullable
  static MaterialVariantId[] findMaterials(CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats) {
    // want one material for each
    MaterialVariantId[] materials = new MaterialVariantId[partCount];
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        for (int p = 0; p < partCount; p++) {
          MaterialVariantId current = materials[p];
          // if we have not found the material yet, or repeats are considered the same material, test the ingredient
          if ((current == null || checkRepeats) && parts.get(p).test(stack)) {
            MaterialVariantId matched;
            if (stack.getItem() instanceof IMaterialItem materialItem) {
              matched = materialItem.getMaterial(stack);
            } else {
              matched = MaterialRecipeCache.findRecipe(stack).getMaterial().getVariant();
            }
            // first occurrence? thats our material
            if (current == null) {
              materials[p] = matched;
              break;
            } else if (!current.matchesVariant(matched)) {
              // if same material but different variants, just discard the variant
              if (current.getId().equals(matched.getId())) {
                materials[p] = current.getId();
                break;
              } else {
                // if different materials, no match
                return null;
              }
            }
          }
        }
      }
    }
    // ensure we found all materials needed
    for (int p = 0; p < partCount; p++) {
      if (materials[p] == null) {
        return null;
      }
    }
    return materials;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }
    // ensure all part materials matched and we found all parts
    return findMaterials(inventory, parts, parts.size(), checkRepeats) != null;
  }

  /** Common logic to this and {@link ShapedMaterialsRecipe} */
  public static void setMaterial(ItemStack stack, MaterialVariantId material, List<MaterialVariantId> extraMaterials) {
    if (extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
      materialItem.setMaterial(stack, material);
    } else {
      MaterialNBT.Builder builder = MaterialNBT.builder();
      builder.add(material);
      for (MaterialVariantId extraMaterial : extraMaterials) {
        builder.add(extraMaterial);
      }
      ToolStack.from(stack).setMaterials(builder.build());
    }
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    setMaterial(stack, material, extraMaterials);
  }

  /** Assembles the item with material information */
  static ItemStack assemble(ItemStack stack, CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats, List<MaterialVariantId> extraMaterials) {
    MaterialVariantId[] materials = findMaterials(inventory, parts, partCount, checkRepeats);
    if (materials != null) {
      // if the result is a tool part, and we only have the one material, set its material
      if (materials.length == 1 && extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
        return materialItem.setMaterial(stack, materials[0]);
      }
      MaterialNBT.Builder builder = MaterialNBT.builder();
      // add each material
      for (MaterialVariantId material : materials) {
        builder.add(material);
      }
      // add extra materials
      builder.add(extraMaterials);
      ToolStack.from(stack).setMaterials(builder.build());
    }
    return stack;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registryAccess) {
    return assemble(super.assemble(inventory, registryAccess), inventory, parts, parts.size(), checkRepeats, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.shapedMaterialsRecipeSerializer.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedMaterialsRecipe> {
    /** Codec for MaterialVariantId - parses from string representation */
    private static final Codec<MaterialVariantId> MATERIAL_VARIANT_CODEC = Codec.STRING.comapFlatMap(
      s -> {
        MaterialVariantId id = MaterialVariantId.tryParse(s);
        return id != null ? DataResult.success(id) : DataResult.error(() -> "Invalid material variant ID: " + s);
      },
      MaterialVariantId::toString
    );
    private static final Codec<List<MaterialVariantId>> EXTRA_MATERIALS_CODEC = MATERIAL_VARIANT_CODEC.listOf();

    /** StreamCodec for list of MaterialVariantId */
    private static final StreamCodec<RegistryFriendlyByteBuf, List<MaterialVariantId>> EXTRA_MATERIALS_STREAM_CODEC =
      StreamCodec.of(
        (buf, list) -> {
          buf.writeVarInt(list.size());
          for (MaterialVariantId id : list) {
            id.toNetwork(buf);
          }
        },
        buf -> {
          int size = buf.readVarInt();
          ArrayList<MaterialVariantId> list = new ArrayList<>(size);
          for (int i = 0; i < size; i++) {
            list.add(MaterialVariantId.fromNetwork(buf));
          }
          return List.copyOf(list);
        }
      );

    /**
     * The MapCodec for ShapedMaterialsRecipe. Supports two field-name formats for the parts list:
     * <ul>
     *   <li>{@code "parts"}: a string of pattern key characters (e.g. {@code "c"}), resolved to
     *       ingredients via the shaped recipe's {@code key} map. This is the format produced by
     *       TCon's data-gen.</li>
     *   <li>{@code "part_ingredients"}: an explicit JSON array of ingredient objects.</li>
     * </ul>
     * Encoding always writes {@code "part_ingredients"} as an explicit ingredient list.
     */
    public static final MapCodec<ShapedMaterialsRecipe> CODEC = new MapCodec<>() {
      @Override
      public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.concat(
          ShapedRecipe.Serializer.CODEC.keys(ops),
          Stream.of(ops.createString("part_ingredients"), ops.createString("parts"), ops.createString("extra_materials")));
      }

      @Override
      public <T> DataResult<ShapedMaterialsRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
        return ShapedRecipe.Serializer.CODEC.decode(ops, input).flatMap(base -> {
          // decode optional extra_materials
          T extraT = input.get("extra_materials");
          DataResult<List<MaterialVariantId>> extraResult = extraT != null
            ? EXTRA_MATERIALS_CODEC.parse(ops, extraT)
            : DataResult.success(List.of());

          // "parts" string format: each character is a key in the shaped pattern's key map
          T partsStrT = input.get("parts");
          if (partsStrT != null) {
            return ShapedRecipePattern.Data.MAP_CODEC.decode(ops, input).flatMap(patternData ->
              ops.getStringValue(partsStrT).flatMap(partsStr -> {
                var keyMap = patternData.key(); // Map<Character, Ingredient>
                List<Ingredient> partIngredients = new ArrayList<>();
                for (int i = 0; i < partsStr.length(); i++) {
                  char c = partsStr.charAt(i);
                  Ingredient ing = keyMap.get(c);
                  if (ing == null) {
                    char fc = c;
                    return DataResult.error(() -> "Unknown pattern key '" + fc + "' referenced in 'parts'");
                  }
                  partIngredients.add(ing);
                }
                return extraResult.map(extra -> new ShapedMaterialsRecipe(base, List.copyOf(partIngredients), extra));
              })
            );
          }

          // "part_ingredients" list format: explicit list of ingredient objects
          T partIngrT = input.get("part_ingredients");
          if (partIngrT != null) {
            return Ingredient.CODEC.listOf().parse(ops, partIngrT)
              .flatMap(parts -> extraResult.map(extra -> new ShapedMaterialsRecipe(base, parts, extra)));
          }

          return DataResult.error(() -> "Missing required 'parts' or 'part_ingredients' field in shaped materials recipe");
        });
      }

      @Override
      public <T> RecordBuilder<T> encode(ShapedMaterialsRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        ShapedRecipe.Serializer.CODEC.encode(input, ops, prefix);
        prefix.add("part_ingredients", Ingredient.CODEC.listOf().encodeStart(ops, input.parts));
        if (!input.extraMaterials.isEmpty()) {
          prefix.add("extra_materials", EXTRA_MATERIALS_CODEC.encodeStart(ops, input.extraMaterials));
        }
        return prefix;
      }
    };

    /**
     * StreamCodec for network sync. We sync the shaped recipe base fields,
     * then sync parts as a list of ingredients and the extra materials.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedMaterialsRecipe> STREAM_CODEC = StreamCodec.of(
      Serializer::toNetwork, Serializer::fromNetwork
    );

    @Override
    public MapCodec<ShapedMaterialsRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapedMaterialsRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static ShapedMaterialsRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      // decode the base shaped recipe
      ShapedRecipe base = ShapedRecipe.Serializer.STREAM_CODEC.decode(buffer);

      // decode parts
      int partCount = buffer.readVarInt();
      List<Ingredient> parts = new ArrayList<>(partCount);
      for (int i = 0; i < partCount; i++) {
        parts.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
      }

      // decode extra materials
      List<MaterialVariantId> extraMaterials = EXTRA_MATERIALS_STREAM_CODEC.decode(buffer);

      return new ShapedMaterialsRecipe(base, List.copyOf(parts), extraMaterials);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedMaterialsRecipe recipe) {
      // encode the base shaped recipe
      ShapedRecipe.Serializer.STREAM_CODEC.encode(buffer, recipe);

      // encode parts
      buffer.writeVarInt(recipe.parts.size());
      for (Ingredient part : recipe.parts) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, part);
      }

      // encode extra materials
      EXTRA_MATERIALS_STREAM_CODEC.encode(buffer, recipe.extraMaterials);
    }
  }
}

package slimeknights.tconstruct.library.recipe.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
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
     * The MapCodec for ShapedMaterialsRecipe.
     * We encode parts as a "parts" string that references keys from the shaped pattern.
     * The shaped recipe itself is decoded from the standard fields, plus we add "parts" and "extra_materials".
     */
    public static final MapCodec<ShapedMaterialsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
      ShapedRecipe.Serializer.CODEC.forGetter(r -> (ShapedRecipe) r),
      Ingredient.CODEC.listOf().fieldOf("part_ingredients").forGetter(r -> r.parts),
      EXTRA_MATERIALS_CODEC.optionalFieldOf("extra_materials", List.of()).forGetter(r -> r.extraMaterials)
    ).apply(instance, ShapedMaterialsRecipe::new));

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

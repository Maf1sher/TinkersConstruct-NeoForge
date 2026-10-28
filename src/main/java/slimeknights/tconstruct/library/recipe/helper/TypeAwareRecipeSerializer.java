package slimeknights.tconstruct.library.recipe.helper;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Supplier;

/**
 * Type aware recipe serializer, providing access to the recipe type for the recipe.
 * Replaces the removed Mantle helper of the same name on top of {@link LoadableRecipeSerializer}.
 * @param <T>  Recipe type
 */
public class TypeAwareRecipeSerializer<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
  private final Supplier<? extends RecipeType<?>> type;

  private TypeAwareRecipeSerializer(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
    super(loadable);
    this.type = type;
  }

  /** Creates a type aware serializer from a loadable and a recipe type supplier */
  public static <T extends Recipe<?>> TypeAwareRecipeSerializer<T> create(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
    return new TypeAwareRecipeSerializer<>(loadable, type);
  }

  /** Gets the recipe type for this serializer */
  public RecipeType<?> getType() {
    return type.get();
  }

  /** Applies the serializer and type to the context */
  @Override
  public void accept(TypedMapBuilder builder) {
    super.accept(builder);
    builder.put(LoadableRecipeSerializer.TYPE, type.get());
  }

  /** Context field to fetch a type aware serializer from the recipe context */
  @SuppressWarnings("unchecked")
  public static final RecordField<TypeAwareRecipeSerializer<?>,Recipe<?>> TYPED_SERIALIZER =
    (RecordField<TypeAwareRecipeSerializer<?>,Recipe<?>>) (RecordField<?,?>) LoadableRecipeSerializer.SERIALIZER.mappedField((value, error) -> {
      if (value instanceof TypeAwareRecipeSerializer<?> aware) {
        return aware;
      }
      ErrorFactory.JSON_SYNTAX_ERROR.accept("Serializer in context is not type aware");
      throw new AssertionError();
    });
}

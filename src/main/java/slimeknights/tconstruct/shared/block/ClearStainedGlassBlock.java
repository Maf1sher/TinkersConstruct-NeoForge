package slimeknights.tconstruct.shared.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.DyeColor;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class ClearStainedGlassBlock extends TransparentBlock implements BeaconBeamBlock {

  public static final MapCodec<ClearStainedGlassBlock> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(
      GlassColor.CODEC.fieldOf("color").forGetter(b -> b.glassColor),
      propertiesCodec()
    ).apply(instance, (color, props) -> new ClearStainedGlassBlock(props, color))
  );

  private final GlassColor glassColor;
  public ClearStainedGlassBlock(Properties properties, GlassColor glassColor) {
    super(properties);
    this.glassColor = glassColor;
  }

  @Override
  protected MapCodec<? extends ClearStainedGlassBlock> codec() {
    return CODEC;
  }

  @Override
  public DyeColor getColor() {
    return this.glassColor.getDye();
  }

  /** Gets the variant color used for tinting */
  public int getVariantColor() {
    return this.glassColor.getColor();
  }

  /** Gets the glass color enum for this block */
  public GlassColor getGlassColor() {
    return this.glassColor;
  }

  /** Enum used for registration of this and the pane block */
  public enum GlassColor implements StringRepresentable {
    WHITE(0xffffff, DyeColor.WHITE),
    ORANGE(0xd87f33, DyeColor.ORANGE),
    MAGENTA(0xb24cd8, DyeColor.MAGENTA),
    LIGHT_BLUE(0x6699d8, DyeColor.LIGHT_BLUE),
    YELLOW(0xe5e533, DyeColor.YELLOW),
    LIME(0x7fcc19, DyeColor.LIME),
    PINK(0xf27fa5, DyeColor.PINK),
    GRAY(0x4c4c4c, DyeColor.GRAY),
    LIGHT_GRAY(0x999999, DyeColor.LIGHT_GRAY),
    CYAN(0x4c7f99, DyeColor.CYAN),
    PURPLE(0x7f3fb2, DyeColor.PURPLE),
    BLUE(0x334cb2, DyeColor.BLUE),
    BROWN(0x664c33, DyeColor.BROWN),
    GREEN(0x667f33, DyeColor.GREEN),
    RED(0x993333, DyeColor.RED),
    BLACK(0x191919, DyeColor.BLACK);

    public static final com.mojang.serialization.Codec<GlassColor> CODEC = StringRepresentable.fromEnum(GlassColor::values);

    private final int color;
    private final DyeColor dye;
    private final float[] rgb;
    private final String name;

    GlassColor(int color, DyeColor dye) {
      this.color = color;
      this.dye = dye;
      this.rgb = calcRGB(color);
      this.name = this.name().toLowerCase(Locale.US);
    }

    /**
     * Converts the color into an RGB float array
     * @param color  Color input
     * @return  Float array
     */
    private static float[] calcRGB(int color) {
      float[] out = new float[3];
      out[0] = ((color >> 16) & 0xFF) / 255f;
      out[1] = ((color >> 8) & 0xFF) / 255f;
      out[2] = (color & 0xFF) / 255f;
      return out;
    }

    /**
     * Variant color to reduce number of models
     * @return  Variant color for BlockColors and ItemColors
     */
    public int getColor() {
      return this.color;
    }

    /** Gets the vanilla dye color associated with this color */
    public DyeColor getDye() {
      return dye;
    }

    /**
     * Gets the RGB value for this color as an array
     * @return  Color RGB for beacon
     */
    public float[] getRgb() {
      return this.rgb;
    }

    @Override
    public String getSerializedName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}

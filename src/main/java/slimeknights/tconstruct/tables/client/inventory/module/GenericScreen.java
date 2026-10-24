package slimeknights.tconstruct.tables.client.inventory.module;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.screen.element.TextureElement;
import slimeknights.tconstruct.TConstruct;

public class GenericScreen {
  public static final ResourceLocation LOCATION = TConstruct.getResource("textures/gui/generic.png");

  // first one sets default texture w/h
  public static final TextureElement cornerTopLeft = new TextureElement(LOCATION, 0, 0, 7, 7, 64, 64);
  public static final TextureElement cornerTopRight = cornerTopLeft.move(64 - 7, 0, 7, 7);
  public static final TextureElement cornerBottomLeft = cornerTopLeft.move(0, 64 - 7, 7, 7);
  public static final TextureElement cornerBottomRight = cornerTopLeft.move(64 - 7, 64 - 7, 7, 7);

  public static final TextureElement borderTop = new TextureElement(LOCATION, 7, 0, 64 - 7 - 7, 7, 64, 64);
  public static final TextureElement borderBottom = borderTop.move(7, 64 - 7, 64 - 7 - 7, 7);
  public static final TextureElement borderLeft = borderTop.move(0, 7, 7, 64 - 7 - 7);
  public static final TextureElement borderRight = borderTop.move(64 - 7, 7, 7, 64 - 7 - 7);

  public static final TextureElement overlap = borderTop.move(21, 45, 7, 14);
  public static final TextureElement overlapTopLeft = cornerTopLeft.move(7, 40, 7, 7);
  public static final TextureElement overlapTopRight = cornerTopLeft.move(14, 40, 7, 7);
  public static final TextureElement overlapBottomLeft = cornerTopLeft.move(7, 47, 7, 7);
  public static final TextureElement overlapBottomRight = cornerTopLeft.move(14, 47, 7, 7);

  public static final TextureElement textBackground = borderTop.move(7 + 18, 7, 18, 10);

  public static final TextureElement slot = borderTop.move(7, 7, 18, 18);
  public static final TextureElement slotEmpty = borderTop.move(7 + 18, 7, 18, 18);

  public static final TextureElement sliderNormal = cornerTopLeft.move(7, 25, 10, 15);
  public static final TextureElement sliderLow = cornerTopLeft.move(17, 25, 10, 15);
  public static final TextureElement sliderHigh = cornerTopLeft.move(27, 25, 10, 15);
  public static final TextureElement sliderTop = cornerTopLeft.move(43, 7, 12, 1);
  public static final TextureElement sliderBottom = cornerTopLeft.move(43, 38, 12, 1);
  public static final TextureElement sliderBackground = borderTop.move(43, 8, 12, 30);
}

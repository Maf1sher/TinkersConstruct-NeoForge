package slimeknights.tconstruct.tables.client.inventory.widget;

import net.minecraft.client.gui.GuiGraphics;
import slimeknights.mantle.client.screen.ModuleScreen;
import slimeknights.mantle.client.screen.element.TextureElement;
import slimeknights.mantle.client.screen.widget.Widget;
import slimeknights.tconstruct.tables.client.inventory.module.GenericScreen;

public class BorderWidget extends Widget {
  // all elements based on generic screen
  public TextureElement cornerTopLeft = GenericScreen.cornerTopLeft;
  public TextureElement cornerTopRight = GenericScreen.cornerTopRight;
  public TextureElement cornerBottomLeft = GenericScreen.cornerBottomLeft;
  public TextureElement cornerBottomRight = GenericScreen.cornerBottomRight;

  public TextureElement borderTop = GenericScreen.borderTop;
  public TextureElement borderBottom = GenericScreen.borderBottom;
  public TextureElement borderLeft = GenericScreen.borderLeft;
  public TextureElement borderRight = GenericScreen.borderRight;

  // TODO: this is not used, was it supposed to be used?

  public int w = borderLeft.width();
  public int h = borderTop.height();

  /** Sets the size so that the given point is the upper left corner of the inside */
  public void setPosInner(int x, int y) {
    this.setPosition(x - this.cornerTopLeft.width(), y - this.cornerTopLeft.height());
  }

  /** Sets the size so that it surrounds the given area */
  public void sedSizeInner(int width, int height) {
    this.setSize(width + this.borderLeft.width() + this.borderRight.width(), height + this.borderTop.height() + this.borderBottom.height());
  }

  public int getWidthWithBorder(int width) {
    return width + this.borderRight.width() + this.borderLeft.width();
  }

  public int getHeightWithBorder(int height) {
    return height + this.borderTop.height() + this.borderBottom.height();
  }

  public void updateParent(ModuleScreen<?,?> gui) {
    gui.leftPos -= this.borderLeft.width();
    gui.topPos -= this.borderTop.height();

    gui.imageWidth += this.borderLeft.width() + this.borderRight.width();
    gui.imageHeight += this.borderTop.height() + this.borderBottom.height();
  }

  @Override
  public void draw(GuiGraphics graphics) {
    int x = this.xPos;
    int y = this.yPos;
    int midW = this.width - this.borderLeft.width() - this.borderRight.width();
    int midH = this.height - this.borderTop.height() - this.borderBottom.height();

    // top row
    x += drawX(this.cornerTopLeft, graphics, x, y);
    this.borderTop.drawScaledRight(graphics, x, y, 0, midW);
    x += midW;
    this.cornerTopRight.draw(graphics, x, y);

    // center row
    x = this.xPos;
    y += this.borderTop.height();
    this.borderLeft.drawScaledDown(graphics, x, y, 0, midH);
    x += this.borderLeft.width();
    x += midW;
    this.borderRight.drawScaledDown(graphics, x, y, 0, midH);

    // bottom row
    x = this.xPos;
    y += midH;
    x += drawX(this.cornerBottomLeft, graphics, x, y);
    this.borderBottom.drawScaledRight(graphics, x, y, 0, midW);
    x += midW;
    this.cornerBottomRight.draw(graphics, x, y);
  }

  /** Draws an element ans returns its width */
  private int drawX(TextureElement element, GuiGraphics graphics, int x, int y) {
    element.draw(graphics, x, y);
    return element.width();
  }
}

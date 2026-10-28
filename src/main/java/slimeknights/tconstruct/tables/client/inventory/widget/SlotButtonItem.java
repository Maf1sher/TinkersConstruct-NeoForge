package slimeknights.tconstruct.tables.client.inventory.widget;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import slimeknights.mantle.client.screen.element.TextureElement;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

public class SlotButtonItem extends Button {

  public static int WIDTH = 18, HEIGHT = 18;

  protected static final TextureElement BUTTON_PRESSED_GUI = new TextureElement(Icons.ICONS, 144, 216, WIDTH, HEIGHT, 256, 256);
  protected static final TextureElement BUTTON_NORMAL_GUI = new TextureElement(Icons.ICONS, 144 + WIDTH * 2, 216, WIDTH, HEIGHT, 256, 256);
  protected static final TextureElement BUTTON_HOVER_GUI = new TextureElement(Icons.ICONS, 144 + WIDTH * 4, 216, WIDTH, HEIGHT, 256, 256);

  @Getter
  private final StationSlotLayout layout;
  public boolean pressed;
  public final int buttonId;

  private TextureElement pressedGui = BUTTON_PRESSED_GUI;
  private TextureElement normalGui = BUTTON_NORMAL_GUI;
  private TextureElement hoverGui = BUTTON_HOVER_GUI;

  public SlotButtonItem(int buttonId, int x, int y, StationSlotLayout layout, OnPress onPress) {
    super(x, y, WIDTH, HEIGHT, layout.getDisplayName(), onPress, DEFAULT_NARRATION);
    this.layout = layout;
    this.buttonId = buttonId;
  }

  public SlotButtonItem setGraphics(TextureElement normal, TextureElement hover, TextureElement pressed) {
    this.pressedGui = pressed;
    this.normalGui = normal;
    this.hoverGui = hover;

    return this;
  }

  @Override
  public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      int x = getX();
      int y = getY();
      if (this.pressed) {
        this.pressedGui.draw(graphics, x, y);
      } else if (this.isHovered) {
        this.hoverGui.draw(graphics, x, y);
      } else {
        this.normalGui.draw(graphics, x, y);
      }
      //this.drawIcon(matrices, Minecraft.getInstance());
      TinkerStationScreen.renderIcon(graphics, layout.getIcon(), x + 1, y + 1);
  }

//  protected void drawIcon(MatrixStack matrices, Minecraft mc) {
//    mc.getItemRenderer().renderItemIntoGUI(this.icon, this.x + 1, this.y + 1);
//  }
}

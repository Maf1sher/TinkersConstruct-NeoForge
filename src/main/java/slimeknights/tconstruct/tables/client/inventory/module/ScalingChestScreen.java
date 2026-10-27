package slimeknights.tconstruct.tables.client.inventory.module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.tconstruct.tables.block.entity.inventory.IScalingContainer;

public class ScalingChestScreen<T extends BlockEntity> extends DynamicContainerScreen<MultiModuleScreen<?>,BaseContainerMenu<T>> {
  private final IScalingContainer scaling;
  public ScalingChestScreen(MultiModuleScreen<?> parent, BaseContainerMenu<T> container, Inventory playerInventory, Component title) {
    super(parent, container, playerInventory, title);
    BlockEntity tile = container.getTile();
    IItemHandler handler = EmptyItemHandler.INSTANCE;
    if (tile != null) {
      Level world = tile.getLevel();
      if (world != null) {
        IItemHandler cap = world.getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(), null);
        if (cap != null) {
          handler = cap;
        }
      }
    }
    this.scaling = handler instanceof IScalingContainer ? (IScalingContainer) handler : handler::getSlots;
    this.slotCount = scaling.getVisualSize();
    this.sliderActive = true;
  }

  @Override
  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    this.leftPos = parentX + this.xOffset;
    this.topPos = parentY + this.yOffset;

    // calculate rows and columns from space
    this.columns = (this.imageWidth - this.slider.width) / slot.width();
    this.rows = this.imageHeight / slot.height();

    this.updateSlider();
    this.updateSlots();
  }

  @Override
  protected void updateSlider() {
    this.sliderActive = this.slotCount > this.columns * this.rows;
    super.updateSlider();
    this.slider.setEnabled(this.sliderActive);
    this.slider.show();
  }

  @Override
  public void update(int mouseX, int mouseY) {
    this.slotCount = this.scaling.getVisualSize();
    super.update(mouseX, mouseY);
    this.updateSlider();
    this.updateSlots();
  }

  @Override
  public boolean shouldDrawSlot(Slot slot) {
    if (slot.getSlotIndex() >= this.scaling.getVisualSize()) {
      return false;
    }
    return super.shouldDrawSlot(slot);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int x, int y) {}
}

package slimeknights.tconstruct.library.client;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.screen.element.TextureElement;
import slimeknights.tconstruct.TConstruct;

/** TODO: consider using {@link slimeknights.tconstruct.library.recipe.partbuilder.Pattern} instead. */
public interface Icons {
  ResourceLocation ICONS = TConstruct.getResource("textures/gui/icons.png");

  //TextureElement ANVIL = new TextureElement(18 * 3, 0, 18, 18, 256, 256);
  TextureElement PATTERN = new TextureElement(ICONS, 0, 18 * 12, 18, 18, 256, 256);
  //TextureElement SHARD = new TextureElement(18, 18 * 12, 18, 18);
  //TextureElement BLOCK = new TextureElement(18 * 2, 18 * 12, 18, 18);
  //TextureElement PICKAXE = new TextureElement(0, 18 * 13, 18, 18);
  //TextureElement DUST = new TextureElement(18, 18 * 13, 18, 18);
  //TextureElement LAPIS = new TextureElement(18 * 2, 18 * 13, 18, 18);
  TextureElement INGOT = PATTERN.move(18 * 3, 18 * 13, 18, 18);
  //TextureElement GEM = new TextureElement(18 * 4, 18 * 13, 18, 18);
  //TextureElement QUARTZ = new TextureElement(18 * 5, 18 * 13, 18, 18);
  TextureElement BUTTON = PATTERN.move(180, 216, 18, 18);
  TextureElement BUTTON_HOVERED = PATTERN.move(180 + 18 * 2, 216, 18, 18);
  TextureElement BUTTON_PRESSED = PATTERN.move(180 - 18 * 2, 216, 18, 18);

  @Deprecated(forRemoval = true)
  TextureElement PIGGYBACK_1 = PATTERN.move(18 * 13, 0, 18, 18);
  @Deprecated(forRemoval = true)
  TextureElement PIGGYBACK_2 = PATTERN.move(18 * 13, 18, 18, 18);
  @Deprecated(forRemoval = true)
  TextureElement PIGGYBACK_3 = PATTERN.move(18 * 13, 18 * 2, 18, 18);
}

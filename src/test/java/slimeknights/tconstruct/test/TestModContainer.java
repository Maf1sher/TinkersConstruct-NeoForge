package slimeknights.tconstruct.test;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.forgespi.language.IModInfo;

public class TestModContainer extends ModContainer {
  public TestModContainer(IModInfo info) {
    super(info);
    this.contextExtension = () -> null;
  }

  @Override
  public boolean matches(Object mod) {
    return mod == this;
  }

  @Override
  public Object getMod() {
    return this;
  }
}

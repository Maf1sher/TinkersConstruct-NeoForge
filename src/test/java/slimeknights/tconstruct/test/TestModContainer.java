package slimeknights.tconstruct.test;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

public class TestModContainer extends ModContainer {
  public TestModContainer(IModInfo info) {
    super(info);
  }

  @Nullable
  @Override
  public IEventBus getEventBus() {
    return null;
  }
}

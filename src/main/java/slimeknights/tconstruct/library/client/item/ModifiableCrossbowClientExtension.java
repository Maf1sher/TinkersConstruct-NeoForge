package slimeknights.tconstruct.library.client.item;

import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.tconstruct.library.tools.item.ranged.ModifiableCrossbowItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;

/** Client extensions for modifiable crossbows. Adds in the arm pose when charged. */
public class ModifiableCrossbowClientExtension extends ModifiableItemClientExtension {
  public static final ModifiableCrossbowClientExtension INSTANCE = new ModifiableCrossbowClientExtension();

  protected ModifiableCrossbowClientExtension() {}

  @Nullable
  @Override
  public ArmPose getArmPose(LivingEntity living, InteractionHand hand, ItemStack stack) {
    if (!living.swinging) {
      CompoundTag tag = DataComponentHelper.getCustomData(stack);
      // must have ammo in persistent data; an empty compound can be left behind by older saves
      if (tag != null && !tag.getCompound(ToolStack.TAG_PERSISTENT_MOD_DATA).getCompound(ModifiableCrossbowItem.KEY_CROSSBOW_AMMO.toString()).isEmpty()) {
        return ArmPose.CROSSBOW_HOLD;
      }
    }
    return ArmPose.ITEM;
  }
}

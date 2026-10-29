package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class TagUtilTest extends BaseMcTest {
  @Test
  void readOptionalPosReadsRelativeIntArray() {
    // 1.21 writes block positions as int arrays, not compounds
    CompoundTag tag = new CompoundTag();
    tag.put("pos", NbtUtils.writeBlockPos(new BlockPos(2, 3, 4)));

    assertThat(TagUtil.readOptionalPos(tag, "pos", new BlockPos(10, 10, 10))).isEqualTo(new BlockPos(12, 13, 14));
    assertThat(TagUtil.readOptionalPos(tag, "missing", BlockPos.ZERO)).isNull();
  }
}

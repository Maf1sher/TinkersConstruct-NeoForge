package slimeknights.tconstruct.tools.modifiers.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;

/** Codec for the vanilla bonus formulas used by Tinkers' loot functions. */
final class LootBonusFormulaCodec {
  private static final Codec<FormulaKind> KIND_CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
    for (FormulaKind kind : FormulaKind.values()) {
      if (kind.id.equals(id)) {
        return DataResult.success(kind);
      }
    }
    return DataResult.error(() -> "Unknown loot bonus formula: " + id);
  }, kind -> kind.id);

  static final MapCodec<ApplyBonusCount.Formula> CODEC = ExtraCodecs.dispatchOptionalValue(
    "formula", "parameters", KIND_CODEC, LootBonusFormulaCodec::kind, kind -> kind.codec);

  private static FormulaKind kind(ApplyBonusCount.Formula formula) {
    if (formula instanceof ApplyBonusCount.BinomialWithBonusCount) return FormulaKind.BINOMIAL;
    if (formula instanceof ApplyBonusCount.OreDrops) return FormulaKind.ORE_DROPS;
    if (formula instanceof ApplyBonusCount.UniformBonusCount) return FormulaKind.UNIFORM;
    throw new IllegalArgumentException("Unknown loot bonus formula: " + formula.getClass().getName());
  }

  private enum FormulaKind {
    BINOMIAL(ResourceLocation.withDefaultNamespace("binomial_with_bonus_count"), RecordCodecBuilder.<ApplyBonusCount.BinomialWithBonusCount>create(instance -> instance.group(
      Codec.INT.fieldOf("extra").forGetter(ApplyBonusCount.BinomialWithBonusCount::extraRounds),
      Codec.FLOAT.fieldOf("probability").forGetter(ApplyBonusCount.BinomialWithBonusCount::probability)
    ).apply(instance, ApplyBonusCount.BinomialWithBonusCount::new))),
    ORE_DROPS(ResourceLocation.withDefaultNamespace("ore_drops"), Codec.unit(ApplyBonusCount.OreDrops::new)),
    UNIFORM(ResourceLocation.withDefaultNamespace("uniform_bonus_count"), RecordCodecBuilder.<ApplyBonusCount.UniformBonusCount>create(instance -> instance.group(
      Codec.INT.fieldOf("bonusMultiplier").forGetter(ApplyBonusCount.UniformBonusCount::bonusMultiplier)
    ).apply(instance, ApplyBonusCount.UniformBonusCount::new)));

    private final ResourceLocation id;
    private final Codec<? extends ApplyBonusCount.Formula> codec;

    FormulaKind(ResourceLocation id, Codec<? extends ApplyBonusCount.Formula> codec) {
      this.id = id;
      this.codec = codec;
    }
  }

  private LootBonusFormulaCodec() {}
}

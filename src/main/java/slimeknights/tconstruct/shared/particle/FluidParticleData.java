package slimeknights.tconstruct.shared.particle;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

/** Particle data for a fluid particle */
@RequiredArgsConstructor
public class FluidParticleData implements ParticleOptions {
  @Getter
  private final ParticleType<FluidParticleData> type;
  @Getter
  private final FluidStack fluid;

  /** Particle type for a fluid particle */
  public static class Type extends ParticleType<FluidParticleData> {
    private final MapCodec<FluidParticleData> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, FluidParticleData> streamCodec;

    public Type() {
      super(false);
      this.codec = FluidStack.CODEC.xmap(fluid -> new FluidParticleData(this, fluid), FluidParticleData::getFluid).fieldOf("fluid");
      this.streamCodec = FluidStack.STREAM_CODEC.map(fluid -> new FluidParticleData(this, fluid), FluidParticleData::getFluid);
    }

    @Override
    public MapCodec<FluidParticleData> codec() {
      return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, FluidParticleData> streamCodec() {
      return streamCodec;
    }
  }
}

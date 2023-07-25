package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

public class SpikeConfiguration implements FeatureConfiguration {
   public static final Codec<SpikeConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.BOOL.fieldOf("crystal_invulnerable").orElse(false).forGetter((spikeconfiguration2) -> spikeconfiguration2.crystalInvulnerable), SpikeFeature.EndSpike.CODEC.listOf().fieldOf("spikes").forGetter((spikeconfiguration1) -> spikeconfiguration1.spikes), BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter((spikeconfiguration) -> Optional.ofNullable(spikeconfiguration.crystalBeamTarget))).apply(recordcodecbuilder_instance, SpikeConfiguration::new));
   private final boolean crystalInvulnerable;
   private final List<SpikeFeature.EndSpike> spikes;
   @Nullable
   private final BlockPos crystalBeamTarget;

   public SpikeConfiguration(boolean flag, List<SpikeFeature.EndSpike> list, @Nullable BlockPos blockpos) {
      this(flag, list, Optional.ofNullable(blockpos));
   }

   private SpikeConfiguration(boolean flag, List<SpikeFeature.EndSpike> list, Optional<BlockPos> optional) {
      this.crystalInvulnerable = flag;
      this.spikes = list;
      this.crystalBeamTarget = optional.orElse((BlockPos)null);
   }

   public boolean isCrystalInvulnerable() {
      return this.crystalInvulnerable;
   }

   public List<SpikeFeature.EndSpike> getSpikes() {
      return this.spikes;
   }

   @Nullable
   public BlockPos getCrystalBeamTarget() {
      return this.crystalBeamTarget;
   }
}

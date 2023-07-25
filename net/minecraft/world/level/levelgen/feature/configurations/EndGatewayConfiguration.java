package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
   public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockPos.CODEC.optionalFieldOf("exit").forGetter((endgatewayconfiguration1) -> endgatewayconfiguration1.exit), Codec.BOOL.fieldOf("exact").forGetter((endgatewayconfiguration) -> endgatewayconfiguration.exact)).apply(recordcodecbuilder_instance, EndGatewayConfiguration::new));
   private final Optional<BlockPos> exit;
   private final boolean exact;

   private EndGatewayConfiguration(Optional<BlockPos> optional, boolean flag) {
      this.exit = optional;
      this.exact = flag;
   }

   public static EndGatewayConfiguration knownExit(BlockPos blockpos, boolean flag) {
      return new EndGatewayConfiguration(Optional.of(blockpos), flag);
   }

   public static EndGatewayConfiguration delayedExitSearch() {
      return new EndGatewayConfiguration(Optional.empty(), false);
   }

   public Optional<BlockPos> getExit() {
      return this.exit;
   }

   public boolean isExitExact() {
      return this.exact;
   }
}

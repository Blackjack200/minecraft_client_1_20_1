package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig extends BlockPileConfiguration {
   public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("state_provider").forGetter((netherforestvegetationconfig2) -> netherforestvegetationconfig2.stateProvider), ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter((netherforestvegetationconfig1) -> netherforestvegetationconfig1.spreadWidth), ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter((netherforestvegetationconfig) -> netherforestvegetationconfig.spreadHeight)).apply(recordcodecbuilder_instance, NetherForestVegetationConfig::new));
   public final int spreadWidth;
   public final int spreadHeight;

   public NetherForestVegetationConfig(BlockStateProvider blockstateprovider, int i, int j) {
      super(blockstateprovider);
      this.spreadWidth = i;
      this.spreadHeight = j;
   }
}

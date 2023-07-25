package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
   public final BlockStateProvider fillingProvider;
   public final BlockStateProvider innerLayerProvider;
   public final BlockStateProvider alternateInnerLayerProvider;
   public final BlockStateProvider middleLayerProvider;
   public final BlockStateProvider outerLayerProvider;
   public final List<BlockState> innerPlacements;
   public final TagKey<Block> cannotReplace;
   public final TagKey<Block> invalidBlocks;
   public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter((geodeblocksettings7) -> geodeblocksettings7.fillingProvider), BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter((geodeblocksettings6) -> geodeblocksettings6.innerLayerProvider), BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter((geodeblocksettings5) -> geodeblocksettings5.alternateInnerLayerProvider), BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter((geodeblocksettings4) -> geodeblocksettings4.middleLayerProvider), BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter((geodeblocksettings3) -> geodeblocksettings3.outerLayerProvider), ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter((geodeblocksettings2) -> geodeblocksettings2.innerPlacements), TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter((geodeblocksettings1) -> geodeblocksettings1.cannotReplace), TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter((geodeblocksettings) -> geodeblocksettings.invalidBlocks)).apply(recordcodecbuilder_instance, GeodeBlockSettings::new));

   public GeodeBlockSettings(BlockStateProvider blockstateprovider, BlockStateProvider blockstateprovider1, BlockStateProvider blockstateprovider2, BlockStateProvider blockstateprovider3, BlockStateProvider blockstateprovider4, List<BlockState> list, TagKey<Block> tagkey, TagKey<Block> tagkey1) {
      this.fillingProvider = blockstateprovider;
      this.innerLayerProvider = blockstateprovider1;
      this.alternateInnerLayerProvider = blockstateprovider2;
      this.middleLayerProvider = blockstateprovider3;
      this.outerLayerProvider = blockstateprovider4;
      this.innerPlacements = list;
      this.cannotReplace = tagkey;
      this.invalidBlocks = tagkey1;
   }
}

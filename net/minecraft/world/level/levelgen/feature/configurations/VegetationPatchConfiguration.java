package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration implements FeatureConfiguration {
   public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter((vegetationpatchconfiguration9) -> vegetationpatchconfiguration9.replaceable), BlockStateProvider.CODEC.fieldOf("ground_state").forGetter((vegetationpatchconfiguration8) -> vegetationpatchconfiguration8.groundState), PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter((vegetationpatchconfiguration7) -> vegetationpatchconfiguration7.vegetationFeature), CaveSurface.CODEC.fieldOf("surface").forGetter((vegetationpatchconfiguration6) -> vegetationpatchconfiguration6.surface), IntProvider.codec(1, 128).fieldOf("depth").forGetter((vegetationpatchconfiguration5) -> vegetationpatchconfiguration5.depth), Codec.floatRange(0.0F, 1.0F).fieldOf("extra_bottom_block_chance").forGetter((vegetationpatchconfiguration4) -> vegetationpatchconfiguration4.extraBottomBlockChance), Codec.intRange(1, 256).fieldOf("vertical_range").forGetter((vegetationpatchconfiguration3) -> vegetationpatchconfiguration3.verticalRange), Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter((vegetationpatchconfiguration2) -> vegetationpatchconfiguration2.vegetationChance), IntProvider.CODEC.fieldOf("xz_radius").forGetter((vegetationpatchconfiguration1) -> vegetationpatchconfiguration1.xzRadius), Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter((vegetationpatchconfiguration) -> vegetationpatchconfiguration.extraEdgeColumnChance)).apply(recordcodecbuilder_instance, VegetationPatchConfiguration::new));
   public final TagKey<Block> replaceable;
   public final BlockStateProvider groundState;
   public final Holder<PlacedFeature> vegetationFeature;
   public final CaveSurface surface;
   public final IntProvider depth;
   public final float extraBottomBlockChance;
   public final int verticalRange;
   public final float vegetationChance;
   public final IntProvider xzRadius;
   public final float extraEdgeColumnChance;

   public VegetationPatchConfiguration(TagKey<Block> tagkey, BlockStateProvider blockstateprovider, Holder<PlacedFeature> holder, CaveSurface cavesurface, IntProvider intprovider, float f, int i, float f1, IntProvider intprovider1, float f2) {
      this.replaceable = tagkey;
      this.groundState = blockstateprovider;
      this.vegetationFeature = holder;
      this.surface = cavesurface;
      this.depth = intprovider;
      this.extraBottomBlockChance = f;
      this.verticalRange = i;
      this.vegetationChance = f1;
      this.xzRadius = intprovider1;
      this.extraEdgeColumnChance = f2;
   }
}

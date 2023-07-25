package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration implements FeatureConfiguration {
   public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((rootsystemconfiguration12) -> rootsystemconfiguration12.treeFeature), Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree").forGetter((rootsystemconfiguration11) -> rootsystemconfiguration11.requiredVerticalSpaceForTree), Codec.intRange(1, 64).fieldOf("root_radius").forGetter((rootsystemconfiguration10) -> rootsystemconfiguration10.rootRadius), TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable").forGetter((rootsystemconfiguration9) -> rootsystemconfiguration9.rootReplaceable), BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter((rootsystemconfiguration8) -> rootsystemconfiguration8.rootStateProvider), Codec.intRange(1, 256).fieldOf("root_placement_attempts").forGetter((rootsystemconfiguration7) -> rootsystemconfiguration7.rootPlacementAttempts), Codec.intRange(1, 4096).fieldOf("root_column_max_height").forGetter((rootsystemconfiguration6) -> rootsystemconfiguration6.rootColumnMaxHeight), Codec.intRange(1, 64).fieldOf("hanging_root_radius").forGetter((rootsystemconfiguration5) -> rootsystemconfiguration5.hangingRootRadius), Codec.intRange(0, 16).fieldOf("hanging_roots_vertical_span").forGetter((rootsystemconfiguration4) -> rootsystemconfiguration4.hangingRootsVerticalSpan), BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter((rootsystemconfiguration3) -> rootsystemconfiguration3.hangingRootStateProvider), Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts").forGetter((rootsystemconfiguration2) -> rootsystemconfiguration2.hangingRootPlacementAttempts), Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree").forGetter((rootsystemconfiguration1) -> rootsystemconfiguration1.allowedVerticalWaterForTree), BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter((rootsystemconfiguration) -> rootsystemconfiguration.allowedTreePosition)).apply(recordcodecbuilder_instance, RootSystemConfiguration::new));
   public final Holder<PlacedFeature> treeFeature;
   public final int requiredVerticalSpaceForTree;
   public final int rootRadius;
   public final TagKey<Block> rootReplaceable;
   public final BlockStateProvider rootStateProvider;
   public final int rootPlacementAttempts;
   public final int rootColumnMaxHeight;
   public final int hangingRootRadius;
   public final int hangingRootsVerticalSpan;
   public final BlockStateProvider hangingRootStateProvider;
   public final int hangingRootPlacementAttempts;
   public final int allowedVerticalWaterForTree;
   public final BlockPredicate allowedTreePosition;

   public RootSystemConfiguration(Holder<PlacedFeature> holder, int i, int j, TagKey<Block> tagkey, BlockStateProvider blockstateprovider, int k, int l, int i1, int j1, BlockStateProvider blockstateprovider1, int k1, int l1, BlockPredicate blockpredicate) {
      this.treeFeature = holder;
      this.requiredVerticalSpaceForTree = i;
      this.rootRadius = j;
      this.rootReplaceable = tagkey;
      this.rootStateProvider = blockstateprovider;
      this.rootPlacementAttempts = k;
      this.rootColumnMaxHeight = l;
      this.hangingRootRadius = i1;
      this.hangingRootsVerticalSpan = j1;
      this.hangingRootStateProvider = blockstateprovider1;
      this.hangingRootPlacementAttempts = k1;
      this.allowedVerticalWaterForTree = l1;
      this.allowedTreePosition = blockpredicate;
   }
}

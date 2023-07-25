package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class WouldSurvivePredicate implements BlockPredicate {
   public static final Codec<WouldSurvivePredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter((wouldsurvivepredicate1) -> wouldsurvivepredicate1.offset), BlockState.CODEC.fieldOf("state").forGetter((wouldsurvivepredicate) -> wouldsurvivepredicate.state)).apply(recordcodecbuilder_instance, WouldSurvivePredicate::new));
   private final Vec3i offset;
   private final BlockState state;

   protected WouldSurvivePredicate(Vec3i vec3i, BlockState blockstate) {
      this.offset = vec3i;
      this.state = blockstate;
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      return this.state.canSurvive(worldgenlevel, blockpos.offset(this.offset));
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.WOULD_SURVIVE;
   }
}

package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class InsideWorldBoundsPredicate implements BlockPredicate {
   public static final Codec<InsideWorldBoundsPredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", BlockPos.ZERO).forGetter((insideworldboundspredicate) -> insideworldboundspredicate.offset)).apply(recordcodecbuilder_instance, InsideWorldBoundsPredicate::new));
   private final Vec3i offset;

   public InsideWorldBoundsPredicate(Vec3i vec3i) {
      this.offset = vec3i;
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      return !worldgenlevel.isOutsideBuildHeight(blockpos.offset(this.offset));
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.INSIDE_WORLD_BOUNDS;
   }
}

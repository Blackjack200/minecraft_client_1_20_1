package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.WorldGenLevel;

public class HasSturdyFacePredicate implements BlockPredicate {
   private final Vec3i offset;
   private final Direction direction;
   public static final Codec<HasSturdyFacePredicate> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Vec3i.offsetCodec(16).optionalFieldOf("offset", Vec3i.ZERO).forGetter((hassturdyfacepredicate1) -> hassturdyfacepredicate1.offset), Direction.CODEC.fieldOf("direction").forGetter((hassturdyfacepredicate) -> hassturdyfacepredicate.direction)).apply(recordcodecbuilder_instance, HasSturdyFacePredicate::new));

   public HasSturdyFacePredicate(Vec3i vec3i, Direction direction) {
      this.offset = vec3i;
      this.direction = direction;
   }

   public boolean test(WorldGenLevel worldgenlevel, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.offset(this.offset);
      return worldgenlevel.getBlockState(blockpos1).isFaceSturdy(worldgenlevel, blockpos1, this.direction);
   }

   public BlockPredicateType<?> type() {
      return BlockPredicateType.HAS_STURDY_FACE;
   }
}

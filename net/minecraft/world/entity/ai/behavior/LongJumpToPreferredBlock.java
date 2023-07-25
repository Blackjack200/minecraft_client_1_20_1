package net.minecraft.world.entity.ai.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;

public class LongJumpToPreferredBlock<E extends Mob> extends LongJumpToRandomPos<E> {
   private final TagKey<Block> preferredBlockTag;
   private final float preferredBlocksChance;
   private final List<LongJumpToRandomPos.PossibleJump> notPrefferedJumpCandidates = new ArrayList<>();
   private boolean currentlyWantingPreferredOnes;

   public LongJumpToPreferredBlock(UniformInt uniformint, int i, int j, float f, Function<E, SoundEvent> function, TagKey<Block> tagkey, float f1, BiPredicate<E, BlockPos> bipredicate) {
      super(uniformint, i, j, f, function, bipredicate);
      this.preferredBlockTag = tagkey;
      this.preferredBlocksChance = f1;
   }

   protected void start(ServerLevel serverlevel, E mob, long i) {
      super.start(serverlevel, mob, i);
      this.notPrefferedJumpCandidates.clear();
      this.currentlyWantingPreferredOnes = mob.getRandom().nextFloat() < this.preferredBlocksChance;
   }

   protected Optional<LongJumpToRandomPos.PossibleJump> getJumpCandidate(ServerLevel serverlevel) {
      if (!this.currentlyWantingPreferredOnes) {
         return super.getJumpCandidate(serverlevel);
      } else {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         while(!this.jumpCandidates.isEmpty()) {
            Optional<LongJumpToRandomPos.PossibleJump> optional = super.getJumpCandidate(serverlevel);
            if (optional.isPresent()) {
               LongJumpToRandomPos.PossibleJump longjumptorandompos_possiblejump = optional.get();
               if (serverlevel.getBlockState(blockpos_mutableblockpos.setWithOffset(longjumptorandompos_possiblejump.getJumpTarget(), Direction.DOWN)).is(this.preferredBlockTag)) {
                  return optional;
               }

               this.notPrefferedJumpCandidates.add(longjumptorandompos_possiblejump);
            }
         }

         return !this.notPrefferedJumpCandidates.isEmpty() ? Optional.of(this.notPrefferedJumpCandidates.remove(0)) : Optional.empty();
      }
   }
}
